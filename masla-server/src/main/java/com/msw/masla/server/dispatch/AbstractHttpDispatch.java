package com.msw.masla.server.dispatch;

import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Gavin.peng on 2017/9/26.
 */
public abstract class AbstractHttpDispatch implements MaslaDispatch {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractHttpDispatch.class);


    @Override
    public void dispatch(IOSession session, FullHttpRequest request) {

        doDispatch(session, request);
    }

    protected abstract void doDispatch(IOSession session, FullHttpRequest request);

}
