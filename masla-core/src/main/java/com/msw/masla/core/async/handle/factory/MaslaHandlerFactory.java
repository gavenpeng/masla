package com.msw.masla.core.async.handle.factory;

import com.msw.masla.protocol.http.netty.event.IEvent;
import com.msw.masla.core.async.handle.EventHandler;
import com.msw.masla.core.async.handle.MaslaCommonResponseHandler;
import com.msw.masla.core.async.event.AsyncNettyEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gavin.peng on 2023/9/5.
 */
public class MaslaHandlerFactory implements HandlerFactory {

    public Map<String, EventHandler> handlerMap = new HashMap<String, EventHandler>();


    public MaslaHandlerFactory(){
        handlerMap.put(AsyncNettyEvent.class.getName(), MaslaCommonResponseHandler.getInstance());
    }

    static class MaslaHandlerFactoryHolder{
        static final MaslaHandlerFactory instance = new MaslaHandlerFactory();
    }

    public static MaslaHandlerFactory getInstance(){
        return MaslaHandlerFactoryHolder.instance;
    }

    @Override
    public EventHandler create(IEvent event) {
        EventHandler handler = null;
        String hName = event.getClass().getName();
        if (handlerMap.size() > 0 && handlerMap.containsKey(hName)) {
            handler = handlerMap.get(hName);
        }
        if(handler == null){
            throw new IllegalArgumentException("Masla not support event "+hName);
        }
        return handler;
    }

    @Override
    public void addHandler(Class eventClasses , EventHandler handler) {
        this.handlerMap.put(eventClasses.getName(),handler);
    }
}
