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


import java.util.concurrent.*;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
public class MaslaProcessorExecutors {

    private Integer coreSize = 10;

    protected ExecutorService executorService;


    static class MaslaProcessorExecutorsHolder{
        static MaslaProcessorExecutors executors = new MaslaProcessorExecutors();
    }

    public static MaslaProcessorExecutors getInstance(){
        return MaslaProcessorExecutorsHolder.executors;
    }


    public void init(int coreSize){
        this.coreSize = coreSize;
        executorService =  Executors.newFixedThreadPool(this.coreSize);
    }

    public void submitProcessor(Runnable task){
        this.executorService.execute(task);
    }


    public void release(){
        this.executorService.shutdown();
    }
}
