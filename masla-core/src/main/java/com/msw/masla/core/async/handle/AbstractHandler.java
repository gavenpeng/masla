package com.msw.masla.core.async.handle;

import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
public abstract class AbstractHandler<T> implements EventHandler<SessionContext, BaseEvent<T>,T>{


    @Override
    public void handle(SessionContext requestContext, BaseEvent event) throws Throwable {
        try{
            initHeader(requestContext,event);
            doHandle(requestContext,event);
        }catch (Throwable e){
            LOG.error("Masla init header request {} repsonse failed {}",requestContext.getRequestUrl(),e.getMessage());
            throw e;
        }

    }

    abstract protected void initHeader(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<T> event);




    protected abstract void doHandle(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<T> event) throws Throwable;
}
