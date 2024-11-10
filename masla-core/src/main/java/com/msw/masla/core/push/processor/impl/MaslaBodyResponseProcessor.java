package com.msw.masla.core.push.processor.impl;

import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.core.push.processor.AbstractBodyResponseProcessor;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;

/**
 * Created by Gavin.peng on 2017/6/16.
 */
public abstract class MaslaBodyResponseProcessor<T> extends AbstractBodyResponseProcessor {

    @Override
    protected void processNettyBody(ChannelContext requestContext, BaseEvent event, ByteBuf content) throws Throwable {
        this.processNettyResponseBody(requestContext,event,content);
    }

    @Override
    protected void processHttpBody(ChannelContext requestContext, BaseEvent event, ByteArrayOutputStream os) throws Throwable {

    }

    public abstract void processNettyResponseBody(ChannelContext requestContext, BaseEvent<T> event, ByteBuf content )throws Throwable;

}
