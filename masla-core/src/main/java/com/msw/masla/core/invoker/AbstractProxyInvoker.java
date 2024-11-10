package com.msw.masla.core.invoker;

import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * Author: Gavin.peng
 * Date: 2024/7/14
 * Description:
 */
public abstract class AbstractProxyInvoker implements ProxyInvoker {

    @Override
    public void invoke(ChannelContext<IOSession, HttpRequest, HttpResponse> context) {

        doInvoke(context);

    }

    public abstract void doInvoke(ChannelContext<IOSession, HttpRequest, HttpResponse> context);
}
