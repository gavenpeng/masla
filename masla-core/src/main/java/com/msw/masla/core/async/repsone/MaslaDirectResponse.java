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
package com.msw.masla.core.async.repsone;

import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.protocol.http.netty.context.SessionContext;
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
    public void witerBack(SessionContext maslaContext) {
        this.pushEngine.push(maslaContext, maslaContext.getEvent());
    }
}
