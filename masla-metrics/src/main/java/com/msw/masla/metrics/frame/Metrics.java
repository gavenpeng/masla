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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 监控数据接口，各种监控数据的获取统一实现次接口，在 {@link Reporter}中进行统一处理上报
 */
public interface Metrics<T> {

  /**
   * 获取监控数据列表
   *
   * @return 监控数据列表
   */
  List<T> getMetrics();

  /**
   * 获取监控数据类型名称
   *
   * @return 监控数据类型名称
   */
  String getMetricsName();

  void setCount(AtomicInteger count);
}
