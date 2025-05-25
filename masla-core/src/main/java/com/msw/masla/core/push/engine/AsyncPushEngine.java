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


import com.msw.masla.common.constant.Constants;
import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.event.EventState;
import com.msw.masla.core.async.pool.PushTask;
import com.msw.masla.protocol.http.netty.pool.SynchronizedStack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
public class AsyncPushEngine extends BasePushEngine {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncPushEngine.class);

    protected ThreadPoolExecutor executor;
    protected SynchronizedStack<PushTask> pushTaskCache;

    private static volatile AsyncPushEngine asyncPushEngine;


    private AsyncPushEngine(MaslaDefaultProxyInvokerFactory factory) {
        super(factory);
        executor = factory.getPushEngineExecutor();
        this.pushTaskCache = new SynchronizedStack<PushTask>(Constants.CACHE_SIZE, Constants.CACHE_SIZE_LIMIT);
    }

    public static PushEngine getPushEngine(MaslaDefaultProxyInvokerFactory factory) {
        if (asyncPushEngine != null) {
            return asyncPushEngine;
        }
        synchronized (AsyncPushEngine.class) {
            if (asyncPushEngine == null) {
                asyncPushEngine = new AsyncPushEngine(factory);
            }
        }
        return asyncPushEngine;
    }

    @Override
    public void push(final SessionContext context, final BaseEvent event) {

        try {
            event.setState(EventState.PUSH_INIT);
            PushTask pushTask = pushTaskCache.pop();
            if (pushTask == null) {
                pushTask = new MaslaPusher(context, event);
            } else {
                pushTask.reset(context, event);
            }
            executor.execute(pushTask);
        } catch (Throwable e) {
            LOG.error("Masla push request {} response status {} failed", context.getRequestUrl(), event.getState(), e);
        }

    }


    @Override
    public boolean isAsync() {
        return true;
    }


    @Override
    public void releaseResource() {
        LOG.info("start shutdown push thread pool...");
        this.executor.shutdown();
    }

    protected class MaslaPusher extends PushTask {

        public MaslaPusher(SessionContext context, BaseEvent event) {
            super(context, event);
        }


        @Override
        public void doRun() {
            try {
                doPush(this.context,this.event);
            }finally {
                //return to cache for reduce minor GC
                pushTaskCache.push(this);
            }

        }

    }

}

