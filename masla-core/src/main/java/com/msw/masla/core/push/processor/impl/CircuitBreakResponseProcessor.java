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

import com.msw.masla.common.circuit.CircuitFactory;
import com.msw.masla.common.circuit.MaslaCircuitBreaker;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.common.util.StringBuilderHolder;
import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.core.discovery.nacos.MaslaNacosServiceDiscovery;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;


/**
 * Created by Gavin.peng on 2023/9/6.
 */
public class CircuitBreakResponseProcessor extends MaslaBodyResponseProcessor<FullHttpResponse> {

    private static final String PROCESSOR_NAME = "CircuitBreakProcessor";


    @Override
    public void processNettyResponseBody(SessionContext requestContext, BaseEvent<FullHttpResponse> event, ByteBuf content) throws Throwable {

        circuitBreaker(requestContext,event.getResult(),event.getErrorCause());

    }

    @Override
    public String getProcessorName() {
        return PROCESSOR_NAME;
    }




    private void circuitBreaker(SessionContext requestContext, FullHttpResponse httpResponse, Throwable cause){

        ServiceApp serviceApp = requestContext.getService();
        StringBuilder stringBuilder = StringBuilderHolder.getGlobal();
        stringBuilder.append(serviceApp.getContextRoot())
                .append(requestContext.getRequestUrl());

        MaslaCircuitBreaker circuitBreaker = CircuitFactory.getCircuitBreaker(serviceApp);

        if (circuitBreaker != null) {
            if (circuitBreaker.isOpen()) {
                if(LOG.isWarnEnabled()){
                    LOG.warn("Masla found app {} is absolute circuiting and detect request {} is callback",serviceApp.getName(), requestContext.getRequestUrl());
                }
                if (httpResponse != null) {
                    if (circuitBreaker.markSuccess()) {
                        if(circuitBreaker.supportUpgradOrDown()) {
                            circuitBreaker.fastRecovery();
                        }else{
                            circuitBreaker.closeCircuit();
                        }
                    }
                } else {
                    circuitBreaker.markNonSuccess();
                }
            } else {
                int httpStatus = -1;
                if (httpResponse != null && httpResponse.status() != null) {
                    httpStatus = httpResponse.status().code();
                }
                MaslaDefaultProxyInvokerFactory defaultProxyInvokerFactory = (MaslaDefaultProxyInvokerFactory)MaslaSpringContextUtil.getBean("proxyInvokerFactory");
                MaslaNacosServiceDiscovery nacosServiceDiscovery = defaultProxyInvokerFactory.getNacosServiceDiscovery();
                int serviceClusterSize = nacosServiceDiscovery.getServiceHostSize(serviceApp.getName());
                circuitBreaker.doUpgradOrDown(cause, serviceApp.getType(), serviceApp.getName(), httpStatus, serviceClusterSize);
            }
        }

    }

}
