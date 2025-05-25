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


import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;

/**
 * Created by Gavin.peng on 2017/6/12.
 */
public  abstract class PushTask implements Runnable {

    protected SessionContext context;

    protected BaseEvent event;


    public PushTask(SessionContext context, BaseEvent event){
        this.reset(context,event);
    }

    public void reset(SessionContext context, BaseEvent event){
        this.context = context;
        this.event = event;
    }

    public SessionContext getContext() {
        return context;
    }

    public BaseEvent getEvent() {
        return event;
    }

    @Override
    public final void run() {
        try {
            doRun();
        }catch (Throwable e){

        }
    }

    public abstract void doRun();


}
