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
package com.msw.masla.protocol.http.netty.context;

import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.enums.RequestDispatchMode;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.AttributeKey;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by Gavin.peng on 2023/6/15.
 */
public interface SessionContext<S , T , R> {

    AttributeKey<SessionContext<IOSession, HttpRequest, HttpResponse>> CONTEXT_KEY = AttributeKey.newInstance("channelContext");

    T getHttpRequest();

     R getHttpResponse();

     void recycle();

     S getSession();

     int getRequestLineSize();

     int getRequestHeaderSize();

     int getRequestBodySize();

     ServiceApp getService();

     void setRequestPath(String requestPath);

     void setRewritePath(String rewritePath);

    void setServiceIdentify(String serviceIdentify);

    String getServiceIdentify();

    BaseEvent<HttpResponse> getEvent();

    //请求path
     String getRequestUrl();

     String getRewritePath();

    String getRouteTag();

    void setRouteHost(String host);

    String getRouteHost();

    void setRouteTag(String routeTag);

    boolean isStressRequest();

    void fillCookies();

     long getTimeout();

     //请求相关的任务，有Timeout task
     void setScheduledFuture(ScheduledFuture<?> scheduledFuture);

     //请求相关的任务，有Timeout task
     ScheduledFuture<?> getScheduledFuture();

    RequestDispatchMode getRequestDispatchMode();

    void setRequestDispatchMode(RequestDispatchMode requestDispatchMode);


    boolean needPush();

    Map<String,String> getParams();

    Map<String, String> getHeaders();

    Map<String, String> getCookie();

   int getResponseContentLength();

   void setResponseContentLength(int responseContentLength);

}
