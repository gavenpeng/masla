package com.msw.masla.core.push.processor.impl;

import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.core.push.processor.AbstractBodyResponseProcessor;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;

/**
 * Created by Gavin.peng on 2017/6/16.
 */
public abstract class MaslaBodyResponseProcessor<T> extends AbstractBodyResponseProcessor {

    @Override
    protected void processNettyBody(SessionContext requestContext, BaseEvent event, ByteBuf content) throws Throwable {
        this.processNettyResponseBody(requestContext,event,content);
    }

    @Override
    protected void processHttpBody(SessionContext requestContext, BaseEvent event, ByteArrayOutputStream os) throws Throwable {

    }

    public abstract void processNettyResponseBody(SessionContext requestContext, BaseEvent<T> event, ByteBuf content )throws Throwable;

}
