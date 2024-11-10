package com.msw.masla.core.push.engine;


import com.msw.masla.protocol.http.netty.event.IEvent;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
public interface PushEngine<C, E extends IEvent<T>,T> {

     void push(final C context,final E event);

     boolean isAsync();

     void releaseResource();
}
