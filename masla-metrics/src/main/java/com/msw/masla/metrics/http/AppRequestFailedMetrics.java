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

import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.pojo.AppRequestFailedDO;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.monitor.metrics.AppRequestFailedCount;
import com.msw.masla.common.pojo.ServiceAppCache;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.metrics.frame.AbstractMetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gavin.peng
 */
public class AppRequestFailedMetrics extends AbstractMetrics {

  public static final String REQ_LINE_TOO_LONG = "An HTTP line is larger than 4096 bytes";

  // appId -> qps(qps + flowControl + circuit)
  private static HashMap<Long, Long> qpsMap = new HashMap<Long, Long>(128);

  @Override
  public List getMetrics() {
    List retList = new ArrayList();

    Map<String, ServiceApp> appDOMap = ServiceAppCache.getAppCache();
    long timestamp = getTimestamp();
    List<AppRequestFailedDO> appRequestFailedCountList = new ArrayList<AppRequestFailedDO>();
    List<String> badServiceIdList = new ArrayList<String>(10);
    for (ServiceApp appDO : appDOMap.values()) {
      for (AppRequestFailedCount count : appDO.getAppRequestFailedMap().values()) {
        if(count.hasFailedCount()) {
          AppRequestFailedDO appRequestFailedDO = new AppRequestFailedDO();
          appRequestFailedDO.setAppId(appDO.getId());
          appRequestFailedDO.setAppName(appDO.getName());
          appRequestFailedDO.setServiceId(count.getServiceId());
          appRequestFailedDO.setHost(MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp());
          appRequestFailedDO.setBlackForbiddenCount(count.getBlackForbiddenCount().getAndSet(0));
          appRequestFailedDO.setNoLoginCount(count.getNoLoginCount().getAndSet(0));
          appRequestFailedDO.setRequestTooLarge(count.getRequestTooLarge().getAndSet(0));
          appRequestFailedDO.setCustomizedResponseHitCount(count.getCustomizedResponseHitCount().getAndSet(0));
          appRequestFailedDO.setNoAvailableHostCount(count.getNoAvailableHostCount().getAndSet(0));
          appRequestFailedDO.setHeaderTooLargeCount(count.getHeaderTooLargeCount().getAndSet(0));
          appRequestFailedDO.setHttpLineTooLargeCount(count.getHttpLineTooLargeCount().getAndSet(0));
          appRequestFailedDO.setReadConnClosedCount(count.getReadConnClosedCount().getAndSet(0));
          appRequestFailedDO.setHeaderInvalidCharacterCount(count.getHeaderInvalidCharacterCount().getAndSet(0));
          appRequestFailedDO.setNoReceivePostBodyCount(count.getNoReceivePostBodyCount().getAndSet(0));
          appRequestFailedDO.setQueueFullCount(count.getQueueFullCount().getAndSet(0));
          appRequestFailedDO.setTimestamp(timestamp);
          appRequestFailedCountList.add(appRequestFailedDO);
        }else{
          badServiceIdList.add(count.getServiceId());
        }

      }

      //请求端连接已关闭异常统计,有数据才统计
      if(appDO.getClientConnClosedCount().get() >0 ) {
        AppRequestFailedDO appRequestFailedDO = new AppRequestFailedDO();
        appRequestFailedDO.setAppId(appDO.getId());
        appRequestFailedDO.setAppName(appDO.getName());
        appRequestFailedDO.setServiceId("client_close");
        appRequestFailedDO.setHost(MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp());
        appRequestFailedDO.setTimestamp(timestamp);
        appRequestFailedDO.setClientConnClosedCount(appDO.getClientConnClosedCount().getAndSet(0));
        appRequestFailedCountList.add(appRequestFailedDO);
      }

      //清除没有数据的serviceid
      if(badServiceIdList.size() >0 ){
        for(String badSID:badServiceIdList){
          appDO.getAppRequestFailedMap().remove(badSID);
        }
      }
    }

    retList.add(appRequestFailedCountList);
    retList.add(qpsMap);
    qpsMap = new HashMap<Long, Long>();
    return retList;
  }




  public static HashMap<Long, Long> getQpsMap() {
    return qpsMap;
  }
}
