package com.msw.masla.core.discovery.nacos;

import com.msw.masla.common.enums.HostStatus;
import com.msw.masla.protocol.http.netty.http.HostInstance;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Gavin.peng on 2017/6/22.
 */
public class HostProfile implements HostInstance {

    public static final long WAKE_UP_COMPLETE_TIME = 180000;

    //2分钟，如果一个host 出现忙，则这个时间内不给流量，这个时间是一个慢启动的时间，过了这个时间，其他的机器应该已经预热完成。
    public static final long FREE_TIME_AFTER_BUSY = 120000000000l;//单位纳秒

    public static final long FLOW_INCRENCE_INTERVAL = 10000;

    public static final long ONE_MINUTE = 60000;
    public static final long TWO_MINUTE = 120000;

    public static final int XCWND = 1;

    public static final int UPGRADE_MULTIPLE = 2;

    private String serviceId;


    private String host;

    private Map<String, String> metadata;

    /**
     * 应用id
     */
    private Long appId;
    /**
     * 服务端口
     */
    private int port = -1;

    /**
     * 当前权重
     */
    private AtomicInteger curWeight;

    //隔离标示，stable和其它
    private String isolationType;

    private long lastMarkTimeoutTime;

    private  HostStatus curStatus = HostStatus.ENABLE;

    private AtomicBoolean upgradeKey = new AtomicBoolean(false);


    private long lastWakeUpTime = System.currentTimeMillis();

    private int bucketSize = XCWND;

    private AtomicInteger flowCount = new AtomicInteger(0);

    private  long updateBucketSizeTime = System.currentTimeMillis();


    public HostProfile(String host, int port){
        this.host = host;
        this.port = port;
//        this.relivePolicy = new DefaultRelivePolicy(hostIp);
    }

    public HostProfile(String host, int port, int weight){
        this.host = host;
        this.port = port;
//        this.relivePolicy = new DefaultRelivePolicy(hostIp);
        this.curWeight = new AtomicInteger(weight);
    }

    public HostProfile(String host, int port, int weight, Integer preReleaseQpsMod, HostStatus status,String isolationType,Long appId){
        this.host = host;
        this.port = port;
        this.curStatus = status;
//        this.relivePolicy = new DefaultRelivePolicy(hostIp);
        this.curWeight = new AtomicInteger(weight);
        this.isolationType = isolationType;
        this.appId = appId;
    }

    @Override
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isAvailable() {
        if(curStatus == HostStatus.ENABLE){
            return true;
        }

        return false;
    }

    public long getLastMarkTimeoutTime(){
        return this.lastMarkTimeoutTime;
    }

    public void resetTimeout(){
        this.lastMarkTimeoutTime = 0;
//        this.timeoutCount.set(0);
    }

    public HostStatus getCurStatus() {
        return curStatus;
    }

    public void setCurStatus(HostStatus curStatus) {

        if(curStatus == this.curStatus) {
            return;
        }

        this.curStatus = curStatus;
    }

    @Override
    public String getInstanceId() {
        return HostInstance.super.getInstanceId();
    }

    @Override
    public String getServiceId() {
        return this.serviceId;
    }

    @Override
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    @Override
    public String getScheme() {
        return HostInstance.super.getScheme();
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public void setServiceId(String serviceId) {

        this.serviceId = serviceId;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        //增加appid做为标示，云原生时代，pod的ip会漂移，即同一个ip会在不同的时间
        //属于不同的app。
//        result = prime * result + appId.intValue();
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        HostProfile other = (HostProfile) obj;
        if (host == null) {
            return other.host == null;
        }
        else if (!appId.equals(other.appId)) return false;
        else if (!host.equals(other.host)) return false;
        else return port == other.port;
    }



    public boolean isWakeUpComplete() {
        //            LOG.info("the host:{} is wake up complete.", hostIp);
        return (System.currentTimeMillis() - lastWakeUpTime) > WAKE_UP_COMPLETE_TIME;
    }


    public boolean hasReachFLowLimit(){

        long now = System.currentTimeMillis();
        if(now - this.lastWakeUpTime>=WAKE_UP_COMPLETE_TIME){
            return false;
        }
        if((now - updateBucketSizeTime) > FLOW_INCRENCE_INTERVAL
            && upgradeKey.compareAndSet(false, true)){
            try {
                flowCount.set(0);
                updateBucketSizeTime = System.currentTimeMillis();
                //前1分分钟按线性增长，1分钟后，指数增长
                long startupTime = now - lastWakeUpTime;
                if(startupTime >= ONE_MINUTE && startupTime <= TWO_MINUTE){
                    bucketSize = bucketSize + 5;
                }if(startupTime > TWO_MINUTE){
                    bucketSize = UPGRADE_MULTIPLE * bucketSize;
                }else{
                    bucketSize++;
                }
            }finally {
                upgradeKey.set(false);
            }

        }

        //flowCount.decrementAndGet();
        return flowCount.incrementAndGet() >= bucketSize;
    }


    public void resetSlowUpgrade(long now){
        this.lastWakeUpTime = now;
        this.updateBucketSizeTime = now;
//        this.wakeUpComplete = false;
        this.bucketSize = XCWND;
    }

    @Override
    public String toString() {
        return "HostProfile{" +
                "hostIp='" + host + '\'' +
                ", port=" + port +
                ", appId=" + appId +
                ", curWeight=" + curWeight +
                ", isolationType='" + isolationType + '\'' +
                ", lastMarkTimeoutTime=" + lastMarkTimeoutTime +
                ", curStatus=" + curStatus +
//                ", lock=" + lock +
                ", upgradeKey=" + upgradeKey +
                ", lastWakeUpTime=" + lastWakeUpTime +
                ", bucketSize=" + bucketSize +
                ", flowCount=" + flowCount +
                ", updateBucketSizeTime=" + updateBucketSizeTime +
                '}';
    }
}
