package com.msw.masla.metrics.frame;

import com.msw.masla.metrics.http.ApiMetrics;
import com.msw.masla.metrics.http.AppRequestFailedMetrics;
import com.msw.masla.metrics.http.ChannelPoolMetrics;
import com.msw.masla.metrics.http.DirectMemoryMetrics;
import com.msw.masla.metrics.http.DomainMetrics;
import com.msw.masla.metrics.http.DomainTotalMetrics;
import com.msw.masla.metrics.http.GlobalRequestFailedMetrics;
import com.msw.masla.metrics.http.GroupServerMertics;
import com.msw.masla.metrics.http.OpenSslStatsMetrics;
import com.msw.masla.metrics.http.SessionMetrics;
import com.msw.masla.metrics.http.TotalMetrics;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Metrics Reporter抽象实现类，所有需要收集的Metrics注册到Metrics Map中， 具体实现类可以通过{@link this#getMetricsData}
 * 获取到注册到Metrics数据
 */
@Slf4j
public abstract class AbstractReporter implements Reporter {

  protected final List<Metrics> metricsDataList = new ArrayList<Metrics>(6);

  AtomicInteger count = new AtomicInteger(0);

  public AbstractReporter() {
    registerMetric();
  }

  private void registerMetric() {
    Metrics sessionMetrics = new SessionMetrics();
    Metrics apiMetrics = new ApiMetrics();
    Metrics channelPoolMetrics = new ChannelPoolMetrics();
    Metrics appRequestFailedMetrics = new AppRequestFailedMetrics();
    Metrics domainMetrics = new DomainMetrics();
    Metrics globalRequestFailedMetrics = new GlobalRequestFailedMetrics();
    Metrics groupServerMetrics = GroupServerMertics.getInstances();
    Metrics totalMetrics = TotalMetrics.getInstance();
    Metrics openSslStatsMetrics = OpenSslStatsMetrics.getInstance();
    DomainTotalMetrics domainTotalMetrics = DomainTotalMetrics.getInstance();
    DirectMemoryMetrics directMemoryMetrics = DirectMemoryMetrics.getInstance();

    metricsDataList.add(apiMetrics);
    metricsDataList.add(sessionMetrics);
    metricsDataList.add(channelPoolMetrics);
    metricsDataList.add(appRequestFailedMetrics);
    metricsDataList.add(domainMetrics);
    metricsDataList.add(globalRequestFailedMetrics);
    //groupServerMetrics 需要放到最后
    metricsDataList.add(groupServerMetrics);
    metricsDataList.add(totalMetrics);
    metricsDataList.add(domainTotalMetrics);
    metricsDataList.add(directMemoryMetrics);
    metricsDataList.add(openSslStatsMetrics);

  }


  protected Map<String, List<Object>> getMetricsData() {
    long start = System.currentTimeMillis();
    Map<String, List<Object>> metricsData = new HashMap<String, List<Object>>();
    count.incrementAndGet();
    for(Metrics metricData:metricsDataList){
      long s = System.currentTimeMillis();
      try {
        if(count.get()%3==0){
          count.set(0);
        }
      }catch (Throwable e){
        log.error("Masla get {} mertic data failed:{}",metricData.getMetricsName(),e);
      }
      log.info("{} spends: {}", metricData.getMetricsName(), (System.currentTimeMillis() - s));
    }
    log.info("getMetricsData() spends: {}", (System.currentTimeMillis() - start));
    return metricsData;
  }


}
