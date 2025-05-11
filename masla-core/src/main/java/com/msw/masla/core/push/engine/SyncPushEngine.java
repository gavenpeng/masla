package com.msw.masla.core.push.engine;

import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.core.async.handle.factory.HandlerFactory;
import com.msw.masla.core.async.handle.factory.MaslaHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Gavin.peng on 2017/6/5.
 * 同步是相对HTTP CORE NIO的，也就是IO线程直接把响应写给客户端.
 */

public class SyncPushEngine extends BasePushEngine {

    private static final Logger LOG = LoggerFactory.getLogger(SyncPushEngine.class);

    private static SyncPushEngine syncPushEngine;
    protected HandlerFactory handlerFactory;

    public static PushEngine getPushEngine(MaslaDefaultProxyInvokerFactory factory) {
        if (syncPushEngine != null) {
            return syncPushEngine;
        }
        synchronized (SyncPushEngine.class) {
            if (syncPushEngine == null) {
                syncPushEngine = new SyncPushEngine(factory);
            }
        }
        return syncPushEngine;
    }


    private SyncPushEngine(MaslaDefaultProxyInvokerFactory factory){
        super(factory);
        this.handlerFactory = MaslaHandlerFactory.getInstance();
    }

    @Override
    public void push(SessionContext context, BaseEvent event) {
        doPush(context,event);
    }


    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void releaseResource(){

    }
}
