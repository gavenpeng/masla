package com.msw.masla.core.push.processor;

import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by Gavin.peng on 2017/6/16.
 */
public abstract class AbstractHeaderResponseProcessor extends BaseResponseProcessor {

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractHeaderResponseProcessor.class);


    @Override
    protected void processHeader(ChannelContext requestContext, BaseEvent event) throws Throwable {
        this.processResponseHeader(requestContext,event);
    }

    @Override
    protected void processBody(ChannelContext requestContext, BaseEvent event, ByteArrayOutputStream os) throws Throwable {

    }


    protected abstract void processResponseHeader(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent event) throws Throwable;

}
