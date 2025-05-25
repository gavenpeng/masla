/*
 * Copyright 2025 msw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.msw.masla.protocol.http.netty.http.handler;


import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.common.util.StringBuilderHolder;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.protocol.http.netty.metrics.GlobalRequestFailedCounter;
import com.msw.masla.protocol.http.netty.http2.Http2OrHttpHandler;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.protocol.http.netty.session.MaslaSession;
import com.msw.masla.protocol.http.netty.util.HeaderUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Gavin.peng on 2017/9/26.
 */
@ChannelHandler.Sharable
public abstract class AbstractServerChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractServerChannelHandler.class);

    public ConcurrentMap<String, IOSession> sessions;

    public static final String MASLA_NETTY_SERVER_HANDLE = "httpServerHandler";

    public Executor executor;

    private int maxSession = 50000;

    //http1的session统计
    public AtomicInteger currentSessions;

    //监控使用
    private int maxSessionCount;

    //一个连接可以重用的最大次数
    private int maxKeepAliveRequests = 10240;


    public AbstractServerChannelHandler(ConcurrentMap<String, IOSession> sessions, int maxChannel, int maxKeepAliveRequests){
        this.maxSession = maxChannel;
        this.maxKeepAliveRequests = maxKeepAliveRequests;
        this.sessions = sessions;
        this.currentSessions = new AtomicInteger(0);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if(msg instanceof FullHttpRequest) {

            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            //fullHttpRequest.
            if(LOG.isDebugEnabled()){
                LOG.debug("Masla receive request {} ",fullHttpRequest.uri());
            }

            Channel channel = ctx.channel();
            String sessionKey = getChannelKey((InetSocketAddress) channel.localAddress(),
                    (InetSocketAddress) channel.remoteAddress(),channel);
            IOSession session = sessions.get(sessionKey);
            if (session == null) {
                LOG.warn("Masla found session {} not exist,maybe is close",sessionKey);
                session = new MaslaSession(sessionKey,ctx.channel());
                sessions.put(sessionKey, session);
            }

            if(!initSession(session, fullHttpRequest)){
                refuseRequest(session,fullHttpRequest);
                return;
            }

            if(!Constants.MASLA_META_CONTEXT.equals(session.getContextRoot())
                    && ( Constants.MASLA_NIO_THREAD_MODE.equals(MaslaSpringContextUtil.getMaslaConfConfigBean().getWorkThreadMode())
                       || session.getContextRoot().equals(Constants.MASLA_HEALTHCHECK_PATH_END))){
                syncProcessRequest(session);
                return;
            }
            asyncProcessRequest(session);

        }else{
            LOG.warn("Received unrecognized message type. " + msg.getClass().getName());
            ReferenceCountUtil.release(msg);
        }


    }


    /**
     * 检查网关是否被下线
     * @param session
     * @return
     */
    private boolean gracefulOFFline(IOSession session){
        if (session.getHttpRequest().uri().endsWith(Constants.MASLA_HEALTHCHECK_PATH_END)) {
            if(LOG.isInfoEnabled()) {
                LOG.info("Masla is turn off status so don't receive request {} ", session.getHttpRequest().uri());
            }
            session.close(null);
            return true;
        }
        return false;
    }


    private void refuseRequest(IOSession session, FullHttpRequest fullHttpRequest){
        ReferenceCountUtil.release(fullHttpRequest);
        //HeaderEnableUtils.isDomainLevelSwitch
        session.writeError(HttpResponseStatus.TOO_MANY_REQUESTS,Constants.MASLA_RESPONSE_HEADER_APPLICATION_SESSION_TOO_MANY, true);
    }

    public void discardRequest(IOSession session, FullHttpRequest fullHttpRequest){
        GlobalRequestFailedCounter.getRequestFailedCount().getQueueFull().incrementAndGet();
        ReferenceCountUtil.release(fullHttpRequest);
        session.writeError(HttpResponseStatus.TOO_MANY_REQUESTS,Constants.MASLA_RESPONSE_HEADER_QUEUE_FULL, true);
    }



    /**
     * Netty io thread direct process
     * 这样减少进出队列锁，线程上下文切换的开销。
     * @param session
     */
    public abstract void syncProcessRequest(IOSession session);



    public abstract void asyncProcessRequest(IOSession session);


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if(LOG.isInfoEnabled()) {
            LOG.info("Masla server receive channel {} is connected!", ctx.channel().remoteAddress());
        }
        Channel channel = ctx.channel();

        String channelKey = getChannelKey((InetSocketAddress) channel.localAddress(),
                (InetSocketAddress) channel.remoteAddress(),channel);
        IOSession session = new MaslaSession(channelKey,channel);
        sessions.put(channelKey, session);
        if(currentSessions.intValue() > maxSessionCount){
            maxSessionCount = currentSessions.intValue();
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    /**
     * Calls {@link ChannelHandlerContext#fireChannelInactive()} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     *
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        LOG.info("Masla server receive client close channel {}",ctx.channel().remoteAddress());
        ctx.fireChannelInactive();
    }


    private boolean initSession(IOSession session, FullHttpRequest fullHttpRequest){
        MaslaSession maslaSession = (MaslaSession)session;

        QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());

        String requestPath = HeaderUtils.resloveRequestPath(decoder.path());

        String contextRoot = requestPath;
        String path = Constants.MASLA_ROOT_CONTEXT;
        if(!StringUtil.isEmptyString(requestPath) && requestPath.length()>1) {
            String tmp = requestPath.substring(1);
            if (!StringUtil.isEmptyString(tmp)) {
                int firstOffset = tmp.indexOf(Constants.HTTP_SCHEMA);
                if (firstOffset < 0) {
                    contextRoot = Constants.HTTP_SCHEMA + tmp;
                } else {
                    contextRoot = Constants.HTTP_SCHEMA + tmp.substring(0, firstOffset);
                    path = tmp.substring(firstOffset);
                }
            }
        }
        maslaSession.setContextRoot(contextRoot);
        maslaSession.setPath(path);
        maslaSession.setHttpRequest(fullHttpRequest);
        return true;

    }

    /**
     * local address + remote address 作为连接的唯一标示
     *
     * @param local localAddr
     * @param remote remoteAddr
     * @return ip:port-ip:port
     */
    public String getChannelKey(InetSocketAddress local, InetSocketAddress remote,Channel channel) {
        StringBuilder sessionKey = StringBuilderHolder.getGlobal();
        if (local == null || local.getAddress() == null) {
            sessionKey.append("null-");
        } else {
            sessionKey.append(local.getAddress().getHostAddress()).append(":").append(local.getPort()).append("-");
//            key += local.getAddress().getHostAddress() + ":" + local.getPort() + "-";
        }

        if (remote == null || remote.getAddress() == null) {
            sessionKey.append("null");
//            key += "null";
        } else {
            sessionKey.append(remote.getAddress().getHostAddress()).append(":").append(remote.getPort());
//            key += remote.getAddress().getHostAddress() + ":" + remote.getPort();
        }
        if(channel instanceof Http2StreamChannel){
            Http2StreamChannel http2StreamChannel = (Http2StreamChannel)channel;
            int streamId = http2StreamChannel.stream().id();
            sessionKey.append("-").append(streamId);
        }
        return sessionKey.toString();
    }

    public Map<String, IOSession> getSessions() {
        return sessions;
    }


    public abstract void shutdown();

    /**
     * close 所有连接着的 client 连接
     */
    public void close() {
        for (Map.Entry<String, IOSession> entry : sessions.entrySet()) {
            try {
                IOSession session = entry.getValue();
                if (session != null) {
                    session.close(null);
                }
            } catch (Exception e) {
                LOG.error("NettyServerChannelManageHandler close channel Error: " + entry.getKey(), e);
            }
        }
        LOG.warn("Masla close session complete!!!");

        shutdown();
        sessions.clear();
        clearAllSession();
        LOG.warn("Masla handler thread exit!!!");

    }

    public abstract void clearAllSession();




    public static boolean isSslChannel(Channel channel){
        //https 过来的channel会有protocol name的标记，h2 stream copy到child channel
        String appProtocol = channel.attr(Http2OrHttpHandler.PROTOCOL_NAME).get();
        if (!StringUtil.isEmptyString(appProtocol)) {
            if(LOG.isInfoEnabled()){
                LOG.info("Masla found channel is ssl channel application protocol is {}",appProtocol);
            }
            return true;
        }
        return false;
    }








    public void clearMaxMonitorCount() {
        maxSessionCount = 0;
    }

    public int getMaxSessionCount() {
        return maxSessionCount;
    }





}
