package com.msw.masla.protocol.http.netty.http.handler;



import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

import java.io.IOException;

/**
 * Created by Gavin.peng on 2017/5/23.
 */
public class ConnectionHeaderHandler implements RequestHeaderHandler {

    @Override
    public void doHandle(HttpRequest request) throws IOException {
        final String method = request.method().name();
        if (method.equalsIgnoreCase("CONNECT")) {
            return;
        }

        if (!request.headers().contains(HttpHeaderNames.CONNECTION)) {
            // Default policy is to keep connection alive
            // whenever possible
            request.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderNames.KEEP_ALIVE);
        }
    }
}
