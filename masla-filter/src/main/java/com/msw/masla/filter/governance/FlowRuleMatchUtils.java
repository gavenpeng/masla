package com.msw.masla.filter.governance;

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
            if (!requestContext.getServiceIdentify().equals(flowSelectorRule.getPath())) {
                return false;
            }
        }

        if (!StringUtils.isEmpty(flowSelectorRule.getIp())) {
            String ip = requestContext.getHttpRequest().headers().get("CLIENT_IP");
            if (!ip.equals(flowSelectorRule.getIp())) {
                return false;
            }
        }

        if (!StringUtils.isEmpty(flowSelectorRule.getHeaderKey())
                && !StringUtils.isEmpty(flowSelectorRule.getHeaderKeyValue())) {
            String headerKey = flowSelectorRule.getHeaderKey();
            String configHeaderValue = flowSelectorRule.getHeaderKeyValue();

            String headerValue = requestContext.getHttpRequest().headers().get(headerKey);
            if (!configHeaderValue.equals(headerValue)) {
                return false;
            }
        }

        return true;


    }

}
