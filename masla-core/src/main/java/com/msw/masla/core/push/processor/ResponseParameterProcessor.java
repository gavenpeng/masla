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
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Gavin.peng on 2024/6/5.
 */
@Slf4j
public class ResponseParameterProcessor extends AbstractHeaderResponseProcessor {

    private static final String PROCESSOR_NAME = "ResponseParameterProcessor";

    @Override
    protected void processResponseHeader(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<HttpResponse> event) throws Throwable {
        try{
            Object result = requestContext.getEvent().getResult();
            if (!(result instanceof FullHttpResponse)) {
                return;
            }

//            FullHttpResponse response = (FullHttpResponse) result;
//            appendResponseHeader(requestContext, response);
        }catch (Throwable e){
            LOG.error("Masla process request {} get token failed {}",requestContext.toString(),e);
        }
    }


    @Override
    public String getProcessorName() {
        return PROCESSOR_NAME;
    }



}
