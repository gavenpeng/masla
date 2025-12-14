/*
 * Copyright 2025 msw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.msw.masla.metrics.prometheus;

import com.msw.masla.common.monitor.metrics.SessionCount;
import com.msw.masla.common.pojo.ApiMetric;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.pojo.ServiceAppCache;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.HdrHistogram.Histogram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Prometheus Metrics导出器
 * 使用Micrometer的Gauge Supplier方式，在Prometheus采集时自动读取最新值
 * 注意：直接读取ApiMetric的当前值，不会触发getMetrics()的重置逻辑
 * 可通过 masla.metrics.prometheus.enabled 配置启用/禁用（默认启用）
 */
@Slf4j
@Component("prometheusMetricsExporter")
public class PrometheusMetricsExporter {

    @Autowired
    private MeterRegistry meterRegistry;

    // 存储已注册的Gauge，避免重复注册
    private final Map<String, Gauge> registeredGauges = new ConcurrentHashMap<>();

    public void init() {
        log.info("PrometheusMetricsExporter initialized - metrics will be read on-demand when Prometheus scrapes");
        // 初始化时注册所有指标（使用Supplier实时读取）
        registerAllMetrics();
    }

    /**
     * 注册所有指标
     * 使用Gauge的Supplier方式，每次Prometheus采集时自动调用获取最新值
     */
    private void registerAllMetrics() {
        // 注册一个定时刷新任务，动态发现新的指标并注册
        // 但这里我们使用更简单的方式：在初始化时注册，使用Supplier闭包捕获ApiMetric引用
        // 这样每次Prometheus采集时会自动读取最新值
        
        // 注意：由于指标是动态的，我们使用一个特殊的Gauge来触发动态注册
        // 每次Prometheus采集时会调用这个Supplier，内部会动态注册所有指标
        Gauge.builder("masla_metrics_registry", this, PrometheusMetricsExporter::refreshAndRegisterMetrics)
                .description("Masla metrics registry - dynamically registers all metrics on scrape")
                .register(meterRegistry);
    }

    /**
     * 刷新并注册所有指标
     * 这个方法会在每次Prometheus采集时被调用
     * 也可以被外部手动调用来刷新指标
     */
    public double refreshAndRegisterMetrics() {
        try {
            // 收集并注册API指标
            registerApiMetrics();
            // 收集并注册Session指标
            registerSessionMetrics();
        } catch (Exception e) {
            log.error("Failed to refresh metrics for Prometheus", e);
        }
        return 1.0; // 返回固定值，这个Gauge只是用来触发刷新
    }

    /**
     * 注册API指标
     * 使用Supplier闭包捕获ApiMetric引用，每次采集时读取最新值
     */
    private void registerApiMetrics() {
        //RouteRuleCache.
        Map<String, ServiceApp> appDOMap = ServiceAppCache.getAppCache();
        if (appDOMap == null || appDOMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, ServiceApp> entry : appDOMap.entrySet()) {
            ServiceApp appDO = entry.getValue();
            if (appDO == null) {
                continue;
            }

            // 遍历每个host的serviceId
            for (Map.Entry<String, ConcurrentHashMap<String, ApiMetric>> hostEntry : 
                    appDO.getAppHostServiceIdMetricMap().entrySet()) {
                String host = hostEntry.getKey();
                Map<String, ApiMetric> apiMetricMap = hostEntry.getValue();
                
                if (apiMetricMap == null || apiMetricMap.isEmpty()) {
                    continue;
                }

                for (Map.Entry<String, ApiMetric> metricEntry : apiMetricMap.entrySet()) {
                    String serviceId = metricEntry.getKey();
                    ApiMetric metric = metricEntry.getValue();
                    if (metric == null) {
                        continue;
                    }

                    String appId = String.valueOf(appDO.getId());
                    String appName = appDO.getName() != null ? appDO.getName() : "unknown";

                    Tags tags = Tags.of(
                            "service_name", appName,
                            "service_id", serviceId,
                            "host", host != null ? host : "unknown"
                    );

                    // 使用Supplier闭包捕获ApiMetric引用，每次采集时读取最新值
                    registerGaugeWithSupplier("masla_api_qps_total", tags, () -> (double) metric.getQps().get());
                    registerGaugeWithSupplier("masla_api_peak_qps_total", tags, () -> (double) metric.getPeakQps().get());
                    registerGaugeWithSupplier("masla_api_success_total", tags, () -> (double) metric.getSuccessNums().get());
                    registerGaugeWithSupplier("masla_api_timeout_total", tags, () -> (double) metric.getTimeoutNums().get());
                    registerGaugeWithSupplier("masla_api_reject_total", tags, () -> (double) metric.getRejectNums().get());
                    registerGaugeWithSupplier("masla_api_slow_total", tags, () -> (double) metric.getSlowNums().get());
                    registerGaugeWithSupplier("masla_api_exception_total", tags, () -> (double) metric.getExceptionNums().get());
                    registerGaugeWithSupplier("masla_api_4xx_total", tags, () -> (double) metric.getFourXXCodeNums().get());
                    registerGaugeWithSupplier("masla_api_5xx_total", tags, () -> (double) metric.getFiveXXCodeNums().get());
                    registerGaugeWithSupplier("masla_api_400_total", tags, () -> (double) metric.getCode400().get());
                    registerGaugeWithSupplier("masla_api_401_total", tags, () -> (double) metric.getCode401().get());
                    registerGaugeWithSupplier("masla_api_404_total", tags, () -> (double) metric.getCode404().get());
                    registerGaugeWithSupplier("masla_api_conn_closed_total", tags, () -> (double) metric.getConnClosedNums().get());
                    registerGaugeWithSupplier("masla_api_conn_refused_total", tags, () -> (double) metric.getConnRefusedNums().get());
                    registerGaugeWithSupplier("masla_api_conn_reset_total", tags, () -> (double) metric.getConnResetNums().get());
                    registerGaugeWithSupplier("masla_api_conn_timeout_total", tags, () -> (double) metric.getConnTimeoutNums().get());
                    registerGaugeWithSupplier("masla_api_conn_pool_full_reject_total", tags, () -> (double) metric.getConnPoolFullRejectNums().get());
                    registerGaugeWithSupplier("masla_api_conn_pool_wait_timeout_total", tags, () -> (double) metric.getConnPoolWaitTimeoutNums().get());

                    // 带宽指标
                    registerGaugeWithSupplier("masla_api_in_bandwidth_bytes", tags, () -> (double) metric.getInBandWidth());
                    registerGaugeWithSupplier("masla_api_out_bandwidth_bytes", tags, () -> (double) metric.getOutBandWidth());

                    // 延迟指标（从Histogram读取）
                    ConcurrentHashMap<String, Histogram> hostTP90Map = 
                            appDO.getAppHostServiceIdTP90Map().get(host);
                    if (hostTP90Map != null) {
                        Histogram histogram = hostTP90Map.get(serviceId);
                        if (histogram != null && histogram.getTotalCount() > 0) {
                            registerGaugeWithSupplier("masla_api_latency_tp50_ms", tags, () -> (double) histogram.getValueAtPercentile(50));
                            registerGaugeWithSupplier("masla_api_latency_tp90_ms", tags, () -> (double) histogram.getValueAtPercentile(90));
                            registerGaugeWithSupplier("masla_api_latency_tp99_ms", tags, () -> (double) histogram.getValueAtPercentile(99));
                            registerGaugeWithSupplier("masla_api_latency_tp999_ms", tags, () -> (double) histogram.getValueAtPercentile(99.9));
                            registerGaugeWithSupplier("masla_api_latency_tp9999_ms", tags, () -> (double) histogram.getValueAtPercentile(99.99));
                            registerGaugeWithSupplier("masla_api_latency_max_ms", tags, () -> (double) histogram.getMaxValue());
                            registerGaugeWithSupplier("masla_api_latency_min_ms", tags, () -> (double) histogram.getMinValue());
                        }
                    }

                    // 平均耗时（需要实时计算）
                    registerGaugeWithSupplier("masla_api_server_cost_avg_ms", tags, () -> {
                        long qps = metric.getQps().get();
                        if (qps > 0) {
                            return (double) metric.getServerCost().get() / qps;
                        }
                        return 0.0;
                    });
                    registerGaugeWithSupplier("masla_api_acquire_cost_avg_ms", tags, () -> {
                        long qps = metric.getQps().get();
                        if (qps > 0) {
                            return (double) metric.getAcquireCost().get() / qps;
                        }
                        return 0.0;
                    });
                    registerGaugeWithSupplier("masla_api_push_cost_avg_ms", tags, () -> {
                        long qps = metric.getQps().get();
                        if (qps > 0) {
                            return (double) metric.getPushCost().get() / qps;
                        }
                        return 0.0;
                    });
                }
            }
        }
    }

    /**
     * 注册Session指标
     */
    private void registerSessionMetrics() {
        try {
            com.msw.masla.metrics.http.SessionMetrics sessionMetrics = new com.msw.masla.metrics.http.SessionMetrics();
            List<SessionCount> sessionCountList = sessionMetrics.getMetrics();
            if (sessionCountList == null || sessionCountList.isEmpty()) {
                return;
            }

            for (Object obj : sessionCountList) {
                if (obj instanceof com.msw.masla.common.monitor.metrics.SessionCount) {
                    com.msw.masla.common.monitor.metrics.SessionCount sessionCount = 
                            (com.msw.masla.common.monitor.metrics.SessionCount) obj;
                    
                    Tags tags = Tags.of("type", "masla");
                    
                    // 注意：SessionCount是一次性读取的，所以这里需要重新创建SessionMetrics来获取最新值
                    registerGaugeWithSupplier("masla_session_total", tags, () -> {
                        com.msw.masla.metrics.http.SessionMetrics sm = new com.msw.masla.metrics.http.SessionMetrics();
                        List<SessionCount> list = sm.getMetrics();
                        if (list != null && !list.isEmpty() && list.get(0) instanceof com.msw.masla.common.monitor.metrics.SessionCount) {
                            return (double) ((com.msw.masla.common.monitor.metrics.SessionCount) list.get(0)).getCount();
                        }
                        return 0.0;
                    });
                    registerGaugeWithSupplier("masla_session_https1_total", tags, () -> {
                        com.msw.masla.metrics.http.SessionMetrics sm = new com.msw.masla.metrics.http.SessionMetrics();
                        List<SessionCount> list = sm.getMetrics();
                        if (list != null && !list.isEmpty() && list.get(0) instanceof com.msw.masla.common.monitor.metrics.SessionCount) {
                            return (double) ((com.msw.masla.common.monitor.metrics.SessionCount) list.get(0)).getHttps1SessionNum();
                        }
                        return 0.0;
                    });
                    registerGaugeWithSupplier("masla_session_https2_total", tags, () -> {
                        com.msw.masla.metrics.http.SessionMetrics sm = new com.msw.masla.metrics.http.SessionMetrics();
                        List<SessionCount> list = sm.getMetrics();
                        if (list != null && !list.isEmpty() && list.get(0) instanceof com.msw.masla.common.monitor.metrics.SessionCount) {
                            return (double) ((com.msw.masla.common.monitor.metrics.SessionCount) list.get(0)).getHttps2SessionNum();
                        }
                        return 0.0;
                    });
                }
            }
        } catch (Exception e) {
            log.error("Failed to register session metrics", e);
        }
    }

    /**
     * 使用Supplier方式注册Gauge
     * 每次Prometheus采集时会自动调用Supplier获取最新值
     */
    private void registerGaugeWithSupplier(String name, Tags tags, Supplier<Double> valueSupplier) {
        String key = name + tags.toString();
        
        // 如果已经注册过，直接返回（避免重复注册）
        if (registeredGauges.containsKey(key)) {
            return;
        }

        // 注册Gauge，使用Supplier实时读取
        Gauge gauge = Gauge.builder(name, valueSupplier, Supplier::get)
                .tags(tags)
                .description("Masla API metric: " + name)
                .register(meterRegistry);
        
        registeredGauges.put(key, gauge);
    }
}
