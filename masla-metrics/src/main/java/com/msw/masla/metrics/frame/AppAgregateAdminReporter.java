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
package com.msw.masla.metrics.frame;

import com.alibaba.fastjson.JSON;
import com.msw.masla.common.monitor.metrics.DomainCount;
import com.msw.masla.common.monitor.metrics.MetricsEntry;
import com.msw.masla.common.monitor.vo.ApiMetricMonitorVO;
import com.msw.masla.common.util.HttpClientUtil;
import com.msw.masla.common.util.MathUtils;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.metrics.http.ApiMetrics;
import com.msw.masla.metrics.http.DomainMetrics;
import com.msw.masla.metrics.http.DomainTotalMetrics;
import com.msw.masla.metrics.http.TotalMetrics;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author gavin.peng
 */
@Slf4j
public class AppAgregateAdminReporter extends AdminReporter {

  @Override
  public void report() {
    Map<String, List<Object>> metricsDataMap = getMetricsData();
    for (Map.Entry<String, List<Object>> entry : metricsDataMap.entrySet()) {

      if (log.isDebugEnabled()) {
        log.debug("sending {} metrics", entry.getKey());
      }

      if (entry.getValue() == null || entry.getValue().isEmpty()) {
        continue;
      }

      if (ApiMetrics.class.getSimpleName().equals(entry.getKey())) {
        Map<Long, List<ApiMetricMonitorVO>> appMetricsMap = new HashMap<Long, List<ApiMetricMonitorVO>>();

        for (Object object : entry.getValue()) {
          ApiMetricMonitorVO apiMetrics = (ApiMetricMonitorVO) object;
          List<ApiMetricMonitorVO> appMetricsList = appMetricsMap.get(apiMetrics.getAppId());
          if (appMetricsList == null) {
            appMetricsList = new LinkedList<ApiMetricMonitorVO>();
            appMetricsMap.put(apiMetrics.getAppId(), appMetricsList);
          }
          appMetricsList.add(apiMetrics);

        }
        for (Map.Entry<Long, List<ApiMetricMonitorVO>> appMetricsEntry : appMetricsMap.entrySet()) {
//          log.info("Start send time {} app name {}, app id {}",
//                  DateFormatUtil.formatDateTime(appMetricsEntry.getValue().get(0).getGmtCreate()),appMetricsEntry.getValue().get(0).getAppName(), appMetricsEntry.getKey());
          send(new MetricsEntry(entry.getKey(), appMetricsEntry.getValue()), appMetricsEntry.getKey());
        }

      } else if (DomainMetrics.class.getSimpleName().equals(entry.getKey())) {
        Map<String, List<DomainCount>> domainMetricsMap = new HashMap<String, List<DomainCount>>();
        for (Object object : entry.getValue()) {
          DomainCount domainMetrics = (DomainCount) object;
          List<DomainCount> domainMetricsList = domainMetricsMap.get(domainMetrics.getDomain());
          if (domainMetricsList == null) {
            domainMetricsList = new LinkedList<DomainCount>();
            domainMetricsMap.put(domainMetrics.getDomain(), domainMetricsList);
          }
          domainMetricsList.add(domainMetrics);

        }

        //按APP发送到ADMIn
        for (Map.Entry<String, List<DomainCount>> domainMetricsEntry : domainMetricsMap.entrySet()) {
          long domainIdx = MathUtils.mod(domainMetricsEntry.getKey().hashCode());
          send(new MetricsEntry(entry.getKey(), domainMetricsEntry.getValue()), domainIdx);
        }

      } else if (TotalMetrics.class.getSimpleName().equals(entry.getKey())) {
        send(new MetricsEntry(entry.getKey(), entry.getValue()), 0);
      } else if (DomainTotalMetrics.class.getSimpleName().equals(entry.getKey())) {
        send(new MetricsEntry(entry.getKey(), entry.getValue()), 0);
      } else {
        send(new MetricsEntry(entry.getKey(), entry.getValue()));
      }

    }
  }

  public void send(MetricsEntry entry, int urlIndex) {
    sentMetrics(JSON.toJSONString(entry), urlIndex);
  }

  public void sentMetrics(String content, int urlIndex) {
    try {
      if (urlList.isEmpty()) {
        log.warn("the metrics report url list is empty, try to reload it again");
        init();
      }

      int size = urlList.size();
      if (urlIndex >= size) {
        log.warn("the urlIndex is out of urlList's size");
        urlIndex = size - 1;
      }

      String url = urlList.get(urlIndex);
      if(log.isDebugEnabled()){
        log.debug("send total metrics data to {}", url);
      }

    } catch (Throwable e) {
      log.error("error to send metrics to console ", e);
    }
  }

  public void send(MetricsEntry entry, Long code) {
    sentMetrics(entry, code);
  }



  public void sentMetrics(MetricsEntry entry, Long code) {
      sentData(entry,code,urlList);
  }


  private void sentData(MetricsEntry entry, Long code,List<String> urlList) {

    try {
      String content = JSON.toJSONString(entry);
      if (urlList.isEmpty()) {
        log.warn("the metrics report url list is empty, try to reload it again");
        init();
      }
      int size = urlList.size();
      int pos = code.intValue() % size;
      String url = urlList.get(pos);

      boolean retry = isRetrySend(url, content);
      if (log.isDebugEnabled()) {
        log.debug("send metrics data type {} to {} of {}, retry={}", entry.getType(), url, code, retry);
      }
      if (retry) {
        //发送失败，需要重新发送到其他机器
        for (int i = 0; i < urlList.size(); i++) {
          String retryUrl = urlList.get(i);
          //已发送的URL不再重试
          if (url.equals(retryUrl)) {
            continue;
          }
          if (!isRetrySend(retryUrl, content)) {
            return;
          }
        }
      }
    }catch (Throwable e){
      log.error("the metrics report url list failed:{}",e.getMessage());
    }

  }


  private boolean isRetrySend(String url, String content) {
    boolean retry = false;
    try {
       HttpClientUtil
              .postDoPostURL(url, content.getBytes("UTF-8"));
    } catch (Throwable e) {
      log.error("error to send metrics to console ", e);
      if (!StringUtil.isEmptyString(e.getMessage()) && (e.getMessage().contains("拒绝连接") || e.getMessage().contains("Refuse"))) {
        retry = true;
      }
    }
    return retry;
  }
}
