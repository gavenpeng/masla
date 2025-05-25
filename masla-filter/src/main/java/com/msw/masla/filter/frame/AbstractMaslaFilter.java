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
package com.msw.masla.filter.frame;

import com.msw.masla.core.utils.MaslaHttpUtil;
import com.msw.masla.filter.exception.FilterException;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.msw.masla.common.constant.Constants.MASLA_COMMON_MATCH_PATH;


/**
 * Author: Gavin.peng
 * Date: 2024/3/31
 * Description:
 */
public abstract class AbstractMaslaFilter implements MaslaFilter{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMaslaFilter.class);


    @Override
    public void doFilter(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent event,
                         MaslaFilterChain filterChain) throws FilterException {

        if (MaslaHttpUtil.isMaslaMetaPath(requestContext)) {
            filterChain.doFilter(requestContext, event);
            return;
        }

        try {
            if (!apply(requestContext, event)) {
                return;
            }

        }catch (Throwable e){
            LOG.error("Masla found request {} do filter {} failed:",requestContext.getRequestUrl(),this.getName(),e);
        }
        filterChain.doFilter(requestContext,event);

    }

    public abstract boolean apply(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent event) throws FilterException;

    public abstract String getName();

    @Override
    public void init() {

    }

    @Override
    public String mappingPath() {
        return MASLA_COMMON_MATCH_PATH;
    }
}
