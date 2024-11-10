package com.msw.masla.common.monitor.metrics;

import lombok.Data;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 域名及nginx带宽,QPS全局统计
 */
@Data
public class DomainTotalCount {

    public static final String IOS = "os";
    public static final String ANDROIDS = "android";
    public static final String WINDOW = "wp";

    // ios_watch
    public static final String IOS_WATCH = "ios_watch";

    // ios_pad
    public static final String IOS_PAD = "ios_pad";

    public DomainTotalCount() {
        inBandwidth = new AtomicLong(0);
        outBandwidth = new AtomicLong(0);
        queryCount = new AtomicLong(0);
        flowControlNums = new AtomicLong(0);
        ipv6Qps = new AtomicLong(0);
        iosQps = new AtomicLong(0);
        iosH2Qps = new AtomicLong(0);
        iosHTTPSQps = new AtomicLong(0);
        androidQps = new AtomicLong(0);
        androidH2Qps = new AtomicLong(0);
        androidHTTPSQps = new AtomicLong(0);
        iosInBandWidth = new AtomicLong(0);
        androidInBandWidth = new AtomicLong(0);
        wpQps = new AtomicLong(0);
        wpInBandWidth = new AtomicLong(0);
        iosWatchQps = new AtomicLong(0);
        iosWatchInBandWidth = new AtomicLong(0);
        iosPadQps = new AtomicLong(0);
        iosPadInBandWidth = new AtomicLong(0);
        iosReQps = new AtomicLong(0);
        androidReQps = new AtomicLong(0);
        wpReQps = new AtomicLong(0);
        //支持自定义扩展字段，方便统计新的头时，网关不需要开发
        extendKeyMap = new ConcurrentHashMap<String, AtomicLong>(4);
    }

    /**
     * 机房
     */
    private String dc;

    /**
     * 入站带宽
     */
    private AtomicLong inBandwidth;

    /**
     * 出站带宽
     */
    private AtomicLong outBandwidth;

    /**
     * 访问量
     */
    private AtomicLong queryCount;

    /**
     * 流控次数
     */
    private AtomicLong flowControlNums;

    private AtomicLong ipv6Qps;

    //ios的qps
    private AtomicLong iosQps;
    private AtomicLong iosH2Qps;
    private AtomicLong iosHTTPSQps;
    private AtomicLong iosInBandWidth;

    //android的qps
    private AtomicLong androidQps;
    private AtomicLong androidH2Qps;
    private AtomicLong androidHTTPSQps;
    private AtomicLong androidInBandWidth;

    //windowPhone的qps
    private AtomicLong wpQps;
    private AtomicLong wpInBandWidth;

    // iosWatch的qps
    private AtomicLong iosWatchQps;
    private AtomicLong iosWatchInBandWidth;

    // iosPad的pqs
    private AtomicLong iosPadQps;
    private AtomicLong iosPadInBandWidth;

    //android和ios的重试qps
    private AtomicLong iosReQps;
    private AtomicLong androidReQps;
    private AtomicLong wpReQps;

    private ConcurrentHashMap<String,AtomicLong> extendKeyMap;

    private String group;

    private long timestamp;

    /**
     * 网关机器
     */
    private String host;

    public void setExtendKeyMap(DomainCount domainCount){
        for (Map.Entry<String, AtomicLong> entry : domainCount.getExtendKeyMap().entrySet()) {
            String key = entry.getKey();
            AtomicLong atomicLong = this.getExtendKeyMap().get(key);
            if (null == atomicLong) {
                atomicLong = entry.getValue();
            } else {
                atomicLong.addAndGet(entry.getValue().get());
            }
            this.getExtendKeyMap().put(key, atomicLong);
        }
    }

    public void setExtendKeyMap(DomainTotalCount domainCount){
        for (Map.Entry<String, AtomicLong> entry : domainCount.getExtendKeyMap().entrySet()) {
            String key = entry.getKey();
            AtomicLong atomicLong = this.getExtendKeyMap().get(key);
            if (null == atomicLong) {
                atomicLong = entry.getValue();
            } else {
                atomicLong.addAndGet(entry.getValue().get());
            }
            this.getExtendKeyMap().put(key, atomicLong);
        }
    }

    public void setExtendKeyMap(ConcurrentHashMap<String,AtomicLong> extendKeyMap) {
        this.extendKeyMap = extendKeyMap;
    }

    /**
     * 两个DomainTotalCount合并，合并之后的dc和timestamp会使用当前的的dc和timestamp
     */
    public DomainTotalCount aggreDomainTotalCount(DomainTotalCount totalCount) {
        this.getQueryCount().addAndGet(totalCount.getQueryCount().get());
        this.getFlowControlNums().addAndGet(totalCount.getFlowControlNums().get());
        this.getIpv6Qps().addAndGet(totalCount.getIpv6Qps().get());
        this.getInBandwidth().addAndGet(totalCount.getInBandwidth().get());
        this.getOutBandwidth().addAndGet(totalCount.getOutBandwidth().get());
        this.getIosH2Qps().addAndGet(totalCount.getIosH2Qps().get());
        this.getIosHTTPSQps().addAndGet(totalCount.getIosHTTPSQps().get());
        this.getIosQps().addAndGet(totalCount.getIosQps().get());
        this.getIosReQps().addAndGet(totalCount.getIosReQps().get());
        this.getAndroidQps().addAndGet(totalCount.getAndroidQps().get());
        this.getAndroidH2Qps().addAndGet(totalCount.getAndroidH2Qps().get());
        this.getAndroidHTTPSQps().addAndGet(totalCount.getAndroidHTTPSQps().get());
        this.getAndroidReQps().addAndGet(totalCount.getAndroidReQps().get());
        this.getIosInBandWidth().addAndGet(totalCount.getIosInBandWidth().get());
        this.getAndroidInBandWidth().addAndGet(totalCount.getAndroidInBandWidth().get());
        this.getWpInBandWidth().addAndGet(totalCount.getWpInBandWidth().get());
        this.getWpQps().addAndGet(totalCount.getWpQps().get());
        this.getWpReQps().addAndGet(totalCount.getWpReQps().get());
        this.getIosWatchQps().addAndGet(totalCount.getIosWatchQps().get());
        this.getIosWatchInBandWidth().addAndGet(totalCount.getIosWatchInBandWidth().get());
        this.getIosPadQps().addAndGet(totalCount.getIosPadQps().get());
        this.getIosPadInBandWidth().addAndGet(totalCount.getIosPadInBandWidth().get());
        this.setExtendKeyMap(totalCount);
        return this;
    }
}
