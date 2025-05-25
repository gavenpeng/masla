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
package com.msw.masla.core.async.pool;

import com.msw.masla.core.push.engine.PushEngine;
import com.msw.masla.core.push.engine.SyncPushEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Gavin.peng on 2017/6/5.
 * 拒绝策略，尝试取消掉队列中的头，很可能要超时，把当前请求添加到队列里。
 */
public class MaslaDiscardOldestPolicy implements RejectedExecutionHandler {

    public static final Logger LOG = LoggerFactory.getLogger(MaslaDiscardOldestPolicy.class);

    /*
     * Push 队列满了，直接io线程push 掉
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        releaseAsyncRequest(r);
    }

    /**
     * 拒绝的请求需要让异步请求完成
     * @param r
     */
    public void releaseAsyncRequest(Runnable r) {

        if (r instanceof PushTask) {
            PushTask pushTask = (PushTask) r;
            directPush(pushTask);
            return;
        }
        throw new IllegalArgumentException("Masla push task "+r.getClass().getName()+" only support type of PushTask!!!");
    }


    private void directPush(PushTask task){
        LOG.warn("Masla push queue is too bussiness so direct to push");
        PushEngine pushEngine = SyncPushEngine.getPushEngine(null);
        pushEngine.push(task.getContext(),task.getEvent());
    }
}
