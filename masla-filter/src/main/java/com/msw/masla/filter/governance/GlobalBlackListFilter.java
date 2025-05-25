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
package com.msw.masla.filter.governance;

import com.msw.masla.common.constant.Constants;
import com.msw.masla.core.router.rule.FlowSelectorRule;
import com.msw.masla.core.router.rule.RouteRuleCache;
import com.msw.masla.core.utils.MaslaHttpUtil;
import com.msw.masla.filter.exception.FilterException;
import com.msw.masla.filter.frame.AbstractMaslaFilter;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;


/**
 * Author: Gavin.peng
 * Date: 2024/8/25
 * Description:
 * Global black filter
 */
@Slf4j
public class GlobalBlackListFilter extends AbstractMaslaFilter {

    private static final String FILTER_NAME = "black";


    @Override
    public boolean apply(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent event) throws FilterException {

        boolean forbidden = false;
        try {

            FlowSelectorRule flowSelectorRule = RouteRuleCache.getGlobalFilter(FILTER_NAME);
            if (flowSelectorRule == null) {
                return true;
            }

            forbidden = FlowRuleMatchUtils.flowMatchSelectorRule(requestContext, flowSelectorRule);
            if (forbidden) {
                log.info("request {} is  match the black list of , forbidden it.",
                        requestContext.getRequestUrl());
                returnForbiddenResponse(requestContext);
                return false;
            }

        } catch (Throwable e) {
            log.error("flow rule filter {} failed:", this.getName(), e);
        }

        return true;
    }


    public String getName() {
        return FILTER_NAME;
    }


    private void returnForbiddenResponse(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext) {

        try {
            HttpResponse httpResponse = MaslaHttpUtil
                        .createResponse(HttpResponseStatus.FORBIDDEN, "Request Forbidden",
                                Constants.MASLA_RESPONSE_HEADER_GLOBAL_BLACK);

            requestContext.getSession().writeAndClose(httpResponse);
        }finally {
            requestContext.getEvent().recycle();
            requestContext.recycle();
        }

    }

    @Override
    public void order() {

    }

}
