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
package com.msw.masla.server.dispatch;

import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Gavin.peng on 2017/9/26.
 */
public abstract class AbstractHttpDispatch implements MaslaDispatch {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractHttpDispatch.class);


    @Override
    public void dispatch(IOSession session, FullHttpRequest request) {

        doDispatch(session, request);
    }

    protected abstract void doDispatch(IOSession session, FullHttpRequest request);

}
