package com.msw.masla.core.async.repsone;

import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.core.push.engine.AsyncPushEngine;
import com.msw.masla.core.push.engine.PushEngine;

/**
 * Created by Gavin.peng on 2017/8/21.
 */
public class MaslaDirectResponse implements MaslaResponse {

    private PushEngine pushEngine;

    private static class MaslaDirectResponseHolder{
        static final MaslaDirectResponse instance = new MaslaDirectResponse();
    }

    public static MaslaDirectResponse getInstance(){
        return MaslaDirectResponseHolder.instance;
    }


    public void initPushEngine(MaslaDefaultProxyInvokerFactory factory){
        this.pushEngine =  AsyncPushEngine.getPushEngine(factory);
    }

    @Override
    public void witerBack(ChannelContext maslaContext) {
        this.pushEngine.push(maslaContext, maslaContext.getEvent());
    }
}
