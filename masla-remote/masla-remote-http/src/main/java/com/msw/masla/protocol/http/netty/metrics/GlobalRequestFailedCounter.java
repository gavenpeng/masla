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
