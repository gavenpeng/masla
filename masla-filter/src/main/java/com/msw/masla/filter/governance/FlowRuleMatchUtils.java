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
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * Author: Gavin.peng
 * Date: 2024/8/11
 * Description:
 */
public class FlowRuleMatchUtils {

    public static boolean flowMatchSelectorRule(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext,
                                         FlowSelectorRule flowSelectorRule) {

        if (!StringUtils.isEmpty(flowSelectorRule.getPath())) {
            String reqPath = requestContext.getSession().getContextRoot() + requestContext.getServiceIdentify();
            if (!reqPath.equals(flowSelectorRule.getPath())) {
                return false;
            }
        }

        if (!StringUtils.isEmpty(flowSelectorRule.getIp())) {
            String ip = requestContext.getHttpRequest().headers().get(Constants.CLIENT_REAL_IP);
            if (!ip.equals(flowSelectorRule.getIp())) {
                return false;
            }
        }

        if (!StringUtils.isEmpty(flowSelectorRule.getHeaderKey())
                && !StringUtils.isEmpty(flowSelectorRule.getHeaderKeyValue())) {
            String headerKey = flowSelectorRule.getHeaderKey();
            String configHeaderValue = flowSelectorRule.getHeaderKeyValue();

            String headerValue = requestContext.getHttpRequest().headers().get(headerKey);
            return configHeaderValue.equals(headerValue);
        }

        return true;


    }

}
