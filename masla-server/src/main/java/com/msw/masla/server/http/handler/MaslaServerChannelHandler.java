package com.msw.masla.server.http.handler;

import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.protocol.http.netty.http.handler.AbstractServerChannelHandler;
import com.msw.masla.protocol.http.netty.pool.SynchronizedStack;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.protocol.http.netty.session.MaslaSession;
import com.msw.masla.server.http.AbstractEndpoint;
import com.msw.masla.server.http.dispatch.MaslaDispatch;
import com.msw.masla.server.http.processor.MaslaProcessorBase;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;

/**
 * Author: Gavin.peng
 * Date: 2024/4/14
 * Description:
 */
public class MaslaServerChannelHandler extends AbstractServerChannelHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MaslaServerChannelHandler.class);


    private static MaslaServerChannelHandler instance;

    protected SynchronizedStack<MaslaProcessorBase> processorCache;

    private MaslaDispatch maslaDispatch;

    private AbstractEndpoint endpoint;

    private Handler[] backupHandlers;

    private Handler[] priorityHandlers;

    //backup queue
    private BlockingQueue backQueue;

    //优先级高的队列
    private BlockingQueue<IOSession> priorityQueue;


    private MaslaServerChannelHandler(AbstractEndpoint endpoint, MaslaDispatch maslaDispatch) {
        super(endpoint.getSessions(), endpoint.getMaxKeepAliveRequests(), endpoint.getMaxKeepAliveRequests());
        this.endpoint = endpoint;
        this.maslaDispatch = maslaDispatch;
        this.processorCache = new SynchronizedStack<MaslaProcessorBase>();
        this.backQueue = endpoint.getBackupqueue();
        this.priorityQueue = endpoint.getPriorityQueue();
        this.backupHandlers = new Handler[endpoint.getBackupHandlers()];
        this.priorityHandlers = new Handler[endpoint.getPriorityHandlers()];
        this.executor = executor;
        //启动backup 线程组
        for(int i=0; i < endpoint.getBackupHandlers(); i++){
            backupHandlers[i] = new Handler(this.backQueue);
            backupHandlers[i].setName("Masla-BackupHandler-Thread-"+(i+1));
            backupHandlers[i].start();
        }
        //启动高优先级线程组
        for(int i=0; i < endpoint.getPriorityHandlers(); i++){
            priorityHandlers[i] = new Handler(this.priorityQueue);
            priorityHandlers[i].setName("Masla-PriorityHandler-Thread-"+(i+1));
            priorityHandlers[i].start();
        }
    }


    public static MaslaServerChannelHandler getInstance(AbstractEndpoint endpoint, MaslaDispatch maslaDispatch){

        if(instance != null){
            return instance;
        }
        if(endpoint == null){
            return null;
        }
        synchronized (MaslaServerChannelHandler.class){
            if(instance == null){
                instance = new MaslaServerChannelHandler(endpoint, maslaDispatch);
            }
        }
        return instance;

    }

    public static MaslaServerChannelHandler getInstance(){
        return instance;
    }


    @Override
    public void asyncProcessRequest(IOSession session) {
        if (!StringUtil.isEmptyString(session.getContextRoot())
                && MaslaSpringContextUtil.getMaslaConfConfigBean().getPrioritySet().contains(session.getContextRoot())) {
            inPriorityQueue(session);
        } else if (Constants.MASLA_META_CONTEXT.equals(session.getContextRoot()) || Constants.MASLA_API_PATH.equals(session.getContextRoot())) {
            inBackupQueue(session);
        } else {
            inCommonQueue(session);
        }
    }

    @Override
    public void syncProcessRequest(IOSession session) {
        try {
            if(session.isActive() || session.getHttpRequest().method() == HttpMethod.POST) {
                maslaDispatch.dispatch(session, session.getHttpRequest());
            }else{
                ReferenceCountUtil.release(session.getHttpRequest());
                LOG.warn("Masla found request {} channel {} is close create time {},so not proxy to upstream!!!",session.getContextRoot()+session.getPath(),session.getChannel().remoteAddress(),session.getCreateTime());
            }
        } catch (Throwable e) {
            try {
                LOG.error("Masla process request {} from {} failed:", session.getContextRoot() + session.getPath(), session.getChannel().remoteAddress().toString(), e);
            } catch (Throwable ee) {
                LOG.error("Masla process request {} from {} record log failed:", session.getContextRoot() + session.getPath(), session.getChannel().remoteAddress().toString(), e);
            }
        }
    }


    private void inPriorityQueue(IOSession session){
        try {
            this.priorityQueue.add(session);
        }catch (IllegalStateException e){
            LOG.warn("Masla found priority queue is full!!!,so request {} add to backup queue", session.getHttpRequest().uri());
            try {
                this.backQueue.add(session);
                LOG.warn("Masla add request {} from priority queue to backup queue is ok!!!", session.getHttpRequest().uri());
            }catch (IllegalStateException e1){
                LOG.warn("Masla add request {} from priority queue to backup queue failed,backup queue is full!!!", session.getHttpRequest().uri());
                discardRequest(session,session.getHttpRequest());
            }
        }
    }

    public void inBackupQueue(IOSession session){

        try {
            this.backQueue.add(session);
        }catch (IllegalStateException e1){
            LOG.warn("Masla found backup queue is full!!!,so request {} is discard", session.getHttpRequest().uri());
            discardRequest(session,session.getHttpRequest());
        }
    }





    private void inCommonQueue(IOSession session){

        FullHttpRequest fullHttpRequest = session.getHttpRequest();
        MaslaProcessorBase processor =  this.processorCache.pop();
        if (processor == null) {
            processor = new MaslaProcessor(session, fullHttpRequest);
        } else {
            processor.reset(session, fullHttpRequest);
        }

        try {
            executor.execute(processor);
        } catch (Throwable e) {
            LOG.warn("Masla found accept queue is full!!!,so request {}  add to backup queue", fullHttpRequest.uri());
            //main executor pool is full,so add to queue
            try {
                //线程池队列已满，交给备用队列处理
                this.inBackupQueue(session);
                //回收processor
                processorCache.push(processor);
            } catch (Throwable e1) {
                LOG.warn("Masla found backup queue is full!!!,maybe too many request,so refuse the request", fullHttpRequest.uri());
                processorCache.push(processor);
                discardRequest(session,fullHttpRequest);
            }

        }


    }


    @Override
    public void clearAllSession() {
        this.endpoint.clearAllSession();
    }

    @Override
    public void shutdown() {
        for(Handler backupHandler : backupHandlers){
            backupHandler.shutdown();
        }

        for(Handler priortiyHandler : priorityHandlers){
            priortiyHandler.shutdown();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Masla found channel {} unregistered!", ctx.channel().remoteAddress());
        }
        Channel channel = ctx.channel();
        String channelKey = getChannelKey((InetSocketAddress) channel.localAddress(),
                (InetSocketAddress) channel.remoteAddress(),channel);
        IOSession session = sessions.remove(channelKey);
        if(session != null){
            this.endpoint.remveSession(channelKey,session);
        }
        //H2的连接不减少，因为前面注册时也没有增加
        if (isSslChannel(channel)) {
            //减少也是，只对http/1.1 做递减计数
            this.currentSessions.decrementAndGet();
        }else{
            if(LOG.isInfoEnabled()){
                LOG.info("Masla found ssl channel {} unregistered!", ctx.channel().remoteAddress());
            }
        }
        if(session != null){
            MaslaSession maslaSession = (MaslaSession)session;
            long connectedTime = System.currentTimeMillis()- maslaSession.getCreateTime();
            long lastWorkTime = System.currentTimeMillis() - session.getActiveTime();
            if(LOG.isInfoEnabled()) {
                LOG.info("Masla receive nginx FIN code from channel {},session connected time {} exec task {} last process time {} so do close", channelKey, connectedTime, maslaSession.getKeepAlive(), lastWorkTime);
            }
            session.error(null);
        }

        super.channelUnregistered(ctx);
    }


    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {

        if(LOG.isInfoEnabled()) {
            LOG.info("Masla receive channel {} event {}", ctx.channel().remoteAddress(),evt.toString());
        }
        if (evt instanceof IdleStateEvent) {
            //如果发生读空闲，而且连接在用，说明超时,需要关闭连接
            final String sessionKey = getChannelKey((InetSocketAddress) ctx.channel().localAddress(),
                    (InetSocketAddress) ctx.channel().remoteAddress(), ctx.channel());
            final IOSession session = sessions.get(sessionKey);
            if(((IdleStateEvent) evt).state() == IdleState.READER_IDLE){
                if(!ctx.channel().closeFuture().isDone()) {
                    LOG.warn("Masla found channel {} is idle so do close",ctx.channel().remoteAddress());
                    //先关闭，后删除session，防止空闲时，该连接正有请求发过来，找不到session
                    if(session != null) {
                        session.error(new IOSession.SessionListener() {
                            @Override
                            public void close() {
                                IOSession session1 = sessions.remove(sessionKey);
                                if(session1 != null){
                                    endpoint.remveSession(sessionKey,session);
                                    LOG.warn("Masla session {} is idle close complete and remove sessions ok!!!",sessionKey);
                                }
                            }
                        });
                    }else {
                        ctx.channel().close();
                    }

                }else{
                    LOG.info("Masla found channel {} is idle but already close",ctx.channel().remoteAddress());
                }
            }
        }else if(evt instanceof SslHandshakeCompletionEvent){
            //https 握手时已经有过注册事件了，但是this handler 还没有添加到pipline，所以
            //这个handler不会有注册事件发生，只能通过握手事件来代替注册事件
            if(LOG.isInfoEnabled()) {
                LOG.info("Masla receive channel {} ssl handshake complete event", ctx.channel().remoteAddress());
            }

            SslHandshakeCompletionEvent handshakeEvent = (SslHandshakeCompletionEvent) evt;
            if (handshakeEvent.isSuccess()) {
                SslHandler sslHandler = ctx.pipeline().get(SslHandler.class);
                if (sslHandler == null) {
                    throw new IllegalStateException("cannot find an SslHandler in the pipeline (required for "
                            + "application-level protocol negotiation)");
                }
                String protocol = sslHandler.applicationProtocol();
                if(LOG.isInfoEnabled()) {
                    LOG.info("Masla found channel {} ssl protocol negotiation result {}", ctx.channel().remoteAddress(), protocol);
                }
                if (protocol == null || ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                    if(LOG.isInfoEnabled()) {
                        LOG.info("Masla found ssl protocol negotiation result {}", protocol);
                    }
                    Channel channel = ctx.channel();
                    String channelKey = getChannelKey((InetSocketAddress) channel.localAddress(),
                            (InetSocketAddress) channel.remoteAddress(), channel);
                    IOSession session = new MaslaSession(channelKey, channel);
                    //这里不同的链接key是不一样的，所以可以用put方法。
                    sessions.put(channelKey, session);
                    return;
                }
            }else{
                LOG.warn("Masla found channel {} ssl handshake failed:{}",ctx.channel().remoteAddress(),handshakeEvent.cause());
            }
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LOG.error("Receive exception event from channel {} exception:",ctx.channel().remoteAddress(),cause);
        final String sessionKey = getChannelKey((InetSocketAddress) ctx.channel().localAddress(),
                (InetSocketAddress) ctx.channel().remoteAddress(), ctx.channel());
        final IOSession session = sessions.remove(sessionKey);
        if(session != null){
            endpoint.remveSession(sessionKey,session);
        }
        if(session != null){
            session.error(null);
//            session.close(null);
        }else {
            ctx.channel().close();
        }

    }

    public AbstractEndpoint getEndpoint() {
        return endpoint;
    }


    private class Handler extends Thread{
        private volatile boolean running;
        private BlockingQueue<IOSession> deque;

        public Handler(BlockingQueue queue){
            this.running = true;
            this.deque = queue;
        }

        @Override
        public void run(){
            while (running){

                IOSession session = null;
                try {
                    session = deque.take();
                } catch (InterruptedException e) {
                    //LOG.warn("Masla {} is interrupted!!!",this.getName());
                    break;
                }catch (Throwable e){
                    LOG.warn("Masla thread {} get session failed:",this.getName(),e);
                }

                if(session != null) {
                    syncProcessRequest(session);
                }
            }
            //LOG.warn("Masla {} is exit!!!",this.getName());

        }

        public void shutdown(){
            this.running = false;
            this.interrupt();
        }


    }


    private class MaslaProcessor extends MaslaProcessorBase {


        public MaslaProcessor(IOSession session, FullHttpRequest request){
            super(session,request);
        }


        @Override
        public void doRun() {
            try {
                syncProcessRequest(session);
            }finally {
                processorCache.push(this);
            }

        }
    }
}
