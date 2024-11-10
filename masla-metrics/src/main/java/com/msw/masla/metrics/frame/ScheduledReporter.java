package com.msw.masla.metrics.frame;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.msw.masla.common.monitor.metrics.MetricsEntry;
import com.msw.masla.metrics.frame.AbstractReporter;
import com.msw.masla.metrics.frame.AsyncAppAgregateAdminReporter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Metrics数据定时上报reporter
 */
@Slf4j
public abstract class ScheduledReporter extends AbstractReporter {


  /**
   * 上报任务默认初始延迟执行时间
   */
  private static final int DEFAULT_INITIAL_DELAY = 30;

  /**
   * 监控数据上报时间间隔
   */
  private static final int DEFAULT_REPROT_PERIOD = 30;

  private static final int ACCURATE_REPORT_PERIOD = 10;

  /**
   * 上报数据线程池
   */
  private final ScheduledExecutorService executor;


  /**
   * Metrics数据批量上报最大数量，数量过大会导致单次请求过大
   */
  private static final int BATCH_SIZE = 500000;


  /**
   *
   * @param executor
   */
  public ScheduledReporter(ScheduledExecutorService executor) {
    this.executor = executor == null ? createDefaultExecutor() : executor;
  }

  /**
   *
   */
  public ScheduledReporter() {
    this(null);
  }


  public void start(long initialDelay, long period, TimeUnit unit, Runnable runnable) {

    executor.scheduleAtFixedRate(runnable, initialDelay, period, unit);
  }

  public void start() {

    start(DEFAULT_INITIAL_DELAY, ACCURATE_REPORT_PERIOD, TimeUnit.SECONDS);
  }

  public void start(long initialDelay, long period, TimeUnit unit) {
    start(initialDelay, period, unit, new Runnable() {
      @Override
      public void run() {
        try {
            report();
        } catch (Throwable ex) {
          log.error("Exception thrown from {}.report()",
              AsyncAppAgregateAdminReporter.class.getSimpleName(), ex);
        }
      }
    });
  }


  @Override
  public void report() {

    Map<String, List<Object>> metricsDataMap = getMetricsData();

    for (Map.Entry<String, List<Object>> entry : metricsDataMap.entrySet()) {

      if (entry.getValue() == null || entry.getValue().isEmpty()) {
        continue;
      }

      if (entry.getValue().size() < BATCH_SIZE) {
        send(new MetricsEntry(entry.getKey(), entry.getValue()));
      } else {
        int size = entry.getValue().size();
        for (int i = 0; i < size; i += 50) {
          send(new MetricsEntry(entry.getKey(),
              entry.getValue().subList(i, Math.min(size, i + BATCH_SIZE))));
        }
      }

    }
  }


  public abstract void send(MetricsEntry entry);


  public void stop() {
    executor.shutdown();
  }


  private static ScheduledExecutorService createDefaultExecutor() {
    return Executors.newSingleThreadScheduledExecutor(
        new ThreadFactoryBuilder().setNameFormat("metrics-reporter-thread-%d").build());
  }


}
