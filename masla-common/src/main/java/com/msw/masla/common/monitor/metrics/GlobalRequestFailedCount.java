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
package com.msw.masla.common.monitor.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

/**
 * 网关未转发给后端请求处理失败数据统计
 *
 * @author jimmy.zhong
 */

@Data
public class GlobalRequestFailedCount {

  public GlobalRequestFailedCount() {
    requestTooLarge = new AtomicLong(0);
    exceedMaxSession = new AtomicLong(0);
    globalBlack = new AtomicLong(0);
    urlNotFound = new AtomicLong(0);
    queueFull = new AtomicLong(0);
    urlNotFoundMap = new ConcurrentHashMap<String, AtomicInteger>();
  }

  private String group;

  private String host;


  /**
   * 请求过大禁止数量
   */
  private AtomicLong requestTooLarge;

  /**
   * 超出网关最大session设置数量
   */
  private AtomicLong exceedMaxSession;


  /**
   * 全局黑名单禁止访问数量
   */
  private AtomicLong globalBlack;

  /**
   * 未找到配置的转发路径数量
   */
  private AtomicLong urlNotFound;

  private ConcurrentHashMap<String, AtomicInteger> urlNotFoundMap;

  /**
   * 队列打满
   */
  private AtomicLong queueFull;

  private long timestamp;

}
