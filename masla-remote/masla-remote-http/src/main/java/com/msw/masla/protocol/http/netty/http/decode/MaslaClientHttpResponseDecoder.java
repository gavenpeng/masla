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
package com.msw.masla.protocol.http.netty.http.decode;

import com.msw.masla.protocol.http.netty.codec.MaslaHttpResponseDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.AttributeKey;

import java.util.List;

/**
 * Created by Gavin.peng on 2017/8/21.
 */
public class MaslaClientHttpResponseDecoder extends MaslaHttpResponseDecoder {

    public static final AttributeKey<Integer> RESPONSE_FRAME_SIZE = AttributeKey.newInstance("responseFrameSize");


    //Decoder 必须和channel 绑定,不能共享
    private Channel channel;


    @Override
    protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        this.channel = ctx.channel();
        super.callDecode(ctx, in, out);
    }



    @Override
    protected boolean isContentAlwaysEmpty(HttpMessage msg) {
        final int statusCode = ((HttpResponse) msg).status().code();
        if (statusCode == 100) {
            // 100-continue response should be excluded from paired comparison.
            return true;
        }

        return super.isContentAlwaysEmpty(msg);
    }

}
