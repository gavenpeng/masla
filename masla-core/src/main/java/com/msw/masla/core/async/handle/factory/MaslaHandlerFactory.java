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
