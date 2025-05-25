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
package com.msw.masla.protocol.http.netty.http2;

import com.msw.masla.protocol.http.netty.ssl.AbstractHttp2SslChannelInitializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just listens for the IdleStateEvent and closes the channel if received.
 */
public class CloseOnIdleStateHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger LOG = LoggerFactory.getLogger(CloseOnIdleStateHandler.class);

    private AbstractHttp2SslChannelInitializer sslChannelInitializer;

    public CloseOnIdleStateHandler(AbstractHttp2SslChannelInitializer sslChannelInitializer){
        this.sslChannelInitializer = sslChannelInitializer;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        if (evt instanceof IdleStateEvent) {
            if(LOG.isInfoEnabled()){
                String protocol = ctx.channel().attr(Http2OrHttpHandler.PROTOCOL_NAME).get();
                LOG.info("Masla gateway found ssl channel {} protocol {} is idle,so do close",ctx.channel().remoteAddress(),protocol);
            }
            ctx.close();
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
    {
        if(LOG.isInfoEnabled()){
            String protocol = ctx.channel().attr(Http2OrHttpHandler.PROTOCOL_NAME).get();
            LOG.info("Masla gateway found ssl channel {} protocol {} is unregistered, do decrement session count",ctx.channel().remoteAddress(),protocol);
        }
        this.sslChannelInitializer.decrementSession(ctx);
        super.channelUnregistered(ctx);
    }
}
