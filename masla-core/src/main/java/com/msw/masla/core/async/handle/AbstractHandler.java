package com.msw.masla.core.async.handle;

import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
public abstract class AbstractHandler<T> implements EventHandler<ChannelContext, BaseEvent<T>,T>{


    @Override
    public void handle(ChannelContext requestContext, BaseEvent event) throws Throwable {
        try{
            initHeader(requestContext,event);
            doHandle(requestContext,event);
        }catch (Throwable e){
            LOG.error("Masla init header request {} repsonse failed {}",requestContext.getRequestUrl(),e.getMessage());
            throw e;
        }

    }

    abstract protected void initHeader(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<T> event);




    protected abstract void doHandle(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<T> event) throws Throwable;
}
