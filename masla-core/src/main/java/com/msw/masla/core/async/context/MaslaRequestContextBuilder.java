package com.msw.masla.core.async.context;

import com.msw.masla.common.constant.Constants;
import com.msw.masla.core.async.event.AsyncNettyEvent;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.pool.SynchronizedStack;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Created by Gavin.peng on 2023/9/9.
 */
public class MaslaRequestContextBuilder {



    /**
     * Cache for masla request context
     */
    private static SynchronizedStack<MaslaAsyncContext> contextCache;

    /**
     * Cache for masla request event
     */
    private static SynchronizedStack<BaseEvent> eventCache;

    static {
        contextCache = new SynchronizedStack<MaslaAsyncContext>(Constants.CACHE_SIZE,Constants.CONTEXT_CACHE_SIZE_LIMIT);
        eventCache = new SynchronizedStack<BaseEvent>(Constants.CACHE_SIZE,Constants.CONTEXT_CACHE_SIZE_LIMIT);
    }

    private MaslaRequestContextBuilder(){
    }




    public static SessionContext buildMaslaDispatchConext(IOSession session, HttpRequest httpRequest, BaseEvent event){
        SessionContext maslaContext = contextCache.pop();
        if(maslaContext == null) {
            maslaContext = new MaslaAsyncContext(session, httpRequest, event);
        }else {
            ((MaslaAsyncContext) maslaContext).reset(session,httpRequest,event);
        }
        return maslaContext;
    }


    public static BaseEvent buildNettyDispatchEvent(){
        BaseEvent event = eventCache.pop();
        if(event == null) {
            event = new AsyncNettyEvent();
        }else {
            event.reset();
        }
        return event;
    }

    public static void recycleContext(SessionContext maslaContext){
        contextCache.push((MaslaAsyncContext) maslaContext);
    }

    public static void recycleEvent(BaseEvent event){
        eventCache.push(event);
    }





}
