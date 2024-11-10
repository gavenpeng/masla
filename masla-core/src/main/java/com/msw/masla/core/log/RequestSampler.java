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
