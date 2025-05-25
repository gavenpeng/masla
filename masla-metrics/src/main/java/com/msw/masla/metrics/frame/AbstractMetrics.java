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

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class AbstractMetrics implements Metrics {

  /**
   * Metrics数据时间间隔
   */
  public static final int METRIC_TASK_INTERVAL = 1000 * 30;

  protected AtomicInteger count;

  @Override
  public String getMetricsName() {
    return this.getClass().getSimpleName();
  }

  /**
   * 获取监控数据类型，默认使用Metrics具体实现类名称作为类型
   *
   * @return 监控数据类型
   */
  public String getType() {
    return this.getClass().getSimpleName();
  }

  /**
   * 获取监控数据时间戳，现在为30s时间间隔上传一次数据
   *
   * @return 时间戳
   */
  public long getTimestamp() {

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MILLISECOND, 0);
    int second = calendar.get(Calendar.SECOND);
    if (second < 30) {
      calendar.set(Calendar.SECOND, 0);
    }else {
      calendar.set(Calendar.SECOND, 30);
    }
    return calendar.getTimeInMillis();
  }

  /**
   * 获取ApiMetrics监控数据时间戳，现在为10s时间间隔上传一次数据
   *
   * @return 时间戳
   */
  public long getApiMetricsTimestamp() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MILLISECOND, 0);
    int second = calendar.get(Calendar.SECOND);
    calendar.set(Calendar.SECOND, (second/10)*10);
    return calendar.getTimeInMillis();
  }

  @Override
  public void setCount(AtomicInteger count) {
    this.count = count;
  }
}
