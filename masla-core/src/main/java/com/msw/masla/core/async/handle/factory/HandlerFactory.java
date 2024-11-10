package com.msw.masla.core.async.handle.factory;

import com.msw.masla.protocol.http.netty.event.IEvent;
import com.msw.masla.core.async.handle.EventHandler;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
public interface HandlerFactory {

    EventHandler create(IEvent event);


    /**
     * 动态添加handler
     * @param eventClasses push 的class事件
     * @param handler
     */
    void addHandler(Class eventClasses,EventHandler handler);

}
