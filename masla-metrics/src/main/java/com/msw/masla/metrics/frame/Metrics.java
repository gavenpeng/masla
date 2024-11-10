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
