package com.msw.masla.core.async.handle;

import com.msw.masla.protocol.http.netty.event.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
public interface EventHandler<C, E extends IEvent<T>,T> {

    public static final Logger LOG = LoggerFactory.getLogger(EventHandler.class);

    void handle(C requestContext,E event) throws Throwable;

    void close();
}
