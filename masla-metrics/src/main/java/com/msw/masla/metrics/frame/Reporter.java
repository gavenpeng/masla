package com.msw.masla.metrics.frame;

/**
 * 监控数据上报Reporter
 */
public interface Reporter extends Cloneable {

  /**
   * 上报数据
   */
  void report();

  /**
   * 开始上报数据，主要针对定时上报任务
   */
  void start();

  /**
   * 停止上报数据，主要针对定时上报任务
   */
  void stop();


}
