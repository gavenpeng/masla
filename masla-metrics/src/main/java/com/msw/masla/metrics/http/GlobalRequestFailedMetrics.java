package com.msw.masla.metrics.http;

import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.monitor.metrics.GlobalRequestFailedCount;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.metrics.frame.AbstractMetrics;
import com.msw.masla.protocol.http.netty.metrics.GlobalRequestFailedCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局访问失败统计
 *
 * @author jimmy.zhong
 */
public class GlobalRequestFailedMetrics extends AbstractMetrics {

  @Override
  public List getMetrics() {
    List<GlobalRequestFailedCount> globalRequestFailedCountList= new ArrayList<GlobalRequestFailedCount>(1);
    GlobalRequestFailedCount count = GlobalRequestFailedCounter.getRequestFailedCount();
    count.setTimestamp(getTimestamp());
    count.setHost(MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp());
    GlobalRequestFailedCounter.setRequestFailedCount(new GlobalRequestFailedCount());
    globalRequestFailedCountList.add(count);
    return globalRequestFailedCountList;
  }

}
