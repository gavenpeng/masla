package com.msw.masla.protocol.http.netty.http.handler;



import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Gavin.peng on 2017/5/23.
 */
public interface RequestHeaderHandler {

    public static final Logger LOG = LoggerFactory.getLogger(RequestHeaderHandler.class);

    void doHandle(HttpRequest request) throws IOException;
}
