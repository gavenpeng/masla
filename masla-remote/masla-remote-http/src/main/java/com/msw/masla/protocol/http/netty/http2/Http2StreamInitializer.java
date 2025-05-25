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
package com.msw.masla.protocol.http.netty.http2
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

;


import com.msw.masla.protocol.http.netty.common.Http2ConnectionCloseHandler;
import com.msw.masla.protocol.http.netty.ssl.BaseMaslaChannelInitializer;
import com.msw.masla.protocol.http.netty.ssl.AbstractHttp2SslChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2StreamFrameToHttpObjectCodec;
import io.netty.util.AttributeKey;

import static com.msw.masla.protocol.http.netty.http2.Http2OrHttpHandler.PROTOCOL_NAME;


/**
 * TODO - can this be done when we create the Http2StreamChannelBootstrap instead now?
 */
@ChannelHandler.Sharable
public class Http2StreamInitializer extends ChannelInboundHandlerAdapter
{
    private static final Http2StreamHeaderCleaner http2StreamHeaderCleaner = new Http2StreamHeaderCleaner();
    private static final Http2ResetFrameHandler http2ResetFrameHandler = new Http2ResetFrameHandler();
    private static final Http2StreamErrorHandler http2StreamErrorHandler = new Http2StreamErrorHandler();

    private Channel parent;
    private final Http2ConnectionCloseHandler connectionCloseHandler;
    private BaseMaslaChannelInitializer channelInitializer;


    public Http2StreamInitializer(Channel channel, AbstractHttp2SslChannelInitializer channelInitializer, Http2ConnectionCloseHandler connectionCloseHandler)
    {
        this.parent = channel;
        this.channelInitializer = channelInitializer;
        this.connectionCloseHandler = connectionCloseHandler;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        copyAttrFromParentChannel(this.parent,ctx.channel(),PROTOCOL_NAME);
        addHttp2StreamSpecificHandlers(ctx.pipeline());
        channelInitializer.addMaslaHttpHandlers(ctx.pipeline());
        ctx.pipeline().remove(this);
    }

    protected void addHttp2StreamSpecificHandlers(ChannelPipeline pipeline)
    {
        pipeline.addLast("h2_conn_close", connectionCloseHandler);
        pipeline.addLast(http2ResetFrameHandler);
        pipeline.addLast("h2_downgrader", new Http2StreamFrameToHttpObjectCodec(true));
        pipeline.addLast(http2StreamErrorHandler);
        pipeline.addLast(http2StreamHeaderCleaner);
    }



    protected void copyAttrFromParentChannel(Channel parent, Channel child, AttributeKey key)
    {
        child.attr(key).set(parent.attr(key).get());
    }
}
