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
package com.msw.masla.protocol.http.netty.metrics;

import com.msw.masla.common.monitor.metrics.GlobalRequestFailedCount;

/**
 * 全局访问失败统计
 *
 * @author jimmy.zhong
 */
public class GlobalRequestFailedCounter {

  private static volatile GlobalRequestFailedCount requestFailedCount = new GlobalRequestFailedCount();

  public static GlobalRequestFailedCount getRequestFailedCount() {
    return requestFailedCount;
  }

  public static void setRequestFailedCount(
      GlobalRequestFailedCount requestFailedCount) {
    GlobalRequestFailedCounter.requestFailedCount = requestFailedCount;
  }
}
