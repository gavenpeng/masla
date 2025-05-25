package com.msw.masla.filter.governance;

import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.monitor.metrics.AppRequestFailedCount;
import com.msw.masla.core.async.context.MaslaAsyncContext;
import com.msw.masla.core.router.rule.FlowSelectorRule;
import com.msw.masla.core.router.rule.RouteRule;
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
 * 黑名单过滤器
 */
@Slf4j
public class BlackListFilter extends AbstractMaslaFilter {

  private static final String FILTER_NAME = "BlackListFilter";

  private static final String SPAN_PROP_MANUAL_BLACK = "manualBlack";




  @Override
  public void init() {

  }

  @Override
  public void order() {

  }

  @Override
  public boolean apply(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent event) throws FilterException {

    boolean forbidden = false;
    try {

      ServiceApp serviceApp = requestContext.getService();
      MaslaAsyncContext maslaAsyncContext = (MaslaAsyncContext) requestContext;

      RouteRule routeRule = maslaAsyncContext.getRouteRule();

      if (routeRule.getStrategy().containsKey(FILTER_NAME)) {
          FlowSelectorRule flowSelectorRule = routeRule.getStrategy().get(FILTER_NAME);
          forbidden = FlowRuleMatchUtils.flowMatchSelectorRule(requestContext, flowSelectorRule);
      }

      if (forbidden) {
          if (requestContext.getServiceIdentify() != null) {
            AppRequestFailedCount appRequestFailedCount = serviceApp.getAppRequestFailedCount(requestContext.getServiceIdentify());
            appRequestFailedCount.getBlackForbiddenCount().incrementAndGet();
          }
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

      HttpResponse  httpResponse = MaslaHttpUtil
            .createResponse(HttpResponseStatus.FORBIDDEN, "Request Forbidden",
                Constants.MASLA_RESPONSE_HEADER_APPLICATION_BLACK);
      requestContext.getSession().writeAndClose(httpResponse);

    } finally {
      requestContext.getEvent().recycle();
      requestContext.recycle();
    }

  }




}
