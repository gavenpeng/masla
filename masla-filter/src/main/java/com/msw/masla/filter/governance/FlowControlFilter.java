package com.msw.masla.filter.governance;

import com.google.common.util.concurrent.RateLimiter;
import com.msw.masla.common.constant.Constants;
import com.msw.masla.core.async.context.MaslaAsyncContext;
import com.msw.masla.core.router.DefaultRouteRuleFactory;
import com.msw.masla.core.router.rule.FlowSelectorRule;
import com.msw.masla.core.router.rule.RouteRule;
import com.msw.masla.core.utils.MaslaBackupResponseUitls;
import com.msw.masla.core.utils.NettyCommonUtil;
import com.msw.masla.filter.exception.FilterException;
import com.msw.masla.filter.frame.AbstractMaslaFilter;
import com.msw.masla.metrics.http.DomainMetrics;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;


/**
 * Author: Gavin.peng
 * Date: 2024/8/17
 * Description:
 * app/serviceid 级别的流量控制,级别秒级别的qps，允许有一定的波动，因为低峰时没有用完的令牌，
 * 会给到高峰用，超过阀值则直接拒绝
 */
@Slf4j
public class FlowControlFilter extends AbstractMaslaFilter {

    private static final String FILTER_NAME = "flowLimit";


    @Override
    public boolean apply(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent event) throws FilterException {
        String serviceId = requestContext.getServiceIdentify();
        String appServiceId = requestContext.getSession().getContextRoot() + serviceId;

        MaslaAsyncContext maslaAsyncContext = (MaslaAsyncContext) requestContext;
        RouteRule routeRule = maslaAsyncContext.getRouteRule();

        if (!routeRule.getStrategy().containsKey(FILTER_NAME)) {
            return true;
        }
        FlowSelectorRule flowSelectorRule = routeRule.getStrategy().get(FILTER_NAME);
        boolean match = FlowRuleMatchUtils.flowMatchSelectorRule(requestContext, flowSelectorRule);

        if (!match) {
            return true;
        }

        RateLimiter rateLimiter = routeRule.getRateLimiter();
        if (rateLimiter == null) {
            DefaultRouteRuleFactory.getDefaultRouteRuleFactoryInstance().initRateLimiter(flowSelectorRule, routeRule);
            rateLimiter = routeRule.getRateLimiter();
        }

        try {
            if (rateLimiter != null && !rateLimiter.tryAcquire()) {
                //获取不到令牌，说明流量超过了阀值，拒绝请求,
                processNotAcquire(rateLimiter, requestContext, serviceId, appServiceId);
                return false;
            }
        } catch (Throwable e) {
            log.error("Masla flow controller request {} failed:", requestContext.getRequestUrl(), e);
        }
        return true;
    }

    private void processNotAcquire(RateLimiter limiter, SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, String serviceId, String appServiceId) {
        if(log.isInfoEnabled()){
            log.info("Masla flow limit rule rate {} permit is not permit for request {}", limiter.getRate(), requestContext.getRequestUrl());
        }
        requestContext.getService().addServiceFlowControllerCount(serviceId);
        DomainMetrics.addDomainFlowControllerCount(requestContext.getService().getName(), requestContext.getRouteHost(), requestContext.getRouteHost(),"local", 1);
        try{
            HttpResponse httpResponse = MaslaBackupResponseUitls.fillBackupResponse(requestContext,appServiceId);
            if(httpResponse != null){
                httpResponse.headers().set(Constants.MASLA_RESPONSE_HEADER_KEY,Constants.MASLA_RESPONSE_HEADER_FLOW_CONTROLLER);
                requestContext.getSession().writeAndClose(httpResponse);
            }else {
                requestContext.getSession().writeError(NettyCommonUtil.FLOW_CONTROL_REQUESTS, Constants.MASLA_RESPONSE_HEADER_FLOW_CONTROLLER,true);
            }
        } finally {
            requestContext.getEvent().recycle();
            requestContext.recycle();
        }
    }

    @Override
    public void order() {

    }

    @Override
    public String getName() {
        return FILTER_NAME;
    }
}
