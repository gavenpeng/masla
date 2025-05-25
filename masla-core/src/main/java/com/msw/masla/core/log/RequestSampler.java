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
package com.msw.masla.core.log;

import com.msw.masla.common.util.AtomicPositiveInteger;
import com.msw.masla.common.enums.ErrorType;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求信息采样
 *
 * 根据设定的采样率进行采样，控制采样时间间隔，防止流量过大时采样日志过多
 *
 * @author gavin.peng
 */
public class RequestSampler {

  private volatile int denominator;

  private AtomicLong lastSampleTime;

  private ErrorType errorType;

  /**
   * 采样最小时间间隔
   */
  private static final int SAMPLE_MINIMUM_INTERVAL = 10;

  /**
   * 默认采样率值
   */
  private static final int DEFAULT_RATIO = 100;

  private AtomicPositiveInteger count;

  public RequestSampler(ErrorType errorType) {

    this.errorType = errorType;

    count = new AtomicPositiveInteger(0);
    lastSampleTime = new AtomicLong(0);
  }

  public RequestSampler() {
    count = new AtomicPositiveInteger(0);
    lastSampleTime = new AtomicLong(0);
  }


}
