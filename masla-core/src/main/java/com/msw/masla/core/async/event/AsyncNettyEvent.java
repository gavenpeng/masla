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
