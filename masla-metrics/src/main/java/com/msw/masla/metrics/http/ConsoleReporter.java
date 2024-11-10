package com.msw.masla.metrics.http;

import com.alibaba.fastjson.JSON;
import com.msw.masla.common.monitor.metrics.MetricsEntry;
import com.msw.masla.metrics.frame.ScheduledReporter;

/**
 * 打印监控信息到控制台，主要用于调试目的
 */
public class ConsoleReporter extends ScheduledReporter {


  @Override
  public void send(MetricsEntry entry) {

    System.out.println(JSON.toJSONString(entry, true));
  }
}
