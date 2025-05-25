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
package com.msw.masla.filter.servlet;

import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.core.invoker.ProxyInvoker;
import com.msw.masla.core.utils.MaslaHttpUtil;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.msw.masla.common.constant.Constants.MASLA_COMMON_MATCH_PATH;

@Slf4j
public class MaslaDefaultServlet implements MaslaServlet {

    private MaslaDefaultProxyInvokerFactory proxyInvokerFactory;

    @Override
    public String mappingPath() {
        return MASLA_COMMON_MATCH_PATH;
    }

    @Override
    public void init(MaslaDefaultProxyInvokerFactory proxyInvokerFactory) {
        this.proxyInvokerFactory = proxyInvokerFactory;
    }

    @Override
    public void service(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent event) throws IOException {

        ProxyInvoker proxyInvoker = this.proxyInvokerFactory.getAsyncProxyInvoker();
        try {
            proxyInvoker.invoke(requestContext);
        } catch (Throwable e) {
            log.error("Masla servlet process request url {} exception:", requestContext.getRequestUrl(), e);
            HttpResponse httpResponse = MaslaHttpUtil.createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Masla gateway process request url execute failed:" + e.getMessage());
            requestContext.getSession().writeAndFlush(httpResponse);
            requestContext.getEvent().recycle();
            requestContext.recycle();
        }
    }

    @Override
    public void destory() {

    }
}
