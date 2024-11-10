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
