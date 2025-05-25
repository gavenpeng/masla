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
import com.msw.masla.common.monitor.metrics.GlobalRequestFailedCount;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.metrics.frame.AbstractMetrics;
import com.msw.masla.protocol.http.netty.metrics.GlobalRequestFailedCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局访问失败统计
 *
 * @author jimmy.zhong
 */
public class GlobalRequestFailedMetrics extends AbstractMetrics {

  @Override
  public List getMetrics() {
    List<GlobalRequestFailedCount> globalRequestFailedCountList= new ArrayList<GlobalRequestFailedCount>(1);
    GlobalRequestFailedCount count = GlobalRequestFailedCounter.getRequestFailedCount();
    count.setTimestamp(getTimestamp());
    count.setHost(MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp());
    GlobalRequestFailedCounter.setRequestFailedCount(new GlobalRequestFailedCount());
    globalRequestFailedCountList.add(count);
    return globalRequestFailedCountList;
  }

}
