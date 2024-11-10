/*
 * Copyright 2018 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.msw.masla.protocol.http.netty.ssl;


import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.monitor.metrics.OpenSslStatsCount;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.protocol.http.netty.common.Http2ConnectionCloseHandler;
import com.msw.masla.protocol.http.netty.config.ChannelConfig;
import com.msw.masla.protocol.http.netty.config.CommonChannelConfigKeys;
import com.msw.masla.protocol.http.netty.http2.DummyChannelHandler;
import com.msw.masla.protocol.http.netty.http2.Http2Configuration;
import com.msw.masla.protocol.http.netty.http2.Http2OrHttpHandler;
import com.msw.masla.protocol.http.netty.http2.Http2StreamInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.ssl.OpenSslSessionStats;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToLongFunction;

/**
 *
 */
public abstract class AbstractHttp2SslChannelInitializer extends BaseMaslaChannelInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractHttp2SslChannelInitializer.class);
    public static final DummyChannelHandler DUMMY_HANDLER = new DummyChannelHandler();

    private final ServerSslConfig serverSslConfig;

    private final SslContext sslContext;

    private final boolean isSSlFromIntermediary;

    private final Http2ConnectionCloseHandler connectionCloseHandler;

    //h2的session统计
    private static AtomicInteger sslH2Sessions = new AtomicInteger(0);

    //SSL 协商结果为http1的统计
    private static AtomicInteger sslH1Sessions = new AtomicInteger(0);

    //监控使用
    private static int maxH1SessionCount;

    private static int maxH2SessionCount;

    private static OpenSslSessionStats stats;


    public AbstractHttp2SslChannelInitializer(ChannelConfig channelConfig,
                                              ChannelGroup channels) {
        super(channelConfig, channels);
        this.connectionCloseHandler = new Http2ConnectionCloseHandler(channelConfig.get(CommonChannelConfigKeys.connCloseDelay));
        this.serverSslConfig = channelConfig.get(CommonChannelConfigKeys.serverSslConfig);
        this.isSSlFromIntermediary = channelConfig.get(CommonChannelConfigKeys.isSSlFromIntermediary);
        SslContextFactory sslContextFactory = channelConfig.get(CommonChannelConfigKeys.sslContextFactory);
        sslContext = Http2Configuration.configureSSL(sslContextFactory, port);
        stats = ((ReferenceCountedOpenSslContext) sslContext).sessionContext().stats();
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        SslHandler sslHandler = sslContext.newHandler(ch.alloc());
        sslHandler.engine().setEnabledProtocols(serverSslConfig.getProtocols());

        if (LOG.isDebugEnabled()) {
            LOG.debug("ssl protocols supported: {}", String.join(", ", sslHandler.engine().getSupportedProtocols()));
            LOG.debug("ssl protocols enabled: {}", String.join(", ", sslHandler.engine().getEnabledProtocols()));

            LOG.debug("ssl ciphers supported: {}", String.join(", ", sslHandler.engine().getSupportedCipherSuites()));
            LOG.debug("ssl ciphers enabled: {}", String.join(", ", sslHandler.engine().getEnabledCipherSuites()));
        }

        // Configure our pipeline of ChannelHandlerS.
        ChannelPipeline pipeline = ch.pipeline();
//        storeChannel(ch);
        addTimeoutHandlers(pipeline,this);
//        addTcpRelatedHandlers(pipeline);
        pipeline.addLast("ssl", sslHandler);
//        addSslInfoHandlers(pipeline, isSSlFromIntermediary);
        pipeline.addLast("http2CodecSwapper", new Http2OrHttpHandler(
                new Http2StreamInitializer(ch,this, connectionCloseHandler),
                channelConfig,
                this));
        pipeline.addLast("codec_placeholder", DUMMY_HANDLER);
    }



    protected void http1Handlers(ChannelPipeline pipeline) {
        //addHttpRelatedHandlers(pipeline);
        addHttp1Handlers(pipeline);
        this.sslSessionMark(pipeline);
    }



    public void sslSessionMark(ChannelPipeline pipeline) {
        //addHttpRelatedHandlers(pipeline);
        boolean alert = false;
        String msg = null;
        if (isSslH2(pipeline)) {
            if (sslH2Sessions.incrementAndGet() > MaslaSpringContextUtil.getMaslaConfConfigBean().getServerH2MaxSessions()) {
                alert = true;
                msg = "]发现SSL-H2超大连接["+sslH2Sessions.get();
            }
            if(sslH2Sessions.intValue() > maxH2SessionCount){
                maxH2SessionCount = sslH2Sessions.intValue();
            }
        } else {
            if (sslH1Sessions.incrementAndGet() > MaslaSpringContextUtil.getMaslaConfConfigBean().getServerH2MaxSessions()) {
                alert = true;
                msg = "]发现SSL-H1超大连接["+sslH1Sessions.get();
            }
            if(sslH1Sessions.intValue() > maxH1SessionCount){
                maxH1SessionCount = sslH1Sessions.intValue();
            }
        }

    }

    private boolean isSslH2(ChannelPipeline pipeline){
        if (Constants.HTTP2_PROTOCOL.equals(pipeline.channel().attr(Http2OrHttpHandler.PROTOCOL_NAME).get())) {
            return true;
        }
        return false;
    }

    public void decrementSession(ChannelHandlerContext ctx){
        if(isSslH2(ctx.pipeline())) {
            sslH2Sessions.decrementAndGet();
        }else{
            sslH1Sessions.decrementAndGet();
        }
    }

    public static int getH2Sessions(){
        return maxH2SessionCount;
    }

    public static int getH1Sessions(){
        return maxH1SessionCount;
    }

    public static void cleanSessions(){
        maxH2SessionCount = 0;
        maxH1SessionCount = 0;
    }

    public static OpenSslStatsCount getOpenSslStats(){
        ToLongFunction<OpenSslSessionStats> data = OpenSslSessionStats::accept;
        OpenSslStatsCount.getInstances().getAccept().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::acceptGood;
        OpenSslStatsCount.getInstances().getAccept_good().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::acceptRenegotiate;
        OpenSslStatsCount.getInstances().getAccept_renegotiate().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::number;
        OpenSslStatsCount.getInstances().getNumber().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::connect;
        OpenSslStatsCount.getInstances().getConnect().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::connectGood;
        OpenSslStatsCount.getInstances().getConnect_good().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::connectRenegotiate;
        OpenSslStatsCount.getInstances().getConnect_renegotiate().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::hits;
        OpenSslStatsCount.getInstances().getHits().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::cbHits;
        OpenSslStatsCount.getInstances().getCb_hits().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::misses;
        OpenSslStatsCount.getInstances().getMisses().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::timeouts;
        OpenSslStatsCount.getInstances().getTimeouts().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::cacheFull;
        OpenSslStatsCount.getInstances().getCache_full().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::ticketKeyFail;
        OpenSslStatsCount.getInstances().getTicket_key_fail().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::ticketKeyNew;
        OpenSslStatsCount.getInstances().getTicket_key_new().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::ticketKeyRenew;
        OpenSslStatsCount.getInstances().getTicket_key_renew().set(data.applyAsLong(stats));
        data = OpenSslSessionStats::ticketKeyResume;
        OpenSslStatsCount.getInstances().getTicket_key_resume().set(data.applyAsLong(stats));
        return  OpenSslStatsCount.getInstances();
    }
}

