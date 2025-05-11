package com.msw.masla.core.push.processor;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import org.apache.http.HttpResponse;

import java.io.ByteArrayOutputStream;

/**
 * Created by Gavin.peng on 2017/6/16.
 */
public abstract class AbstractBodyResponseProcessor extends BaseResponseProcessor{


    @Override
    protected void processHeader(SessionContext requestContext, BaseEvent event) throws Throwable {
    }

    @Override
    protected void processBody(SessionContext requestContext, BaseEvent event, ByteArrayOutputStream os) throws Throwable {
        Object response  = event.getResult();
        if(response instanceof HttpResponse){
            this.processHttpBody(requestContext,event,os);
        }else if(response instanceof FullHttpResponse){
            FullHttpResponse fullHttpResponse = (FullHttpResponse)response;
            this.processNettyBody(requestContext, event, fullHttpResponse.content());
        }else if(response == null){
            //错误也记录
            this.processNettyBody(requestContext, event, null);
        }
    }

    abstract protected void processNettyBody(SessionContext requestContext, BaseEvent event, ByteBuf content) throws Throwable;

    abstract protected void processHttpBody(SessionContext requestContext, BaseEvent event, ByteArrayOutputStream os) throws Throwable;

}
