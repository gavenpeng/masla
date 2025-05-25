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
 * HTTP请求/响应大小分布统计
 *
 * 请求/响应大小统计区间:
 * 0KB    < 请求/响应大小 <= 200KB
 * 200KB  < 请求/响应大小 <= 500KB
 * 500KB  < 请求/响应大小 <= 1000KB
 * 1000KB < 请求/响应大小
 *
 * @author jimmy.zhong
 */
@Data
public class BodySectionCount {

  public BodySectionCount() {
    this.section1 = new AtomicLong(0);
    this.section2 = new AtomicLong(0);
    this.section3 = new AtomicLong(0);
    this.section4 = new AtomicLong(0);
  }


  private String group;

  private String appName;

  private String host;

  private long timestamp;

  /**
   * 0KB    < 请求/响应 <= 1KB
   */
  private AtomicLong section1;

  /**
   * 1KB  < 请求/响应 <= 5KB
   */
  private AtomicLong section2;

  /**
   * 5KB  < 请求/响应 <= 20KB
   */
  private AtomicLong section3;

  /**
   * 20KB < 请求/响应
   */
  private AtomicLong section4;

}
