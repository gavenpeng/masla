package com.msw.masla.metrics.http;

import com.msw.masla.common.monitor.metrics.ChannelPoolCount;
import com.msw.masla.metrics.frame.AbstractMetrics;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用后端连接池数据监控
 */
@Slf4j
public class ChannelPoolMetrics extends AbstractMetrics {

  @Override
  public List getMetrics() {

    List<ChannelPoolCount> channelPoolMetricsList = new ArrayList<ChannelPoolCount>();
    return channelPoolMetricsList;
  }





}
