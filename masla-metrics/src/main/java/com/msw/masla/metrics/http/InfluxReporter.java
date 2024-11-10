package com.msw.masla.metrics.http;

import com.msw.masla.common.monitor.metrics.MetricsEntry;
import com.msw.masla.metrics.frame.ScheduledReporter;
import lombok.extern.slf4j.Slf4j;

/**
 * 发送监控信息到InfluxDB
 */
@Slf4j
public class InfluxReporter extends ScheduledReporter {

  @Override
  public void send(MetricsEntry entry) {

  }
}
