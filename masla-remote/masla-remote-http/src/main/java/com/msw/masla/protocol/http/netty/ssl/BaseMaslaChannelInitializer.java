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
package com.msw.masla.protocol.http.netty.ssl;


import com.msw.masla.protocol.http.netty.config.NettyConfig;
import com.msw.masla.protocol.http.netty.common.SourceAddressChannelHandler;
import com.msw.masla.protocol.http.netty.config.ChannelConfig;
import com.msw.masla.protocol.http.netty.config.CommonChannelConfigKeys;
import com.msw.masla.protocol.http.netty.http.handler.MaslaReadIdleStateHandler;
import com.msw.masla.protocol.http.netty.http2.CloseOnIdleStateHandler;
import com.msw.masla.protocol.http.netty.http2.MaxInboundConnectionsHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.concurrent.TimeUnit;


/**
 * Created by Gavin.peng on 2017/9/25.
 */
public abstract class BaseMaslaChannelInitializer extends ChannelInitializer<Channel>
{
    public static final String HTTP_CODEC_HANDLER_NAME = "codec";

    public static final int MAX_INITIAL_LINE_LENGTH = 16384;
    public static final int MAX_HEADER_SIZE = 32768;
    public static final int MAX_CHUNK_SIZE = 32768;

    protected final int port;
    protected final ChannelConfig channelConfig;
    protected final int idleTimeout;
    protected final int httpRequestReadTimeout;
    protected final int maxRequestsPerConnection;
    protected final int maxRequestsPerConnectionInBrownout;
    protected final int connectionExpiry;
    protected final int maxConnections;


    protected final MaxInboundConnectionsHandler maxConnectionsHandler;

    protected BaseMaslaChannelInitializer(
            ChannelConfig channelConfig,
            ChannelGroup channels)
    {
        this.port = NettyConfig.getInstance().getPort();
        this.channelConfig = channelConfig;
        this.idleTimeout = channelConfig.get(CommonChannelConfigKeys.idleTimeout);
        this.httpRequestReadTimeout = channelConfig.get(CommonChannelConfigKeys.httpRequestReadTimeout);

        this.maxConnections = channelConfig.get(CommonChannelConfigKeys.maxConnections);
        this.maxConnectionsHandler = new MaxInboundConnectionsHandler(maxConnections);
        this.maxRequestsPerConnection = channelConfig.get(CommonChannelConfigKeys.maxRequestsPerConnection);
        this.maxRequestsPerConnectionInBrownout = channelConfig.get(CommonChannelConfigKeys.maxRequestsPerConnectionInBrownout);
        this.connectionExpiry = channelConfig.get(CommonChannelConfigKeys.connectionExpiry);
    }

    protected void storeChannel(Channel ch)
    {
//        this.channels.add(ch);
    }

    protected void addTcpRelatedHandlers(ChannelPipeline pipeline)
    {
        pipeline.addLast("addressHandler",new SourceAddressChannelHandler());
    }


    protected HttpServerCodec createHttpServerCodec()
    {
        return new HttpServerCodec(
                MAX_INITIAL_LINE_LENGTH,
                MAX_HEADER_SIZE,
                MAX_CHUNK_SIZE,
                false
        );
    }
    


    protected void addTimeoutHandlers(ChannelPipeline pipeline, AbstractHttp2SslChannelInitializer sslChannelInitializer) {
        //MaslaReadIdleStateHandler 支持写的时候更新读事件的时间
        //这里先占为，确定http1 还是 h2 后再确定，因为h1和h2的空闲时间可能不一样
        pipeline.addLast("idleHandler",new MaslaReadIdleStateHandler(NettyConfig.getInstance().getSessionIdleTime(), 0, 0, TimeUnit.MILLISECONDS));
        //如果是https/1.1  masla server handler 会监听空闲事件，这里不需要，这里只需要https/2.0 来关闭底层连接
        pipeline.addLast("closeHandler",new CloseOnIdleStateHandler(sslChannelInitializer));
    }


    public abstract void addMaslaHttpHandlers(final ChannelPipeline pipeline);





    public abstract  void addHttp1Handlers(final ChannelPipeline pipeline);


    protected void addSslInfoHandlers(ChannelPipeline pipeline, boolean isSSlFromIntermediary) {
        pipeline.addLast("ssl_info", new SslHandshakeInfoHandler(isSSlFromIntermediary));
    }

}
