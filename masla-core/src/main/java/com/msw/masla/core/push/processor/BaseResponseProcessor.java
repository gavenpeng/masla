package com.msw.masla.core.push.processor;

import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Created by Gavin.peng on 2017/6/6.
 */
public abstract class BaseResponseProcessor<T> implements ResponseProcessor<ChannelContext<IOSession, HttpRequest, HttpResponse>, BaseEvent<HttpResponse>> {


    @Override
    public void process(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<HttpResponse> event, OutputStream os) throws Throwable{
        try{
            processHeader(requestContext,event);
            processBody(requestContext, event,(ByteArrayOutputStream)os);
        }catch (Throwable e){
            LOG.error("Masla process request {} response failed {}",requestContext.getRequestUrl(),e);
            throw e;
        }
    }



    protected abstract void processHeader(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<HttpResponse> event) throws Throwable;


    protected abstract void processBody(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<HttpResponse> event, ByteArrayOutputStream os) throws Throwable;

}
