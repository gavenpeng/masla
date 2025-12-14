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

import com.msw.masla.common.config.MaslaServerConfig;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Prometheus Metrics配置类
 * 配置Micrometer的Prometheus Registry
 * 可通过 masla.metrics.prometheus.enabled 配置启用/禁用（默认启用）
 */
@Slf4j
@Configuration
public class PrometheusMetricsConfig {

    @Autowired
    private MaslaServerConfig maslaServerConfig;
    /**
     * 创建Prometheus MeterRegistry
     */
    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        log.info("Prometheus MeterRegistry initialized");
        return registry;
    }

}

