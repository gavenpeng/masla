package com.msw.masla.filter.governance;

import com.msw.masla.common.circuit.CircuitFactory;
import com.msw.masla.common.circuit.CircuitRuleDefine;
import com.msw.masla.common.circuit.MaslaCircuitBreakerImpl;
import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.util.StringBuilderHolder;
import com.msw.masla.core.utils.MaslaBackupResponseUitls;
import com.msw.masla.core.utils.NettyCommonUtil;
import com.msw.masla.filter.exception.FilterException;
import com.msw.masla.filter.frame.AbstractMaslaFilter;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 判断是否需要熔断请求的过滤器，返回本地缓存中的Varnish响应
 */

/**
 * Author: Gavin.peng
 * Date: 2024/8/10
 * Description:
 */
@Slf4j
public class CircuitFilter extends AbstractMaslaFilter {

  private static final Logger LOG = LoggerFactory.getLogger(CircuitFilter.class);

  private static final String FILTER_NAME = "CircuitFilter";


  @Override
  public boolean apply(ChannelContext<IOSession, HttpRequest, HttpResponse> context, BaseEvent event) throws FilterException {

    try {
      ServiceApp appDO = context.getService();
      String serviceId = context.getServiceIdentify();
      //获取熔断配置
      StringBuilder stringBuilder = StringBuilderHolder.getGlobal();
      stringBuilder.append(appDO.getContextRoot()).append(serviceId);
      String appServiceId = stringBuilder.toString();
      MaslaCircuitBreakerImpl circuitBreaker = (MaslaCircuitBreakerImpl) CircuitFactory.getCircuitBreaker(appDO);
      //检查是否有熔断配置
      if (circuitBreaker != null && allowCheckCircuit(context, circuitBreaker.getApiCircuitDO())
              && circuitBreaker.doCircuit()) {
        appDO.addServiceCircuitCount(serviceId);
        doClosure(context, appServiceId, circuitBreaker.getApiCircuitDO());
        return true;
      }
    }catch (Throwable e){
      LOG.error("Masla gateway do circuit filter failed:",e);
    }


    return false;


  }

  @Override
  public String getName() {
    return FILTER_NAME;
  }




  /**
   * 熔断，先检查是否有varnish缓存，有用，没有再检查是否配置了自定义响应
   * @param context
   * @param serviceId
   * @param apiCircuitDO
   */
  private void doClosure(ChannelContext<IOSession, HttpRequest, HttpResponse> context, String serviceId, CircuitRuleDefine apiCircuitDO) {

    //标记开始熔断的时间
    if (apiCircuitDO != null && apiCircuitDO.getCircuitTime().get() == -1) {
      apiCircuitDO.setCircuitTime();
    }
    context.getSession().setError();
    try {

      HttpResponseStatus circuitResponseCode = NettyCommonUtil.CIRCUIT_REQUESTS;
      if (apiCircuitDO != null) {
        HttpResponse httpResponse = MaslaBackupResponseUitls.fillBackupResponse(context,serviceId);
        if (httpResponse != null){
          httpResponse.headers().set(Constants.MASLA_RESPONSE_HEADER_KEY,Constants.MASLA_RESPONSE_HEADER_CIRCUIT);
          context.getSession().writeAndFlush(httpResponse);
        } else {
          context.getSession().writeError(circuitResponseCode, Constants.MASLA_RESPONSE_HEADER_CIRCUIT,true);
        }
      } else {
        context.getSession().writeError(circuitResponseCode,Constants.MASLA_RESPONSE_HEADER_CIRCUIT, true);
      }
    } finally {
      context.getEvent().recycle();
      context.recycle();
    }


  }



  /**
   * 检查熔断配置，逻辑如下：
   *
   * 1. 如果没有配置API或者APP级别熔断，不进行熔断 2. 如果配置熔断，并且配置熔断参数，但是没有命中请求参数，不进行熔断 3.
   * 如果配置熔断，没有配置参数熔断，或者参数熔断命中，进行熔断检查
   *
   * @param context 请求上下文
   * @param apiCircuitDO 熔断配置对象
   * @return true: 允许熔断检查，false：不进行熔断检查
   */
  private boolean allowCheckCircuit(ChannelContext<IOSession, HttpRequest, HttpResponse> context, CircuitRuleDefine apiCircuitDO) {

    //没有熔断配置，不进行检测
    if (apiCircuitDO == null) {
      return false;
    }
    //没有配置参数级别，直接 熔断
    if (apiCircuitDO.getCustomizedResponseParamMap() == null ||
            apiCircuitDO.getCustomizedResponseParamMap().isEmpty()){
      return true;
    }

    //检查参数级别
    if (!contains(context, apiCircuitDO)) {
      return false;
    }

    return true;
  }

  /**
   * 检查请求参数是否命中熔断配置参数, 配置多个参数时，关系为与的关系，必须全部匹配
   *
   * @param context 请求上下文
   * @param apiCircuitDO 熔断配置对象
   * @return true: 命中，false：没有命中
   */
  private boolean contains(ChannelContext<IOSession, HttpRequest, HttpResponse> context,
                           CircuitRuleDefine apiCircuitDO) {

    //如果cookie没有解析，则临时解析
    if (null == context.getCookie()) {
      context.fillCookies();
    }

    Map<String, String> requestParamMap = new HashMap<String, String>();

    if (context.getCookie() != null && !context.getCookie().isEmpty()) {
      requestParamMap.putAll(context.getCookie());
    }
    if (context.getHeaders() != null && !context.getHeaders().isEmpty()) {
      requestParamMap.putAll(context.getHeaders());
    }

    //requestParams不能重复调用
    Map<String,String> requestParams = context.getParams();
    if (requestParams != null && !requestParams.isEmpty()) {
      requestParamMap.putAll(requestParams);
    }
//    if (context.getParams() != null && !context.getParams().isEmpty()) {
//      requestParamMap.putAll(context.getParams());
//    }

    //检查请求参数是否包含全部设置参数名
    for (String paramName : apiCircuitDO.getCustomizedResponseParamMap().keySet()) {
      String requestParamValue = requestParamMap.get(paramName);
      if(requestParamValue == null){
        return false;
      }
      String[] customizedResponseParamValues = apiCircuitDO
              .getCustomizedResponseParamMap().get(paramName).split(",");
      if (!Arrays.asList(customizedResponseParamValues).contains(requestParamValue)) {
        return false;
      }
    }

    return true;


  }

  @Override
  public void order() {

  }
}
