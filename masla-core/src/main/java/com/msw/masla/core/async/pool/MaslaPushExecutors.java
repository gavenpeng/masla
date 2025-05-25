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

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
@Component
public class MaslaPushExecutors {


    private static final int DEFAULT_POOL_CORESIZE = 20;
    private static final int DEFAULT_POOL_MAXSIZE = 100;
    private static final int DEFAULT_POOL_IDLETIME = 60;
    private static final int DEFAULT_POOL_QUEUESIZE = 500;


    @NacosValue(value = "${masla.push.thread.pool.core.size:10}", autoRefreshed = true)
    private Integer coreSize;

    @NacosValue(value = "${masla.push.thread.pool.core.maxsize:20}", autoRefreshed = true)
    private Integer maxSize;

    @NacosValue(value = "${masla.push.thread.pool.core.queuesize:200}", autoRefreshed = true)
    private Integer queueSize;

    private ThreadPoolExecutor executor;


    @PostConstruct
    public void init(){
        BlockingQueue<Runnable> eventQueue = new LinkedBlockingQueue<Runnable>(queueSize);
        executor = new ThreadPoolExecutor(coreSize, maxSize, DEFAULT_POOL_IDLETIME, TimeUnit.SECONDS, eventQueue, new MaslaThreadFactory(), new MaslaDiscardOldestPolicy());
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }


}
