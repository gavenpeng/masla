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
package com.msw.masla.metrics.http;

import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.monitor.metrics.OpenSslStatsCount;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.metrics.frame.AbstractMetrics;
import com.msw.masla.protocol.http.netty.ssl.AbstractHttp2SslChannelInitializer;

import java.util.ArrayList;
import java.util.List;

public class OpenSslStatsMetrics extends AbstractMetrics {

    private static class OpenSslStatsMetricsHolder {
        private static OpenSslStatsMetrics instance = new OpenSslStatsMetrics();
    }

    public static OpenSslStatsMetrics getInstance() {
        return OpenSslStatsMetricsHolder.instance;
    }

    @Override
    public List getMetrics() {
        long timestamp = getTimestamp();
        List<OpenSslStatsCount> list = new ArrayList<OpenSslStatsCount>(1);
        OpenSslStatsCount count = AbstractHttp2SslChannelInitializer.getOpenSslStats();
        count.setTimestamp(timestamp);
        count.setHost(MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp());
        list.add(count);
        //OpenSslStatsCount.clear();
        return list;
    }
}
