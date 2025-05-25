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
package com.msw.masla.core.async.event;

import com.msw.masla.core.async.context.MaslaRequestContextBuilder;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.event.EventState;


/**
 * Created by Gavin.peng on 2017/6/15.
 */
public class AsyncNettyEvent extends BaseEvent {


    public AsyncNettyEvent(){
        super();
        this.setState(EventState.DISPATCHING);
    }

    public AsyncNettyEvent(Throwable throwable,EventState state){
        super();
        this.remoteException = throwable;
        this.state = state;
    }

    @Override
    public void recycle() {
        this.remoteException = null;
        this.result = null;
        this.execCount = 0;
        this.start = 0;
        this.startAcquireConnTime = 0;
        this.startSendTime = 0;
        this.sendCompleteTime = 0;
        this.responseCompleteTime = 0;
        this.startEncodeTime = 0;
        this.authEndTime = 0;
        this.authStartTime = 0;
        this.invokeSso = false;
        this.ssoCircuit = false;
        this.retryable = true;
        this.push = false;
        MaslaRequestContextBuilder.recycleEvent(this);
    }

    @Override
    public void reset() {
        this.setStart(System.nanoTime());
        this.state = EventState.DISPATCHING;
    }
}
