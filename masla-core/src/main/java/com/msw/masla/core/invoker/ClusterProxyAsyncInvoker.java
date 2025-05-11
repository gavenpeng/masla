package com.msw.masla.core.invoker;

import com.msw.masla.common.circuit.CircuitRuleDefine;
import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.enums.HostStatus;
import com.msw.masla.common.monitor.metrics.AppRequestFailedCount;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.core.async.repsone.MaslaHttpDecode;
import com.msw.masla.core.discovery.nacos.HostProfile;
import com.msw.masla.core.discovery.nacos.MaslaNacosServiceDiscovery;
import com.msw.masla.core.invoker.loadbalance.LoadBalance;
import com.msw.masla.core.invoker.loadbalance.LoadBalanceFactory;
import com.msw.masla.core.invoker.loadbalance.MaslaDefaultLoadBalanceFactory;
import com.msw.masla.core.push.engine.AsyncPushEngine;
import com.msw.masla.core.push.engine.PushEngine;
import com.msw.masla.metrics.http.QpsMetrics;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.event.EventState;
import com.msw.masla.protocol.http.netty.exception.MaslaException;
import com.msw.masla.protocol.http.netty.exception.NoAvailableConnectionException;
import com.msw.masla.protocol.http.netty.exception.NoAvailableHostException;
import com.msw.masla.protocol.http.netty.http.HostInstance;
import com.msw.masla.protocol.http.netty.http.connection.MaslaChannelPoolManager;
import com.msw.masla.protocol.http.netty.http.connection.MaslaHttpClientHandler;
import com.msw.masla.protocol.http.netty.http.decode.MaslaDecode;
import com.msw.masla.protocol.http.netty.pool.MaslaChannelPool;
import com.msw.masla.protocol.http.netty.pool.SimpleChannelPool;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.handler.codec.PrematureChannelClosureException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericProgressiveFutureListener;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gavin.peng on 2024/5/22.
 */
@Data
public class ClusterProxyAsyncInvoker extends AbstractProxyInvoker {

    protected static final Logger LOG = LoggerFactory.getLogger(ClusterProxyAsyncInvoker.class);

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5, new DefaultThreadFactory("Masla-Timeout-Thread"));

    private final MaslaDefaultProxyInvokerFactory factory;

    private final MaslaNacosServiceDiscovery maslaNacosServiceDiscovery;

    private final LoadBalanceFactory<HostInstance> loadBalanceFactory;

    private final MaslaChannelPoolManager maslaChannelPoolManager;

    private long timeout;


    public ClusterProxyAsyncInvoker(MaslaDefaultProxyInvokerFactory factory, MaslaNacosServiceDiscovery maslaNacosServiceDiscovery) {
        super();
        this.factory = factory;
        this.maslaNacosServiceDiscovery = maslaNacosServiceDiscovery;
        this.loadBalanceFactory = MaslaDefaultLoadBalanceFactory.getInstance();
        this.maslaChannelPoolManager = MaslaChannelPoolManager.getInstance();
    }

    public void setTimeout(Long timeout) {
        if(timeout != null && timeout > 0){
            this.timeout = timeout;
        }
    }


    @Override
    public void doInvoke(SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext) {
        HttpRequest httpRequest = maslaContext.getHttpRequest();
        ServiceApp serviceApp = maslaContext.getService();
        try {
            //get route app service instance from nacos
            List<HostInstance> instanceList = maslaNacosServiceDiscovery.getAvailableInstances(serviceApp.getName(), false);
            List<HostInstance> tagList = maslaNacosServiceDiscovery.getAvailableInstances(serviceApp.getName(), true);
            //selected instance by load balance
            LoadBalance<HostInstance> loadBalance = loadBalanceFactory.getLoadBalance(serviceApp.getLoadBalanceName());
            HostInstance hostInstance = loadBalance.select(instanceList, tagList, maslaContext.getRouteTag());
            if (hostInstance == null) {
                throw new NoAvailableHostException("No servers host available for service:" + serviceApp.getName());
            }
            //get channel pool with selected instance
            MaslaChannelPool channelPool = maslaChannelPoolManager.getChannelPool(hostInstance, serviceApp);
            BaseEvent event = maslaContext.getEvent();
            event.setState(EventState.REQUESTING);
            event.increaseExecCount();
            ((FullHttpRequest)httpRequest).retain(event.getMaxRedoCount());
            event.setStartAcquireConnTime(System.nanoTime());
            Future<Channel> channelFuture = channelPool.acquire();
            if(channelFuture.isDone() && !channelFuture.isSuccess()) {
                LOG.error("Masla do request {} acquire channel failed:{}", maslaContext.getRequestUrl(),channelFuture.cause().getMessage());
                throw channelFuture.cause();
            }else {
                channelFuture.addListener(new AsyncConnRequestCallback(httpRequest, channelPool, maslaContext, event));
            }
        }catch (NoAvailableConnectionException e){
            LOG.error("Masla found request {} no available connection",httpRequest.uri(),e);
            localExecFailed(maslaContext,e);
        }catch (IllegalArgumentException e){
            LOG.error("Masla found request {} protocol not support",httpRequest.uri());
            localExecFailed(maslaContext,e);
        }catch (NoAvailableHostException e){

            String serviceIdentify = maslaContext.getServiceIdentify();
            if (serviceIdentify != null) {
                AppRequestFailedCount appRequestFailedCount = serviceApp.getAppRequestFailedCount(serviceIdentify);
                if (appRequestFailedCount != null) {
                    appRequestFailedCount.getNoAvailableHostCount().incrementAndGet();
                }
            }
            LOG.error("Masla found request {} no available host",httpRequest.uri());
            localExecFailed(maslaContext,e);
        }catch (Throwable e){
            LOG.error("Masla send http request {} failed:",httpRequest.uri(), e);
            if(checkNettyQueueFullException(e)){
                countQueueFull(maslaContext);
            }
            localExecFailed(maslaContext,e);
        }

    }

    private boolean checkNettyQueueFullException(Throwable e){
        return e instanceof RejectedExecutionException;
    }

    private void countQueueFull(SessionContext maslaContext){
        try {
            String serviceId = maslaContext.getServiceIdentify();
            if (serviceId != null) {
                AppRequestFailedCount appRequestFailedCount = maslaContext.getService().getAppRequestFailedCount(serviceId);
                if (appRequestFailedCount != null) {
                    appRequestFailedCount.getQueueFullCount().incrementAndGet();
                }
            }
        }catch (Throwable e){
            LOG.error("Masla statics CountQueueFull failed",e);
        }
    }

    private void sendRequest(HttpRequest httpRequest,
                             MaslaChannelPool channelPool, SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, Channel channel, BaseEvent event) throws Exception{
        try {
            if(LOG.isDebugEnabled()){
                LOG.debug("Masla start to send request {} to remote {}", maslaContext.getHttpRequest().uri(), channel.remoteAddress());
            }
            event.setStartSendTime(System.nanoTime());

            if(channel.isActive()) {
                maslaContext.setRouteHost(channelPool.getHostInstance().getHost());
                QpsMetrics.recordQpsMertic(maslaContext.getServiceIdentify(), maslaContext.getService() ,channelPool.getHostInstance().getHost(), maslaContext.isStressRequest());
                MaslaHttpClientHandler handler = (MaslaHttpClientHandler) channel.pipeline().get(MaslaHttpClientHandler.MASLA_NETTY_CLIENT_HANDLE);
                if(handler != null) {
                    if (handler.getMaslaDecode() == null) {
                        handler.setMaslaDecode(MaslaHttpDecode.getInstance());
                    }

                    bindChannelContext(maslaContext, channel);
                    ScheduledFuture<?> timeoutFuture = scheduledThreadPoolExecutor.schedule(new TimeoutTask(channel), maslaContext.getTimeout(), TimeUnit.MILLISECONDS);
                    maslaContext.setScheduledFuture(timeoutFuture);
                    final ChannelFuture future = channel.writeAndFlush(httpRequest,channel.newProgressivePromise());
                    future.addListener(new AsyncRequestExecutionCallback(maslaContext,channelPool));
                    SessionContext<IOSession, HttpRequest, HttpResponse> requestContext = channel.attr(SessionContext.CONTEXT_KEY).get();
                    if(requestContext != null) {
                        channelPool.addPendingOutboundBytes(requestContext.getRequestLineSize()+requestContext.getRequestHeaderSize()+requestContext.getRequestBodySize());
                    }
                }else{
                    if(LOG.isInfoEnabled()) {
                        LOG.info("Masla found channel {} pipeline not contain handler {} ,maybe is destroy,so close retry", channel.remoteAddress(), MaslaHttpClientHandler.MASLA_NETTY_CLIENT_HANDLE);
                    }
                    closeAndRetry(httpRequest,channelPool, maslaContext,channel,new PrematureChannelClosureException("Channel closed"));
                }
            } else {
                LOG.info("Masla send request {} to channel {} but channel is closed", maslaContext.getHttpRequest().uri(), channel.remoteAddress());
                closeAndRetry(httpRequest,channelPool, maslaContext,channel,new PrematureChannelClosureException("Channel closed"));
            }

        }catch (Throwable e){
            //防止异步提交队列时，满了被拒绝,需要检查是否是队列满的异常。如果是满了，则要自动下线。nginx 走 backup
            if(this.checkNettyQueueFullException(e)){
                countQueueFull(maslaContext);
            }
            asyncExecFailed(maslaContext,channelPool,channel,e,event);
            //throw e;
        }
    }


    private void closeAndRetry(final HttpRequest httpRequest,
                               final MaslaChannelPool channelPool, final SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, final Channel channel, final Throwable cause) throws Exception {

        this.cleanChannelContext(maslaContext,channel);
        final BaseEvent event = maslaContext.getEvent();
        if (!channel.closeFuture().isDone()){
            channel.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    doRetry(channelPool,httpRequest, maslaContext,event,channel,cause);
                }
            });
        }else{
            doRetry(channelPool,httpRequest, maslaContext,event,channel,cause);
        }
    }


    private void doRetry(MaslaChannelPool channelPool, HttpRequest httpRequest, SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, BaseEvent event, Channel channel, Throwable cause) throws Exception{
        this.releaseChannel(channel);
        if (event.isRetryable()) {
            long pendingBytes = maslaContext.getRequestLineSize()+ maslaContext.getRequestHeaderSize()+ maslaContext.getRequestBodySize();
            channelPool.addPendingOutboundBytes(-pendingBytes);
            event.increaseExecCount();
            MaslaChannelPool newPool = retrySelectHostInstance(maslaContext, channelPool.getHostInstance());
            newPool.acquire().addListener(new AsyncConnRequestCallback(httpRequest, newPool, maslaContext, event));
        }else{
            asyncExecFailed(maslaContext, channelPool, channel, new MaslaException("Masla retry " + event.getExecCount() + " reach max !!!",cause), event);
        }
    }



    private MaslaChannelPool retrySelectHostInstance(SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, HostInstance hostInstance) throws Exception {

        List<HostInstance> instanceList = maslaNacosServiceDiscovery.getAvailableInstances(maslaContext.getService().getName(), false);
        List<HostInstance> tagList = maslaNacosServiceDiscovery.getAvailableInstances(maslaContext.getService().getName(), true);
        List<HostInstance> usedList = new ArrayList<>();
        usedList.add(hostInstance);
        //selected instance by load balance
        LoadBalance<HostInstance> loadBalance = loadBalanceFactory.getLoadBalance(maslaContext.getService().getLoadBalanceName());
        HostInstance reSelectHost = loadBalance.retrySelect(instanceList, tagList, usedList);
        return maslaChannelPoolManager.getChannelPool(reSelectHost, maslaContext.getService());
    }

    private void releaseChannel(Channel channel){
        final SimpleChannelPool channelPool = channel.attr(SimpleChannelPool.POOL_KEY).get();
        if(channelPool != null) {
            try {
                channelPool.release(channel, channel.voidPromise());
            }catch (Throwable e){
                LOG.error("Masla found channle {} is already release to pool",channel.remoteAddress());
            }
        }
    }


    private void bindChannelContext(SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, Channel channel) {
        //TODO context is reuse for thread local
        channel.attr(SessionContext.CONTEXT_KEY).set(maslaContext);
        //channel.attr(BaseEvent.EVENT_KEY).set(event);
    }


    private boolean cleanChannelContext(SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, Channel channel){
        if(channel.attr(SessionContext.CONTEXT_KEY).getAndSet(null) != null) {
            if (maslaContext.getScheduledFuture() != null) {
                maslaContext.getScheduledFuture().cancel(false);
            }
            return true;
        }
        return false;
    }


    private void localExecFailed(SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, final Throwable e) {//todo 添加监控，app级别、异常分类
        //recordFaildMertic(maslaContext);
        BaseEvent event = maslaContext.getEvent();
        if(event != null) {
            long opt = System.nanoTime();
            if(event.getStartAcquireConnTime() <=0) {
                event.setStartAcquireConnTime(opt);
            }
            if(event.getStartSendTime() <=0) {
                event.setStartSendTime(opt);
            }
            if(event.getSendCompleteTime() <=0) {
                event.setSendCompleteTime(opt);
            }
            if(event.getResponseCompleteTime() <=0) {
                event.setResponseCompleteTime(opt);
            }
            event.setRemoteException(e);
            event.setState(EventState.REQUEST_FAILED);
            LOG.error("Masla exec request {} status {} failed:{}", maslaContext.getRequestUrl(), event.getState(), e.getMessage());
            PushEngine pushEngine = AsyncPushEngine.getPushEngine(factory);
            pushEngine.push(maslaContext, event);
        }
    }


    private void asyncExecFailed(final SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, final MaslaChannelPool pool, final Channel channel, final Throwable e, final BaseEvent event){

        if(LOG.isWarnEnabled()) {
            LOG.warn("Masla exec request {} status {} exec count {} async failed:", maslaContext.getRequestUrl(), event.getState(), event.getExecCount(), e);
        }
        QpsMetrics.recordQpsMertic(maslaContext.getServiceIdentify(), maslaContext.getService() ,pool.getHostInstance().getHost(), maslaContext.isStressRequest());
        long now = System.nanoTime();
        event.setStartSendTime(now);
        event.setSendCompleteTime(now);

        try {
            if (channel != null) {
                if(this.cleanChannelContext(maslaContext, channel)) {
                    if (!channel.closeFuture().isDone()) {
                        channel.close().addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                SimpleChannelPool pool1 = channel.attr(SimpleChannelPool.POOL_KEY).get();
                                if (pool1 != null) {
                                    pool1.release(channel, channel.voidPromise());
                                    responseError(maslaContext, event, e);
                                } else {
                                    responseError(maslaContext, event, e);
                                }
                            }
                        });
                    } else {
                        //如果链接已经关闭，直接释放，不提交任务到
                        SimpleChannelPool pool1 = channel.attr(SimpleChannelPool.POOL_KEY).get();
                        if (pool1 != null) {
                            pool1.release(channel, channel.voidPromise());
                            responseError(maslaContext, event, e);
                        } else {
                            responseError(maslaContext, event, e);
                        }
                    }
                }else{
                    //channel上的请求上下文已经被清空，说明该请求已经push，并且该channel已经close，所以这里不需要在响应了
                    //确保这种情况能释放链接池。
                    SimpleChannelPool pool1 = channel.attr(SimpleChannelPool.POOL_KEY).get();
                    if (pool1 != null) {
                        pool1.release(channel, channel.voidPromise());
                    }
                    LOG.warn("Masla found request {} is push,so do nothing", maslaContext.getRequestUrl());
                }
            } else {
                responseError(maslaContext, event, e);
            }
        }catch (Throwable ee){
            responseError(maslaContext, event, ee);
        }

    }


    private void responseError(SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, BaseEvent event, Throwable e) {
        PushEngine pushEngine = AsyncPushEngine.getPushEngine(factory);
        Throwable ee = e instanceof MaslaException ?e.getCause():e;
        if(ee == null){
            ee = e;
        }
        event.setRemoteException(ee);
        pushEngine.push(maslaContext,event);
    }


    private void tempDisableHostProfile(HostProfile hostProfile){
        //临时降级处理
        if(hostProfile.getCurStatus() == HostStatus.ENABLE) {
            hostProfile.setCurStatus(HostStatus.TEMP_DISENABLE);
            if(LOG.isInfoEnabled()) {
                LOG.info("Masla found host {} port {} is connection refuse so do turn off,status {}", hostProfile.getHost(), hostProfile.getPort(), hostProfile.getCurStatus().name());
            }
        }else if(hostProfile.getCurStatus() == HostStatus.EXCLUDE){
            //健康检查成功后，可以恢复会exclude的状态 EXCLUDE--> EXC_DISENABLE--> EXCLUDE
            hostProfile.setCurStatus(HostStatus.EXC_TEMP_DISENABLE);
        }
    }

    /**
     * 获取连接Callback
     */
    class AsyncConnRequestCallback implements FutureListener<Channel>{

        private HttpRequest httpRequest;
        private MaslaChannelPool channelPool;
        private SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext;
        private BaseEvent event;

        AsyncConnRequestCallback(HttpRequest httpRequest, MaslaChannelPool channelPool,
                                 SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, BaseEvent event){
            this.httpRequest = httpRequest;
            this.channelPool = channelPool;
            this.maslaContext = maslaContext;
            this.event = event;
        }

        @Override
        public void operationComplete(Future<Channel> future) throws Exception {

            if(checkRequestTimeout(maslaContext,event,channelPool,future)){
                return;
            }

            if(future.isSuccess()) {
                //获取连接成功
                sendRequest(this.httpRequest, this.channelPool, maslaContext,future.getNow(),event);
            }else{

                if(LOG.isInfoEnabled()) {
                    LOG.info("Masla send request {} found acquire channel failed {},start switch channel to retry", maslaContext.getRequestUrl(), future.cause().getMessage());
                }

                if (future.cause() != null){
                    if(checkNettyQueueFullException(future.cause())){
                        countQueueFull(maslaContext);
                        event.setRetryable(false);
                    }else {
                        String errMsg = future.cause().getMessage();
                        if (errMsg != null) {
                            if (errMsg.startsWith(Constants.ACQUIRE_CONN_QUEUE_FULL_EXCEPTION)
                                    || errMsg.startsWith(Constants.WAIT_TIMEOUT_EXCEPTION)) {//todo 添加监控报警
                                event.setRetryable(false);

                            } else {
                                if (!errMsg.contains(Constants.TIMED_OUT_EXCEPTION)) {
                                    String appName = maslaContext.getService().getName();
//                                    if (appName.endsWith(Constants.VARNISH_APP_SUFFIX)) {
//                                        HostProfile hostInstance = (HostProfile) this.channelPool.getHostInstance();
//                                        tempDisableHostProfile(hostInstance);
//                                    }
                                }
                                //2 再检查当前是否有熔断，如果是熔断状态，则不重试
                                CircuitRuleDefine apiCircuitDO = maslaContext.getService().getDefaultCircuit();
                                if (apiCircuitDO != null && apiCircuitDO.isDoDisalbed()) {
                                    event.setRetryable(false);
                                }
                            }
                        } else {
                            LOG.warn("Masla send request {} found acquire channel failed and cause msg is null stack:", maslaContext.getRequestUrl(), future.cause());
                        }
                    }
                }

                try {
                    if (event.isRetryable()) {
                        event.increaseExecCount();
                        MaslaChannelPool newPool = retrySelectHostInstance(maslaContext, channelPool.getHostInstance());
                        newPool.acquire().addListener(new AsyncConnRequestCallback(httpRequest, newPool, maslaContext, event));
                    } else {
                        asyncExecFailed(maslaContext, channelPool, null, new MaslaException(future.cause().getMessage() + " retry " + event.getExecCount() + " reach max!!!", future.cause()), event);
                    }
                }catch (Throwable e){
                    LOG.error("Masla exec request {} retry get connection failed:", maslaContext.getRequestUrl(), e.getMessage());
                    if(checkNettyQueueFullException(e)){
                        countQueueFull(maslaContext);
                    }
                    asyncExecFailed(maslaContext, channelPool, null, new MaslaException(future.cause().getMessage() + " retry " + event.getExecCount() + " reach max!!!", future.cause()), event);
                }
            }
        }
    }


    /**
     * 检查请求的整体时间是否大于nginx的等待时间,如果大于则不再转发该请求
     * @param maslaContext
     * @param event
     * @param channelPool
     * @param future
     * @return
     */
    private boolean checkRequestTimeout(SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, BaseEvent event, MaslaChannelPool channelPool, Future<Channel> future){
        //检查获取超时的时间，如果获取链接耗时过多，大于超时时间，则不再转发。
        try {
            //copy的流量不检查
            long downStreamTimeout = maslaContext.getTimeout();
            if(downStreamTimeout>0){
                long queueTime = 0;
                long now = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
//                if(maslaContext.getSession() != null) {
//                    queueTime = event.getStart() - maslaContext.getSession().getActiveTime();
//                }
                long gwTime = event.getStartAcquireConnTime() - event.getStart();
                long acquireTime = now - event.getStartAcquireConnTime();
                //如果网关发现请求的处理时间已经大于nginx的等待时间,则不再转发，记录409
                long gwCost = acquireTime + gwTime ;
                if (downStreamTimeout > 0 && gwCost >= downStreamTimeout) {
                    LOG.warn("Masla found request {} queueTime {} gwTime {} acquireTime {} total cost more than nginx wait time {},so discard!!", maslaContext.getRequestUrl(), queueTime, gwTime, acquireTime, downStreamTimeout);
                    //如果获取到了链接，需要释放链接
                    if (future.isSuccess()) {
                        Channel channel = future.getNow();
                        //不需要回调，设置空的promise,避免新创建一个
                        channelPool.release(channel, channel.voidPromise());
                    }
                    localExecFailed(maslaContext, new MaslaException("Acquire connection cost " + gwCost + " more than nginx wait time " + downStreamTimeout));
                    return true;
                }
            }

        } catch (Throwable e) {
            if(checkNettyQueueFullException(e)){
                countQueueFull(maslaContext);
            }
            LOG.error("Masla check request {} process timeout failed:", maslaContext.getRequestUrl(), e);
        }
        return false;
    }


    /**
     * 发送请求Callback
     */
    class AsyncRequestExecutionCallback implements GenericProgressiveFutureListener<ChannelProgressiveFuture> {

        private SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext;
        private MaslaChannelPool channelPool;


        AsyncRequestExecutionCallback(SessionContext<IOSession, HttpRequest, HttpResponse> maslaContext, MaslaChannelPool channelPool){
            this.maslaContext = maslaContext;
            this.channelPool = channelPool;
        }


        /**
         * Invoked when the operation has progressed.
         *
         * @param future
         * @param progress the progress of the operation so far (cumulative)
         * @param total    the number that signifies the end of the operation when {@code progress} reaches at it.
         *                 {@code -1} if the end of operation is unknown.
         */
        @Override
        public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {

            BaseEvent event = this.maslaContext.getEvent();
            long now  = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            long ioQueueCost = event.getStartEncodeTime() - event.getStartSendTime();


            long sendCost = now - event.getStartEncodeTime();
            long gwCost = now - event.getStartSendTime();
            if(gwCost > this.maslaContext.getTimeout()){
                LOG.warn("Masla found request {} io queue cost {} send timeout cost {} send {} total size {}, gw cost {} > execApi timeout {}",this.maslaContext.getHttpRequest().uri(),ioQueueCost,sendCost,progress,total, gwCost, this.maslaContext.getTimeout());
            }else{
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Masla found request {} send progress cost {} send {} total size {}", this.maslaContext.getRequestUrl(), sendCost, progress, total);
                }
            }
        }

        /**
         * Invoked when the operation associated with the {@link Future} has been completed.
         *
         * @param future the source {@link Future} which called this callback
         */
        @Override
        public void operationComplete(ChannelProgressiveFuture future) throws Exception {
            BaseEvent event = this.maslaContext.getEvent();
            Channel channel = future.channel();
            if(future.isSuccess()){
                if(event != null) {
                    event.setState(EventState.REQUEST_COMPLETE);
                    event.setSendCompleteTime(System.nanoTime());
                }

                //记录已经发送完成的bytes
                channelPool.addMaxSendOKBytes(this.maslaContext.getRequestLineSize()+this.maslaContext.getRequestHeaderSize()+this.maslaContext.getRequestBodySize());
                if(LOG.isDebugEnabled()){
                    LOG.debug("Request {} is send complete!!!", maslaContext.getRequestUrl());
                }
            }else{
                //检查是否可以重试
                SessionContext<IOSession, HttpRequest, HttpResponse> requestContext = channel.attr(SessionContext.CONTEXT_KEY).get();
                if(requestContext != null && future.cause() instanceof ClosedChannelException
                        && event.isRetryable()) {
                    FullHttpRequest retryHttpRequest = ((FullHttpRequest) maslaContext.getHttpRequest()).duplicate();
                    if(LOG.isInfoEnabled()) {
                        LOG.info("retry request content readIndex {} writer Index {}", retryHttpRequest.content().readerIndex(), retryHttpRequest.content().writerIndex());
                    }
                    closeAndRetry(retryHttpRequest, channelPool, maslaContext, future.channel(),future.cause());

                }else {
                    if(checkNettyQueueFullException(future.cause())){
                        countQueueFull(maslaContext);
                    }
                    event.setState(EventState.REQUEST_FAILED);
                    asyncExecFailed(maslaContext, channelPool, future.channel(), future.cause(),event);
                }

            }
        }
    }


    // 仅仅在不支持多路复用时候使用
    class TimeoutTask implements Runnable{

        private Channel channel;

        public TimeoutTask(Channel channel){
            this.channel = channel;
        }

        @Override
        public void run() {
            try {

                MaslaDecode maslaDecode = MaslaHttpDecode.getInstance();
                maslaDecode.readTimeout(channel);

            }catch (Throwable e){
                LOG.error("Masla do read timeout task failed:",e);
            }
        }
    }

}
