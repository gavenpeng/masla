package com.msw.masla.core.invoker;

import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public interface ProxyInvoker {

    public void invoke(ChannelContext<IOSession, HttpRequest, HttpResponse> context);
}
