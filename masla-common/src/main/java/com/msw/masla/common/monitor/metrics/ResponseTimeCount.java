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

import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

/**
 * 响应时间分布统计
 *
 * 响应时间 <= 200ms
 * 200ms   < 响应时间 <= 500ms
 * 500ms   < 响应时间 <=1000ms
 * 1000ms  < 响应时间 <=2000ms
 * 响应时间 > 2000ms
 *
 * @author jimmy.zhong
 */
@Data
public class ResponseTimeCount {



  public ResponseTimeCount() {
    this.section1 = new AtomicLong(0);
    this.section2 = new AtomicLong(0);
    this.section3 = new AtomicLong(0);
    this.section4 = new AtomicLong(0);
    this.section5 = new AtomicLong(0);
  }

  private String group;

  private String appName;

  private String host;

  private long timestamp;

  /**
   * 响应时间 <= 200ms
   */
  private AtomicLong section1;

  /**
   * 200ms   < 响应时间 <= 500ms
   */
  private AtomicLong section2;

  /**
   * 500ms   < 响应时间 <=1000ms
   */
  private AtomicLong section3;

  /**
   * 1000ms  < 响应时间 <=2000ms
   */
  private AtomicLong section4;

  /**
   * 响应时间 > 2000ms
   */
  private AtomicLong section5;

}
