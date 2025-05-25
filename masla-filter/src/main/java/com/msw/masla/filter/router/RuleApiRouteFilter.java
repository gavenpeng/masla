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
package com.msw.masla.filter.router;

import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.core.ServiceIdFormatUtil;
import com.msw.masla.core.async.context.MaslaAsyncContext;
import com.msw.masla.core.router.rule.RouteRule;
import com.msw.masla.core.router.rule.RouteRuleCache;
import com.msw.masla.core.utils.MaslaHttpUtil;
import com.msw.masla.filter.exception.FilterException;
import com.msw.masla.filter.frame.MaslaFilter;
import com.msw.masla.filter.frame.MaslaFilterChain;
import com.msw.masla.metrics.http.AppRequestFailedMetrics;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static com.msw.masla.common.constant.Constants.MASLA_COMMON_MATCH_PATH;
import static com.msw.masla.common.constant.Constants.MASLA_ROUTE_TAG_HEADER;

/**
 * Author: Gavin.peng
 * Date: 2024/7/28
 * Description:
 * route server base rule of properties
 */
@Slf4j
public class RuleApiRouteFilter implements MaslaFilter {

    private static final long MAX_REQ_LINE_LENGTH = 4096L;

    @Override
    public String mappingPath() {
        return MASLA_COMMON_MATCH_PATH;
    }

    @Override
    public void doFilter(SessionContext<IOSession, HttpRequest, HttpResponse> context, BaseEvent event, MaslaFilterChain filterChain) throws FilterException {

        HttpRequest httpRequest = context.getHttpRequest();

        String host = null;
        Integer port = 80;
        String headerHost = httpRequest.headers().get(HttpHeaderNames.HOST);
        if (headerHost != null) {
            String[] hostPort = headerHost.split(":");
            host = hostPort[0];
            if (hostPort.length == 2) {
                port = Integer.valueOf(hostPort[1]);
            }
        }

        String contextRoot = context.getSession().getContextRoot();
        String path = context.getSession().getPath();
        String serviceName = stripLeadingSlash(contextRoot);

        String requestPath = contextRoot + path;
        //first direct match with path;
        RouteRule rule = RouteRuleCache.getDirectRouteRuleCache().get(requestPath);
        if (rule == null) {
            //second pattern match with path
            Map<String, TreeMap<Pattern, RouteRule>> routeRulePattern =  RouteRuleCache.getAppPatterRouteRule();

            TreeMap<Pattern, RouteRule> appRouteRule = routeRulePattern.get(serviceName);
            if (appRouteRule != null) {
                Set<Map.Entry<Pattern, RouteRule>> rulePatterSet = appRouteRule.entrySet();
                for (Map.Entry<Pattern, RouteRule> entry : rulePatterSet) {
                    if (entry.getKey().matcher(requestPath).find()) {
                        //判断是否是后端无容器的请求
                        rule = entry.getValue();
                        break;
                    }

                }
            }

        }

        //three match with domain
        if (rule == null) {
            rule = RouteRuleCache.getDirectRouteRuleCache().get(host);
        }

        if (rule == null) {
            writeUNValidRouteUrl(context);
            return;
        }

        ServiceApp routeService = RouteRuleCache.getRouteAppCache(rule.getAppName());
        Map<String, String> headers = MaslaHttpUtil.getHeaderMap(httpRequest);
        MaslaAsyncContext maslaAsyncContext = (MaslaAsyncContext) context;
        maslaAsyncContext.setRouteRule(rule);
        maslaAsyncContext.setService(routeService);
        maslaAsyncContext.setTimeout(rule.getTimeout());
        maslaAsyncContext.setHeaders(headers);
        if (StringUtil.isEmptyString(rule.getRewritePath())) {
            maslaAsyncContext.setRewritePath(rule.getRewritePath());
        }

        //config route tag from http header
        String routeTag = httpRequest.headers().get(MASLA_ROUTE_TAG_HEADER);
        if (!StringUtil.isEmptyString(routeTag)) {
            maslaAsyncContext.setRouteTag(routeTag);
        }

        String serviceIdentify = null;

        String reqPath = path.length() > 0 ? path : contextRoot;
        serviceIdentify = ServiceIdFormatUtil.formatServerId(path, context);
        if(ServiceIdFormatUtil.isUNvalidUrl(reqPath)){
            serviceIdentify = Constants.UNVALID_SERVICE_PATH;
        }else if(reqPath.endsWith(Constants.ICO_REQUEST)||
                reqPath.equals(Constants.HTTP_SCHEMA)){
            serviceIdentify =  reqPath;
        }

        maslaAsyncContext.setServiceIdentify(serviceIdentify);

        if (!checkDecodeResult(context,routeService)){
            return;
        }

        try {
            filterChain.doFilter(context, event);
        } catch (FilterException e) {
            log.error("Masla do NettyApiMatchFilter failed:", e);
            throw e;
        }

    }


    private String stripLeadingSlash(String contextRoot) {
        if (contextRoot != null && contextRoot.startsWith("/")) {
            return contextRoot.substring(1);
        }
        return contextRoot;
    }


    @Override
    public void init() {

    }

    @Override
    public void order() {

    }


    private boolean checkDecodeResult(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, ServiceApp appDO){
        try {

            int requestLineLength = requestContext.getRequestLineSize();

            if (requestLineLength > MAX_REQ_LINE_LENGTH){
                try {
                    writeHttpDecodeErrorResponse(requestContext, AppRequestFailedMetrics.REQ_LINE_TOO_LONG);
                }finally {
                    requestContext.recycle();
                }
                return false;
            }else {
                DecoderResult decoderResult = requestContext.getHttpRequest().decoderResult();
                if (decoderResult != null && decoderResult.isFailure()) {
                    Throwable cause = decoderResult.cause();
                    try {
                        String message = cause.getMessage();
                        writeHttpDecodeErrorResponse(requestContext, message);
                    } finally {
                        requestContext.recycle();
                    }
                    return false;
                }
            }

        }catch(Throwable e){
            log.error("Masla check request {} decode result failed:",requestContext.getRequestUrl(),e);
        }
        return true;
    }

    private void writeHttpDecodeErrorResponse(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext,String message) {
        requestContext.getSession().writeAndClose(
                MaslaHttpUtil.createResponse(HttpResponseStatus.BAD_REQUEST, message, Constants.MASLA_RESPONSE_HEADER_PROTOCOL_EXCEPTION));
    }

    private void writeUNValidRouteUrl(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext) {
        String unvalidRoute = "Not found service by request url:" + requestContext.getRequestUrl();
        requestContext.getSession().writeAndClose(
                MaslaHttpUtil.createResponse(HttpResponseStatus.BAD_REQUEST, unvalidRoute, Constants.MASLA_RESPONSE_HEADER_UNVALID_PATH));
    }

}
