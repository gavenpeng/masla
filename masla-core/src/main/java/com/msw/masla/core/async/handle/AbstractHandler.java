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
