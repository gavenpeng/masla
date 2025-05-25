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
