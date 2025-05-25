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

import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.pojo.ApiMetric;
import com.msw.masla.common.pojo.IOTDevice;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.monitor.metrics.BandwidthCount;
import com.msw.masla.common.util.IpUtil;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.core.push.processor.AbstractHeaderResponseProcessor;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.metrics.http.DomainMetrics;
import com.msw.masla.protocol.http.netty.exception.ServerClosedChannelException;
import com.msw.masla.core.utils.MaslaHttpUtil;


import com.msw.masla.common.enums.SessionType;

import io.netty.handler.codec.http.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Gavin.peng on 2024/5/10.
 */
public class ApiMetricResponseProcessor extends AbstractHeaderResponseProcessor {


    private static final String PROCESSOR_NAME = "ApiMetricProcessor";


    @Override
    public void processResponseHeader(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<HttpResponse> event) throws Throwable {
        try{
            long now = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());


                String serviceId = requestContext.getServiceIdentify();
                ServiceApp appDO = requestContext.getService();
                long totalCost = now - event.getStart();
                long serverCostTime = -1L;
                long accquireTime = event.getStartSendTime() - event.getStartAcquireConnTime();
                long pushTime = 0;
                if(event.getResponseCompleteTime() >0){
                    pushTime = now - event.getResponseCompleteTime();
                }
                ApiMetric apiMetric = appDO.getApiMetricByServiceIdAndHost(serviceId, requestContext.getRouteHost());
                if(apiMetric == null){
                    if(LOG.isDebugEnabled()){
                        LOG.debug("Masla found request {} not found api metric,so not record metric",requestContext.getRequestUrl());
                    }
                    return;
                }
                //记录响应吗以及异常Metric
                int contentLength = 0;
                FullHttpResponse httpResponse = null;
                if (event.getResult() instanceof FullHttpResponse) {
                    httpResponse = (FullHttpResponse) event.getResult();
                }
                if (httpResponse != null) {
                    contentLength = httpResponse.content() == null?0:httpResponse.content().writerIndex();
                    if (httpResponse.status().code() < HttpResponseStatus.BAD_REQUEST.code()) {
                        apiMetric.getSuccessNums().incrementAndGet();
                    }
                    else if (httpResponse.status().code() >= HttpResponseStatus.BAD_REQUEST.code()
                            && httpResponse.status().code() < HttpResponseStatus.INTERNAL_SERVER_ERROR.code()) {
                        if(httpResponse.status().code() == HttpResponseStatus.BAD_REQUEST.code()){
                            apiMetric.getCode400().incrementAndGet();
                        }else if(httpResponse.status().code() == HttpResponseStatus.UNAUTHORIZED.code()){
                            apiMetric.getCode401().incrementAndGet();
                        }else if(httpResponse.status().code() == HttpResponseStatus.NOT_FOUND.code()){
                            apiMetric.getCode404().incrementAndGet();
                        }else{
                            apiMetric.getFourXXCodeNums().incrementAndGet();
                        }
                    } else if(httpResponse.status().code() >= HttpResponseStatus.INTERNAL_SERVER_ERROR.code() && httpResponse.status().code() < Constants.HTTP_CODE_APP_DEFINE){
                        //500 业务参数不合法时，都响应500，不记录
                        apiMetric.getFiveXXCodeNums().incrementAndGet();
                    }else{
                        apiMetric.getCodeAppDefine().incrementAndGet();
                    }
                    apiMetric.setOutBandWidth(contentLength);
                } else {
                    Throwable cause = event.getErrorCause();
                    if(cause != null) {
                        String errMsg = cause.getMessage();
                        if (errMsg.startsWith(Constants.WAIT_TIMEOUT_EXCEPTION)) {
                            apiMetric.getConnPoolWaitTimeoutNums().incrementAndGet();
                        } else if (cause instanceof TimeoutException) {
                            apiMetric.getTimeoutNums().incrementAndGet();
                        } else if (cause instanceof ServerClosedChannelException) {
                            apiMetric.getConnClosedNums().incrementAndGet();
                        } else {
                            if (errMsg != null) {
                                if (errMsg.contains(Constants.CONN_REFUSED) || errMsg.contains(Constants.CONN_REFUSED_CHINESE)) {
                                    apiMetric.getConnRefusedNums().incrementAndGet();
                                } else if (errMsg.contains(Constants.CONN_RESET)) {
                                    apiMetric.getConnResetNums().incrementAndGet();
                                } else if (errMsg.contains(Constants.TIMED_OUT_EXCEPTION)) {
                                    apiMetric.getConnTimeoutNums().incrementAndGet();
                                } else if (errMsg.startsWith(Constants.ACQUIRE_CONN_QUEUE_FULL_EXCEPTION)) {
                                    apiMetric.getConnPoolFullRejectNums().incrementAndGet();
                                } else {
                                    apiMetric.getExceptionNums().incrementAndGet();
                                }
                            } else {
                                apiMetric.getExceptionNums().incrementAndGet();
                            }
                        }
                    }
                }


                apiMetric.getPushCost().addAndGet(pushTime);
//                long MaslaCostTime = event.getStartAcquireConnTime()-event.getStart();
                //
                if(event.getSendCompleteTime()<=0){
                    event.setSendCompleteTime(event.getStartSendTime(),false);
                }



                if (event.getResponseCompleteTime() > 0 && event.getSendCompleteTime() > 0) {
                    serverCostTime = event.getResponseCompleteTime() - event.getSendCompleteTime();
                    if(serverCostTime <0){
                        //是异步导致，响应已经回来，但回调future在响应后执行，导致。
                        serverCostTime = event.getResponseCompleteTime() - event.getStartSendTime();
                    }
                    if(serverCostTime>0) {
                        apiMetric.getServerCost().addAndGet(serverCostTime);
                        if (serverCostTime > MaslaSpringContextUtil.getMaslaConfConfigBean().getApiSlowResponseTimeInterval()) {
                            apiMetric.getSlowNums().incrementAndGet();
                        }
                        if (serverCostTime > 0) {
                            requestContext.getService().collectResponseTime(requestContext.getRouteHost(), serviceId, serverCostTime);
                        }
                        if(event.getResult() != null && !requestContext.getSession().getChannel().isActive()){
                            long proxyTime = totalCost - accquireTime-serverCostTime;
                            if(LOG.isInfoEnabled()) {
                                LOG.info("Masla found request {} is response but session {} is closed  proxy cost {} acquire connection cost {} server cost {}", requestContext.getRequestUrl(),requestContext.getSession().getChannel().remoteAddress(), proxyTime, accquireTime, serverCostTime);
                            }
                        }
                    }
                }

                staticsDomainBandwidth(requestContext,appDO);

        } catch (Throwable e){
            LOG.error("Masla process request {} statics mertic data failed:",requestContext.getRequestUrl(),e);
        }
    }


    /**
     * 入站带宽和域名级别的qps统计
     * @param requestContext async  request context
     * @param appDO request service
     */
    private void staticsDomainBandwidth(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, ServiceApp appDO){
        try {

            int totalBandWidthSize = 0;
            int requestLineLength = requestContext.getRequestLineSize();
            int requestHeaderLength = requestContext.getRequestHeaderSize();
            int requestBodyLength = requestContext.getRequestBodySize();


            FullHttpResponse httpResponse = (FullHttpResponse) requestContext.getEvent().getResult();
            String serviceId = requestContext.getServiceIdentify();
            if (serviceId != null && (httpResponse == null||httpResponse.status().code() < HttpResponseStatus.BAD_REQUEST.code())) {
                BandwidthCount bandwidthCount = requestContext.getService().getAppBandwidthCount(serviceId);
                if (bandwidthCount != null) {
                    bandwidthCount.getInLineBWCount().addAndGet(requestLineLength);
                    bandwidthCount.getInHeaderBWCount().addAndGet(requestHeaderLength);
                    bandwidthCount.getInBodyBWCount().addAndGet(requestBodyLength);
                }
            }

            totalBandWidthSize = requestLineLength + requestHeaderLength + requestBodyLength;
            String domain = requestContext.getHttpRequest().headers().get(HttpHeaderNames.HOST);
            InetSocketAddress remoteAddress = (InetSocketAddress) requestContext.getSession().getChannel().remoteAddress();
            String clientIp = remoteAddress.getAddress().getHostAddress();
            String dc = "local";

            Map<String,String> cookie = requestContext.getCookie();
            if(cookie == null) {
                requestContext.fillCookies();
            }

            boolean isRetry = false;

            int sessionType = SessionType.HTTP.ordinal();
            Object serverProtocol = requestContext.getHeaders().get(Constants.HTTPS_PROTOCOL_HEADER_KEY);
            if(serverProtocol != null && Constants.HTTPS1_PROTOCOL_HEADER_KEY_VALUE.equals(serverProtocol.toString())){
                sessionType = SessionType.HTTPS1.ordinal();
            }else if(serverProtocol != null && Constants.HTTPS2_PROTOCOL_HEADER_KEY_VALUE.equals(serverProtocol.toString())){
                sessionType = SessionType.HTTPS2.ordinal();
            }

            String realIp = MaslaHttpUtil.getClientRealIp(requestContext.getHttpRequest(), requestContext.getSession().getChannel());

            Map<String,String> extendHeaderMap = MaslaSpringContextUtil.getMaslaConfConfigBean().getDomainExtendHeaderMap();
            List<String> hitExtendHeaderList = new ArrayList<String>(extendHeaderMap.size());
            for(Map.Entry<String,String> entry:extendHeaderMap.entrySet()){
                String headerKey = entry.getKey();
                if(requestContext.getHeaders().containsKey(headerKey)){
                    hitExtendHeaderList.add(headerKey);
                }else{
                    if(requestContext.getCookie() == null) {
                        requestContext.fillCookies();
                    }
                    if(requestContext.getCookie().containsKey(headerKey)){
                        hitExtendHeaderList.add(headerKey);
                    }
                }
            }

            DomainMetrics
                    .countQueryAndInBandwidth(appDO.getId(),appDO.getName(), domain, clientIp, totalBandWidthSize, 1, IOTDevice.getInstance(),
                            isRetry,sessionType,hitExtendHeaderList,requestContext.getResponseContentLength(), dc, getIpv6Qps(realIp));
            DomainMetrics.countOutBandwidth(appDO.getId(),appDO.getName(), domain, clientIp, requestContext.getResponseContentLength());


        }catch(Throwable e){
            LOG.error("Masla statics bandwidth failed:",requestContext.getRequestUrl(),e);
        }

    }

    private long getIpv6Qps(String realIp) {
        return isIpv6(realIp) ? 1: 0;
    }

    /**
     * 判断是否是ipv6
     */
    private boolean isIpv6(String realIp) {
//        realIp = "A00:a00:100:f261::F15";
        if (StringUtil.isEmptyString(realIp)) {
            return false;
        }
        // 判断是否是ipv6
        return IpUtil.isIPv6Address(realIp);
    }

    @Override
    public String getProcessorName() {
        return PROCESSOR_NAME;
    }

}
