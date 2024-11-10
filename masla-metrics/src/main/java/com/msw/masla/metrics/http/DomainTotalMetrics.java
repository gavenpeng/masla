package com.msw.masla.metrics.http;

import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.monitor.metrics.DomainCount;
import com.msw.masla.common.monitor.metrics.DomainTotalCount;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.metrics.frame.AbstractMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DomainTotalMetrics extends AbstractMetrics {

    private static class DomainTotalMetricsHolder {
        private static DomainTotalMetrics instance = new DomainTotalMetrics();
    }

    public static DomainTotalMetrics getInstance() {
        return DomainTotalMetricsHolder.instance;
    }

    private static ConcurrentHashMap<String, DomainTotalCount> domainTotalCountMap = new ConcurrentHashMap<String, DomainTotalCount>();

    @Override
    public List getMetrics() {
        long timestamp = getTimestamp();
        List<DomainTotalCount> domainTotalCounts =  new ArrayList<DomainTotalCount>(domainTotalCountMap.size());

        for (DomainTotalCount domainTotalCount : domainTotalCountMap.values()) {
            domainTotalCount.setTimestamp(timestamp);
            domainTotalCount.setHost(MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp());
            domainTotalCounts.add(domainTotalCount);
        }

        domainTotalCountMap.clear();
        return domainTotalCounts;
    }

    public void convert2DomainCount(DomainCount count) {

        String dc = count.getDc() == null ? "unknown" : count.getDc();
        DomainTotalCount domainTotalCount = getDomainTotalCount(dc);
        domainTotalCount.setDc(dc);
        domainTotalCount.getQueryCount().addAndGet(count.getQueryCount().get());
        domainTotalCount.getFlowControlNums().addAndGet(count.getFlowControlNums().get());
        domainTotalCount.getIpv6Qps().addAndGet(count.getIpv6Qps().get());
        domainTotalCount.getInBandwidth().addAndGet(count.getInBandwidth().get());
        domainTotalCount.getOutBandwidth().addAndGet(count.getOutBandwidth().get());
        domainTotalCount.getIosH2Qps().addAndGet(count.getIosH2Qps().get());
        domainTotalCount.getIosHTTPSQps().addAndGet(count.getIosHTTPSQps().get());
        domainTotalCount.getIosQps().addAndGet(count.getIosQps().get());
        domainTotalCount.getIosReQps().addAndGet(count.getIosReQps().get());
        domainTotalCount.getAndroidQps().addAndGet(count.getAndroidQps().get());
        domainTotalCount.getAndroidH2Qps().addAndGet(count.getAndroidH2Qps().get());
        domainTotalCount.getAndroidHTTPSQps().addAndGet(count.getAndroidHTTPSQps().get());
        domainTotalCount.getAndroidReQps().addAndGet(count.getAndroidReQps().get());
        domainTotalCount.getIosInBandWidth().addAndGet(count.getIosInBandWidth().get());
        domainTotalCount.getAndroidInBandWidth().addAndGet(count.getAndroidInBandWidth().get());
        domainTotalCount.getWpInBandWidth().addAndGet(count.getWpInBandWidth().get());
        domainTotalCount.getWpQps().addAndGet(count.getWpQps().get());
        domainTotalCount.getWpReQps().addAndGet(count.getWpReQps().get());
        domainTotalCount.getIosWatchQps().addAndGet(count.getIosWatchQps().get());
        domainTotalCount.getIosWatchInBandWidth().addAndGet(count.getIosWatchInBandWidth().get());
        domainTotalCount.getIosPadQps().addAndGet(count.getIosPadQps().get());
        domainTotalCount.getIosPadInBandWidth().addAndGet(count.getIosPadInBandWidth().get());
        domainTotalCount.setExtendKeyMap(count);
    }

    private DomainTotalCount getDomainTotalCount(String dc) {
        DomainTotalCount domainTotalCount = domainTotalCountMap.get(dc);
        if (null == domainTotalCount) {
            domainTotalCount = new DomainTotalCount();
            DomainTotalCount preDomainTotalCount = domainTotalCountMap.putIfAbsent(dc, domainTotalCount);
            if (preDomainTotalCount != null) {
                domainTotalCount = preDomainTotalCount;
            }
        }
        return domainTotalCount;
    }

}
