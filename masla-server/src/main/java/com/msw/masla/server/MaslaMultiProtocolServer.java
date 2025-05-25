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
package com.msw.masla.server;

import com.msw.masla.common.config.MaslaServerConfig;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.protocol.http.netty.codec.MaslaHttpObjectAggregator;
import com.msw.masla.protocol.http.netty.config.ChannelConfig;
import com.msw.masla.protocol.http.netty.config.ChannelConfigValue;
import com.msw.masla.protocol.http.netty.config.CommonChannelConfigKeys;
import com.msw.masla.protocol.http.netty.http.handler.MaslaReadIdleStateHandler;
import com.msw.masla.protocol.http.netty.exception.MaslaException;
import com.msw.masla.protocol.http.netty.factory.MaslaEventLoopGroupFactory;
import com.msw.masla.core.utils.MaslaHttpUtil;
import com.msw.masla.protocol.http.netty.util.BufferUtils;
import com.msw.masla.server.handler.MaslaServerChannelHandler;
import com.msw.masla.protocol.http.netty.http.handler.MaslaServerHttpRequestDecode;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.protocol.http.netty.ssl.BaseSslContextFactory;
import com.msw.masla.protocol.http.netty.ssl.ServerSslConfig;
import com.msw.masla.server.ssl.MaslaChannelInitialier;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gavin.peng on 2023/9/25.
 */
public class MaslaMultiProtocolServer extends AbstractEndpoint {

    private static final String SSL_SERVER_CERT_FILE = "server.cer";
    private static final String SSL_SERVER_KEY_FILE = "server.key";

    private ChannelGroup channels;
    private AsyncTimeout asyncTimeout = null;


    private static final String[] WWW_PROTOCOLS = new String[]{"TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1"};


    private static class MaslaNIOServerHolder{
        static final MaslaMultiProtocolServer instance = new MaslaMultiProtocolServer();
    }

    public static MaslaMultiProtocolServer getInstance(){
        return MaslaNIOServerHolder.instance;
    }





    @Override
    public void bind() throws Exception {
        MaslaServerConfig serverConfig = MaslaSpringContextUtil.getMaslaServerConfigBean();
        if (serverConfig.isSupportHttps()) {
            initSslProtocol();
        }
        initHttp1Protocol();
    }

    private void initSslProtocol(){
        ChannelGroup clientChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        ChannelConfig channelConfig = defaultChannelConfig(this);
        addHttp2DefaultConfig(channelConfig);
        addSslConfig(channelConfig);

        ChannelInitializer channelInitializer = new MaslaChannelInitialier(channelConfig,clientChannels);

        bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.ALLOCATOR, BufferUtils.SERVER_POOL_ALLOCATOR)
                .option(ChannelOption.SO_BACKLOG, this.backLog).option(ChannelOption.SO_REUSEADDR,true);
        bootstrap.group(acceptorGroup, ioGroup).channel(serverSocketChannelClass)
                .childHandler(channelInitializer)
                .childOption(ChannelOption.ALLOCATOR, BufferUtils.SERVER_POOL_ALLOCATOR)
                .childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024));

        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.bind(new InetSocketAddress(this.sslPort)).sync();
        } catch (InterruptedException e) {
            throw new MaslaException("Open Masla server failed:"+e.getMessage(),e);
        }
        if (!channelFuture.isSuccess()) {
            throw new MaslaException("bind server with port:" + this.sslPort + " failed.",channelFuture.cause());
        }

        LOG.warn("Masla server start complete,http2 protocol listener to {}..........",this.sslPort);
    }

    private void initHttp1Protocol(){
        final ChannelHandler serverHandler = MaslaServerChannelHandler.getInstance(this,this.getMaslaDispatch());

        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline channelPipeline = ch.pipeline();
                channelPipeline.addLast("idleStateHandler", new MaslaReadIdleStateHandler(MaslaMultiProtocolServer.this.sessionIdleTime, 0, 0, TimeUnit.MILLISECONDS));
                //增加统计请求带宽的大小
                channelPipeline.addLast("httpDecode",new MaslaServerHttpRequestDecode(4096,65536,8192));
//                channelPipeline.addLast("httpDecode",new HttpRequestDecoder());
                channelPipeline.addLast("httpEncode",new HttpResponseEncoder());
                //channelPipeline.addLast("httpEncode",new HttpResponseEncoder());
                channelPipeline.addLast("aggregator", new MaslaHttpObjectAggregator(MaslaMultiProtocolServer.this.maxContentLength));
                channelPipeline.addLast(MaslaServerChannelHandler.MASLA_NETTY_SERVER_HANDLE,serverHandler);
            }
        };

        bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.ALLOCATOR, BufferUtils.SERVER_POOL_ALLOCATOR)
                .option(ChannelOption.SO_BACKLOG, this.backLog).option(ChannelOption.SO_REUSEADDR,true);
        bootstrap.group(acceptorGroup, ioGroup).channel(serverSocketChannelClass).
                childHandler(channelInitializer)
                .childOption(ChannelOption.ALLOCATOR, BufferUtils.SERVER_POOL_ALLOCATOR)
                .childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024));
//                .childOption(ChannelOption., new WriteBufferWaterMark(8 * 1024, 32 * 1024));

        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.bind(new InetSocketAddress(this.port)).sync();
        } catch (InterruptedException e) {
            throw new MaslaException("Open masla server failed:"+e.getMessage());
        }
        if (!channelFuture.isSuccess()) {
            throw new MaslaException("bind server with port:" + this.port + " failed.");
        }

        LOG.warn("Masla server was ready listener to {}..........",this.port);

    }

    @Override
    public void unbind() throws Exception {
        //this.bootstrap.
        this.bindState = BindState.UNBOUND;
    }

    @Override
    public void startInternal() throws Exception {

        if(getExecutor() == null){
            this.createExecutor();
        }

        //startAsyncTimeout();
    }

    protected AsyncTimeout getAsyncTimeout(){
        return this.asyncTimeout;
    }

    private void startAsyncTimeout(){
        this.asyncTimeout = new AsyncTimeout();
        Thread timeoutThread = new Thread(asyncTimeout, "Masla-AsyncTimeout-Thread");
        timeoutThread.setPriority(Thread.NORM_PRIORITY);
        timeoutThread.setDaemon(true);
        timeoutThread.start();
    }

    @Override
    public void stopInternal() throws Exception {
        LOG.warn("Masla start to exit..............");
        this.unbind();
        shutdownEventLoopGroup();
        if(getExecutor() != null){
           this.shutdownExecutor();
        }

        MaslaServerChannelHandler maslaServerHandler = MaslaServerChannelHandler.getInstance(this,this.getMaslaDispatch());
        maslaServerHandler.close();


        MaslaEventLoopGroupFactory.getInstance().destroy();

    }

    public static ChannelConfig defaultChannelConfig(AbstractEndpoint endpoint)
    {
        ChannelConfig config = new ChannelConfig();

        config.add(new ChannelConfigValue(CommonChannelConfigKeys.maxConnections,200));
        config.add(new ChannelConfigValue(CommonChannelConfigKeys.maxRequestsPerConnection, 20000));
        config.add(new ChannelConfigValue(CommonChannelConfigKeys.maxRequestsPerConnectionInBrownout, CommonChannelConfigKeys.maxRequestsPerConnectionInBrownout.defaultValue()));
        config.add(new ChannelConfigValue(CommonChannelConfigKeys.idleTimeout,60 * 1000));
        config.add(new ChannelConfigValue(CommonChannelConfigKeys.httpRequestReadTimeout, 5000));
        config.add(new ChannelConfigValue(CommonChannelConfigKeys.connCloseDelay, endpoint.getSessionTimeout()));
        config.add(new ChannelConfigValue(CommonChannelConfigKeys.connectionExpiry, 2000));
        // For security, default to NEVER allowing XFF/Proxy headers from client.
        //config.set(CommonChannelConfigKeys.withProxyProtocol, true);
        //config.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
        return config;
    }

    public static void addHttp2DefaultConfig(ChannelConfig config) {
        config.add(new ChannelConfigValue(CommonChannelConfigKeys.maxConcurrentStreams, CommonChannelConfigKeys.maxConcurrentStreams.defaultValue()));
        config.add(new ChannelConfigValue(CommonChannelConfigKeys.initialWindowSize, CommonChannelConfigKeys.initialWindowSize.defaultValue()));
        config.add(new ChannelConfigValue(CommonChannelConfigKeys.maxHttp2HeaderTableSize, 65536));
        config.add(new ChannelConfigValue(CommonChannelConfigKeys.maxHttp2HeaderListSize, 32768));

        // Override this to a lower value, as we'll be using ELB TCP listeners for h2, and therefore the connection
        // is direct from each device rather than shared in an ELB pool.
        config.add(new ChannelConfigValue(CommonChannelConfigKeys.maxRequestsPerConnection, 4000));
    }


    public static void addSslConfig(ChannelConfig channelConfig){
        ServerSslConfig sslConfig = ServerSslConfig.withDefaultCiphers(
                loadFromResources(SSL_SERVER_CERT_FILE),
                loadFromResources(SSL_SERVER_KEY_FILE),
                WWW_PROTOCOLS);

        channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
        channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
        channelConfig.set(CommonChannelConfigKeys.serverSslConfig, sslConfig);
        channelConfig.set(CommonChannelConfigKeys.sslContextFactory, new BaseSslContextFactory(sslConfig));
    }

    private static File loadFromResources(String s) {
        return new File(MaslaMultiProtocolServer.class.getClassLoader().getSystemResource(s).getFile());
    }
    /**
     * Async timeout thread
     */
    protected class AsyncTimeout implements Runnable {

        private volatile boolean asyncTimeoutRunning = true;

        /**
         * The background thread that checks async requests and fires the
         * timeout if there has been no activity.
         */
        @Override
        public void run() {

            // Loop until we receive a shutdown command
            while (asyncTimeoutRunning) {
                try {
                    Thread.sleep(ASYNC_TIME_OUT_INTERVAL);
                } catch (InterruptedException e) {
                    LOG.warn("Masla async timeout thread is interrupted!!!");
                    asyncTimeoutRunning = false;
                    // Ignore
                }
                //executor.
                int timeoutNums = 0;


            }
            LOG.warn("Masla AsyncTimeout thread exit!!!");

        }

        protected void stop() {
            asyncTimeoutRunning = false;
        }
    }


    private void processTimeout(IOSession session){
        ReferenceCountUtil.release(session.getHttpRequest());
        session.setError();
        session.writeError(MaslaHttpUtil.TIMEOUT_REQUESTS,true);
    }
}
