package com.msw.masla.core.push.processor;

import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Created by Gavin.peng on 2017/6/6.
 */
public abstract class BaseResponseProcessor<T> implements ResponseProcessor<SessionContext<IOSession, HttpRequest, HttpResponse>, BaseEvent<HttpResponse>> {


    @Override
    public void process(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<HttpResponse> event, OutputStream os) throws Throwable{
        try{
            processHeader(requestContext,event);
            processBody(requestContext, event,(ByteArrayOutputStream)os);
        }catch (Throwable e){
            LOG.error("Masla process request {} response failed {}",requestContext.getRequestUrl(),e);
            throw e;
        }
    }



    protected abstract void processHeader(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<HttpResponse> event) throws Throwable;


    protected abstract void processBody(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<HttpResponse> event, ByteArrayOutputStream os) throws Throwable;

}
