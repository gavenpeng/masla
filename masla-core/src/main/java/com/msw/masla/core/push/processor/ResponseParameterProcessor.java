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
