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
package com.msw.masla.core.push.processor;

import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by Gavin.peng on 2023/6/16.
 */
public abstract class AbstractHeaderResponseProcessor extends BaseResponseProcessor<HttpResponse> {

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractHeaderResponseProcessor.class);


    @Override
    protected void processHeader(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<HttpResponse> event) throws Throwable {
        this.processResponseHeader(requestContext,event);
    }

    @Override
    protected void processBody(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<HttpResponse> event, ByteArrayOutputStream os) throws Throwable {

    }


    protected abstract void processResponseHeader(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<HttpResponse> event) throws Throwable;

}
