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
import com.msw.masla.common.config.MaslaServerConfig;
import com.msw.masla.common.monitor.metrics.MetricsEntry;
import com.msw.masla.common.util.HttpClientUtil;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.metrics.http.MetricConfConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class AdminReporter extends ScheduledReporter {

  private static String HTTP_PROTOCOL = "http://";

  private static String monitorPath = "/masla-console/v2/metricStatic";

  private static Random random = new Random();

  private AtomicInteger position = new AtomicInteger(random.nextInt(1));

  protected static volatile List<String> urlList = new ArrayList<String>(2);

  public AdminReporter(ScheduledExecutorService executor) {
    super(executor);
  }

  public AdminReporter() {
    super();
    init();
  }

  public void init() {
    fetchLatestAdminList();
    if (urlList.size() == 0) {
      //配置中心没有，则读本地的配置文件
      MaslaServerConfig maslaConfig = (MaslaServerConfig) MaslaSpringContextUtil.getBean("maslaConfig");
      if (maslaConfig != null) {
        String adminServerAddress = maslaConfig.getMaslaCollectorServerAddress();
        log.warn("Masla get console server address {} from local", adminServerAddress);
        urlList.add(HTTP_PROTOCOL + adminServerAddress + monitorPath);
      }
    }
  }

  public static void fetchLatestAdminList() {
    String serverUrls = MetricConfConstants.getMaslaAdminServerAddess();
    List<String> list = new ArrayList<String>();
    if (!StringUtil.isEmptyString(serverUrls)) {
      String[] adminIps = serverUrls.split(";");
      for (String ip : adminIps) {
        list.add(HTTP_PROTOCOL + ip + monitorPath);
      }
      urlList = list;
    }
  }

  public void sentAppMetric(String content) {
    sendData(urlList,content);
  }


  private void sendData(List<String> urlList,String content){
    try {
      if (urlList.isEmpty()) {
        log.warn("the metrics report url list is empty, try to reload it again");
        return;
      }

      for (int i = 0; i < urlList.size(); i++) {
        String response = HttpClientUtil
                .postDoPostURL(getUrl(urlList), content.getBytes("UTF-8"));
        if (response != null) {
          break;
        }
      }
    } catch (Throwable e) {
      log.error("error to send metrics to console ", e);
    }
  }


  public String getUrl(List<String> urlList) {
    if (urlList != null) {
      int totalSize = urlList.size();
      if (totalSize > 0) {
        int key = position.getAndIncrement();
        int realPos = key % totalSize;
        if (key > 10000) {
          position.set(0);
        }
        return urlList.get(realPos);
      }
    }
    return null;
  }



  @Override
  public void send(MetricsEntry entry) {
    sentAppMetric(JSON.toJSONString(entry));
  }
}
