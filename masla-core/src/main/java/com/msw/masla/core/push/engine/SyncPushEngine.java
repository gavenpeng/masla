/*
 * Copyright 2025 msw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
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
