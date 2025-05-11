package com.msw.masla.core.push.processor.listener;

import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * Created by Gavin.peng on 2018/2/7.
 */
public interface MaslaResponseBodyListener {

    public void listenerBody(byte[] body, SessionContext requestContext, BaseEvent<FullHttpResponse> event) throws Throwable;
}
