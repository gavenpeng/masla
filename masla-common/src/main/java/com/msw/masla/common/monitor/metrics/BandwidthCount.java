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
 * 带宽使用情况统计
 *
 * @author jimmy.zhong
 */
@Data
public class BandwidthCount {


  public BandwidthCount() {
    this.inLineBWCount = new AtomicLong(0);
    this.inHeaderBWCount = new AtomicLong(0);
    this.inBodyBWCount = new AtomicLong(0);

    this.outLineBWCount = new AtomicLong(0);
    this.outHeaderBWCount = new AtomicLong(0);
    this.outBodyBWCount = new AtomicLong(0);



    lastActiveTime = System.currentTimeMillis();
  }

  private String group;

  private String appName;

  private String host;

  private String serviceId;

  private long timestamp;
  //入站带宽统计
  private AtomicLong inLineBWCount;
  private AtomicLong inHeaderBWCount;
  private AtomicLong inBodyBWCount;


  //出站带宽统计
//  private AtomicLong outBandwidthCount;

  private AtomicLong outLineBWCount;
  private AtomicLong outHeaderBWCount;
  private AtomicLong outBodyBWCount;
  private long lastActiveTime;

}
