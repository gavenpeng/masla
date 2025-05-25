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
package com.msw.masla.server.processor;

import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Gavin.Peng on 2024/03/19.
 */
@Data
public abstract class MaslaProcessorBase implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(MaslaProcessorBase.class);


    protected IOSession session;

    protected FullHttpRequest request;

    public MaslaProcessorBase(IOSession session, FullHttpRequest request){
        this.reset(session,request);
    }

    public void reset(IOSession session, FullHttpRequest request){
        this.session = session;
        this.request = request;
    }

    @Override
    public final void run() {

        try {
            doRun();
        }catch (Throwable e){
            try {
                LOG.error("Masla process request {} failed:", request.uri(), e);
            }catch (Throwable ee){
                LOG.error("Masla record error log failed:", ee);
            }

        }
    }

    public abstract void doRun();

}
