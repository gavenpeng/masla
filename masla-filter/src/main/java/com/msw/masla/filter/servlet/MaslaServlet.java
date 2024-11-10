package com.msw.masla.filter.servlet;

import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.io.IOException;

/**
 * Created by Gavin.peng on 2017/9/26.
 * Netty Http Servlet
 */
public interface MaslaServlet {

    /**
     * servlet mapping path
     * @return
     */
    String mappingPath();

    void init(MaslaDefaultProxyInvokerFactory proxyInvokerFactory);

    void service(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent event)throws IOException;

    void destory();
}
