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
package com.msw.masla.metrics.http;

import com.msw.masla.common.pojo.ApiMetric;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.monitor.vo.ApiMetricMonitorVO;
import com.msw.masla.common.monitor.vo.TotalMetricMonitorVO;
import com.msw.masla.common.pojo.ServiceAppCache;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.metrics.frame.AbstractMetrics;
import lombok.extern.slf4j.Slf4j;
import org.HdrHistogram.Histogram;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * API监控数据实现类，收集当前host各个API QPS，超时，4XX，5XX等监控数据
 */
@Slf4j
public class ApiMetrics extends AbstractMetrics {

  private int execNums = 0;
  private static long KB_SIZE = 1024l;
  private static long SERVICE_ID_ACTIVE_TIME_INTERVAL = 3 * 60 * 1000l;
  private NumberFormat numberFormat;

  private TotalMetricMonitorVO totalMetricMonitorVO = new TotalMetricMonitorVO();



  public ApiMetrics() {
    numberFormat = NumberFormat.getInstance();
    numberFormat.setMaximumFractionDigits(0);
  }

  @Override
  public List<ApiMetricMonitorVO> getMetrics() {

    List<ApiMetricMonitorVO> metricMonitorVOList = new ArrayList<ApiMetricMonitorVO>();
    if(count.get()%3==0) {
      totalMetricMonitorVO = new TotalMetricMonitorVO();
    }

    long timestampFix = getTimestamp();
    Date nowFix = new Date(timestampFix);

    long totalQps = 0;
    boolean allOFFLine = true;
    long appQps = 0;  // app的总qps
    long errorFiveCode = 0;
    long appConnectionCloseNums = 0;
    long appConnectionRefuseNums = 0;
    long appConnectionResetNums = 0;
    long appConnectionTimeoutNums = 0;
    long appAcquireConnFailedNums = 0;

    long appCircuitNums = 0;
    long appFlowLimitNums = 0;

    int appCircuitCounts = 0;//触发熔断app的个数
    int appFlowLimitCounts = 0;//触发流控app的个数
    int qpsDownZeroCounts = 0;//qps跌0的应用数


    List<ApiMetric> badMetricList = new ArrayList<ApiMetric>();
    Map<String, ServiceApp> appDOMap = ServiceAppCache.getAppCache();
    for (Map.Entry<String, ServiceApp> entry : appDOMap.entrySet()) {
      ServiceApp appDO = entry.getValue();
      long timestamp = timestampFix;
      Date now = nowFix;;



      Map<String, Long> serviceIdFailedMap = new HashMap<String, Long>();
      Map<String, Long> serviceIdTimeoutMap = new HashMap<String, Long>();
      Map<String, Long> serviceId4XXMap = new HashMap<String, Long>();
      Map<String, Long> serviceId400Map = new HashMap<String, Long>();
      Map<String, Long> serviceId401Map = new HashMap<String, Long>();
      Map<String, Long> serviceId404Map = new HashMap<String, Long>();
      Map<String, Long> serviceId5XXMap = new HashMap<String, Long>();
      Map<String, Long> serviceIdQpsMap = new HashMap<String, Long>();
      Map<String, Long> serviceIdSlowMap = new HashMap<String, Long>();
      Map<String, Long> serviceIdOutBandWidthMap = new HashMap<String, Long>();
      Map<String, Long> serviceIdInBandWidthMap = new HashMap<String, Long>();

      Map<String, Long> hostConnRefusedMap = new HashMap<String, Long>();
      Map<String, Long> hostConnResetMap = new HashMap<String, Long>();
      Map<String, Long> hostConnTimeoutMap = new HashMap<String, Long>();
      Map<String, Long> hostConnPoolQueueFullMap = new HashMap<String, Long>();
      Map<String, Long> hostConnWaitTimeoutdMap = new HashMap<String, Long>();


      if (!StringUtil.isEmptyString(appDO.getGroupName())) {

        int maxServiceIdCount = 0;

        //每个app的serviceid
        for (Map.Entry<String, ConcurrentHashMap<String, ApiMetric>> hostServerIdMetricEntry : appDO
                .getAppHostServiceIdMetricMap().entrySet()) {

          int serviceIdCount = 0;
          String host = hostServerIdMetricEntry.getKey();

          long appSidOutBandWidth = 0;
          long appSidInBandWidth = 0;

          Map<String, ApiMetric> apiMetricMap = hostServerIdMetricEntry.getValue();
          for (Map.Entry<String, ApiMetric> metricEntry : apiMetricMap.entrySet()) {
            try {
              String serviceId = metricEntry.getKey();
              long circuitCount = appDO.getAndResetServiceCircuitCount(serviceId);
              long flowControllerCount = appDO.getAndResetServiceFlowControllerCount(serviceId);
              ApiMetric metric = metricEntry.getValue();

              // setTotalMetrics
              TotalMetrics.getInstance().convert2TotalMetricMonitorVO(totalMetricMonitorVO, metric, appDO, host, serviceId,
                      circuitCount, flowControllerCount);

              ApiMetricMonitorVO vo = convertTOApiMetricMonitorVO(appDO, metric);
              appDO.setServiceIdlatencyPercent(host, serviceId, vo);
              vo.setHost(host);
              //vo.setHostNums(hostNums);
              vo.setCircuitState(circuitCount > 0 ? "1" : "0");
              vo.setGmtCreate(now);
              vo.setCircuitNums(circuitCount);
              vo.setFlowControllerNums(flowControllerCount);
//                long outbw = vo.getOutBandWidth() / KB_SIZE;
//                long inbw = vo.getInBandWidth() / KB_SIZE;
              if (vo.getOutBandWidth() > appSidOutBandWidth) {
                appSidOutBandWidth = vo.getOutBandWidth();
              }
              if (vo.getInBandWidth() > appSidInBandWidth) {
                appSidInBandWidth = vo.getInBandWidth();
              }



              appQps = appQps + vo.getQps();

              appCircuitNums = appCircuitNums + circuitCount;
              appFlowLimitNums = appFlowLimitNums + flowControllerCount;

              appConnectionCloseNums = appConnectionCloseNums + vo.getConnClosedNums();
              appConnectionTimeoutNums = appConnectionTimeoutNums + vo.getConnTimeoutNums();
              appConnectionRefuseNums = appConnectionRefuseNums + vo.getConnRefusedNums();
              appConnectionResetNums = appConnectionResetNums + vo.getConnResetNums();
              appAcquireConnFailedNums = appAcquireConnFailedNums + vo.getConnPoolFullRejectNums() + vo.getConnPoolWaitTimeoutNums();

              errorFiveCode = errorFiveCode + vo.getFiveXXCodeNums();


              metricMonitorVOList.add(vo);
              metric.setHost(host);
              if (vo.getQps() > 0) {
                metric.setLastActiveTime(timestamp);
              } else {
                if (timestamp - metric.getLastActiveTime() > SERVICE_ID_ACTIVE_TIME_INTERVAL) {
                  badMetricList.add(metric);
                }
              }

              summateHostConnError(hostConnRefusedMap, hostConnResetMap, hostConnTimeoutMap, hostConnPoolQueueFullMap, hostConnWaitTimeoutdMap, vo);

              serviceIdCount++;
            } catch (Throwable e) {
              log.error("Masla process api {} metric failed:", metricEntry.getKey(), e);
            }

          }//end serviceId


          if (serviceIdCount > maxServiceIdCount) {
            maxServiceIdCount = serviceIdCount;
          }
        }// end host
        //标记service id 数量
        appDO.setServiceIdCount(maxServiceIdCount);

        if (appQps + appFlowLimitNums + appCircuitNums > 0) {
          AppRequestFailedMetrics.getQpsMap().put(appDO.getId(), appQps + appFlowLimitNums + appCircuitNums);
        }

      }

      totalQps = totalQps + appQps;
      if (appDO.getStatus() == 1) {
        allOFFLine = false;
      }
      //按app post

      if (metricMonitorVOList.size() > 0) {
        clearAppBadServiceID(appDO, badMetricList);
      }

      if (appCircuitNums > 0) {
        appCircuitCounts++;
      }

      if (appFlowLimitNums > 0) {
        appFlowLimitCounts++;
      }

      appCircuitNums = 0;
      appFlowLimitNums = 0;

      appQps = 0;
      appConnectionCloseNums = 0;
      appConnectionTimeoutNums = 0;
      appConnectionRefuseNums = 0;
      appAcquireConnFailedNums = 0;
      errorFiveCode = 0;
    }

    totalQps = totalQps / 30;

    totalMetricMonitorVO.setServerIP(MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp());
    totalMetricMonitorVO.setGmtCreate(nowFix);

    return metricMonitorVOList;
  }

  private void summateHostConnError(Map<String,Long> hostConnRefusedMap,
      Map<String,Long> hostConnResetMap,
      Map<String,Long> hostConnTimeoutMap,
      Map<String,Long> hostConnPoolQueueFullMap,
      Map<String,Long> hostConnWaitTimeoutMap,
      ApiMetricMonitorVO apiMetricMonitorVO) {
    String host = apiMetricMonitorVO.getHost();
    if(hostConnRefusedMap.containsKey(host)){
      long connRefusedNums = hostConnRefusedMap.get(host) + apiMetricMonitorVO.getConnRefusedNums();
      hostConnRefusedMap.put(host, connRefusedNums);
    }else{
      hostConnRefusedMap.put(host, apiMetricMonitorVO.getConnRefusedNums());
    }

    if(hostConnResetMap.containsKey(host)){
      long connResetNums = hostConnResetMap.get(host) + apiMetricMonitorVO.getConnResetNums();
      hostConnResetMap.put(host, connResetNums);
    }else{
      hostConnResetMap.put(host, apiMetricMonitorVO.getConnResetNums());
    }

    if(hostConnTimeoutMap.containsKey(host)){
      long connTimeout = hostConnTimeoutMap.get(host) +  apiMetricMonitorVO.getConnTimeoutNums();
      hostConnTimeoutMap.put(host, connTimeout);
    }else{
      hostConnTimeoutMap.put(host, apiMetricMonitorVO.getConnTimeoutNums());
    }


    if(hostConnPoolQueueFullMap.containsKey(host)){
      long connQueueFullReject = hostConnPoolQueueFullMap.get(host) +  apiMetricMonitorVO.getConnPoolFullRejectNums();
      hostConnPoolQueueFullMap.put(host, connQueueFullReject);
    }else{
      hostConnPoolQueueFullMap.put(host, apiMetricMonitorVO.getConnPoolFullRejectNums());
    }


    if(hostConnWaitTimeoutMap.containsKey(host)){
      long connWaitTimeout = hostConnWaitTimeoutMap.get(host) +  apiMetricMonitorVO.getConnPoolWaitTimeoutNums();
      hostConnWaitTimeoutMap.put(host, connWaitTimeout);
    }else{
      hostConnWaitTimeoutMap.put(host, apiMetricMonitorVO.getConnPoolWaitTimeoutNums());
    }


  }

  private ApiMetricMonitorVO convertTOApiMetricMonitorVO(ServiceApp appDO, ApiMetric metric) {
    ApiMetricMonitorVO vo = new ApiMetricMonitorVO();
    vo.setAppId(appDO.getId());
    vo.setAppName(appDO.getName());
    if (metric != null) {
      vo.setServiceId(metric.getServiceId());
      long qps = metric.getQps().getAndSet(0);
      vo.setQps(qps);
      long peakQps = metric.getPeakQps().getAndSet(0);
      vo.setPeakQps(peakQps);
      float serverCost = metric.getServerCost().floatValue();
      metric.getServerCost().set(0);
      vo.setServerCost(serverCost);
      float acquireCost = metric.getAcquireCost().floatValue();
      metric.getAcquireCost().set(0);
      vo.setAcquireCost(acquireCost);

      float pushCost = metric.getPushCost().floatValue();
      metric.getPushCost().set(0);
      vo.setPushCost(pushCost);
      vo.setSuccessNums(metric.getSuccessNums().getAndSet(0));
      vo.setTimeoutNums(metric.getTimeoutNums().getAndSet(0));
      vo.setRejectNums(metric.getRejectNums().getAndSet(0));
      vo.setSlowNums(metric.getSlowNums().getAndSet(0));
      vo.setExceptionNums(metric.getExceptionNums().getAndSet(0));
      vo.setFiveXXCodeNums(metric.getFiveXXCodeNums().getAndSet(0));
      vo.setFourXXCodeNums(metric.getFourXXCodeNums().getAndSet(0));
      vo.setCode400(metric.getCode400().getAndSet(0));
      vo.setCode401(metric.getCode401().getAndSet(0));
      vo.setCode404(metric.getCode404().getAndSet(0));
      vo.setCodeAppDefine(metric.getCodeAppDefine().getAndSet(0));
      vo.setOutBandWidth(new Long(metric.getOutBandWidth()));
      vo.setInBandWidth(new Long(metric.getInBandWidth()));
      vo.setConnClosedNums(metric.getConnClosedNums().getAndSet(0));
      vo.setConnRefusedNums(metric.getConnRefusedNums().getAndSet(0));
      vo.setConnResetNums(metric.getConnResetNums().getAndSet(0));
      vo.setConnTimeoutNums(metric.getConnTimeoutNums().getAndSet(0));
      vo.setConnPoolFullRejectNums(metric.getConnPoolFullRejectNums().getAndSet(0));
      vo.setConnPoolWaitTimeoutNums(metric.getConnPoolWaitTimeoutNums().getAndSet(0));
      vo.setVarnishCacheMiss(metric.getVarnishCacheMiss().getAndSet(0));

      metric.resetBandWidth();
    }

    vo.setServerIP(MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp());
    return vo;

  }


  private void clearAppBadServiceID(ServiceApp appDO, List<ApiMetric> badMetricList) {
    if (badMetricList.size() > 0) {
      for (ApiMetric metric : badMetricList) {
        Map<String, ApiMetric> metricMap = appDO.getAppHostServiceIdMetricMap()
                .get(metric.getHost());
        if (metricMap != null) {
          metricMap.remove(metric.getServiceId());
        }

        Map<String, Histogram> histogramMap = appDO.getAppHostServiceIdTP90Map()
                .get(metric.getHost());
        if (histogramMap != null) {
          histogramMap.remove(metric.getServiceId());
        }

      }
      badMetricList.clear();
    }
  }

}