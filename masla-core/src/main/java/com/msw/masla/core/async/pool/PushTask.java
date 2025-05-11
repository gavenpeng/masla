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
