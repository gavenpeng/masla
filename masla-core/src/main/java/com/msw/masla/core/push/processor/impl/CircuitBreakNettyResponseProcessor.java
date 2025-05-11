package com.msw.masla.core.push.processor.impl;

import com.msw.masla.common.circuit.CircuitFactory;
import com.msw.masla.common.circuit.MaslaCircuitBreaker;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.util.StringBuilderHolder;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;


/**
 * Created by Gavin.peng on 2023/9/6.
 */
public class CircuitBreakNettyResponseProcessor extends MaslaBodyResponseProcessor<FullHttpResponse> {

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

        //ServiceApi execApi = requestContext.getExecApi();
        ServiceApp appDO = requestContext.getService();
        StringBuilder stringBuilder = StringBuilderHolder.getGlobal();
        stringBuilder.append(appDO.getContextRoot())
                .append(requestContext.getRequestUrl());


        MaslaCircuitBreaker circuitBreaker = CircuitFactory.getCircuitBreaker(appDO);

        if (circuitBreaker != null) {
            if (circuitBreaker.isOpen()) {
                if(LOG.isWarnEnabled()){
                    LOG.warn("Masla found app {} is absolute circuiting and detect request {} is callback",appDO.getName(),requestContext.getRequestUrl());
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
                circuitBreaker.doUpgradOrDown(cause, appDO.getType(), appDO.getName(), httpStatus);
            }
        }

    }

}
