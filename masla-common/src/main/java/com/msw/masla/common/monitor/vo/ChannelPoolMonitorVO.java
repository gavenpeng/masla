package com.msw.masla.common.monitor.vo;

/**
 * Created by gaoyue on 17/7/14.
 */
public class ChannelPoolMonitorVO implements Comparable{
    private String appName;
    private String ip;
    private String maxConnection;
    private String maxPendingAcquire;
    private String acquiredChannelCount;
    private String pendingAcquireCount;
    private String closed;

    private String channelStatus;
    private String multiplexLimit;


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMaxConnection() {
        return maxConnection;
    }

    public void setMaxConnection(String maxConnection) {
        this.maxConnection = maxConnection;
    }

    public String getMaxPendingAcquire() {
        return maxPendingAcquire;
    }

    public void setMaxPendingAcquire(String maxPendingAcquire) {
        this.maxPendingAcquire = maxPendingAcquire;
    }

    public String getAcquiredChannelCount() {
        return acquiredChannelCount;
    }

    public void setAcquiredChannelCount(String acquiredChannelCount) {
        this.acquiredChannelCount = acquiredChannelCount;
    }

    public String getPendingAcquireCount() {
        return pendingAcquireCount;
    }

    public void setPendingAcquireCount(String pendingAcquireCount) {
        this.pendingAcquireCount = pendingAcquireCount;
    }

    public String getClosed() {
        return closed;
    }

    public void setClosed(String closed) {
        this.closed = closed;
    }

    @Override
    public int compareTo(Object o) {
        ChannelPoolMonitorVO b = (ChannelPoolMonitorVO)o;
        if(b.getPendingAcquireCount().equals(this.getPendingAcquireCount())){
            return this.getIp().compareTo(b.getIp());
        }else{
            return b.getPendingAcquireCount().compareTo(this.getPendingAcquireCount());
        }
    }

    public String getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(String channelStatus) {
        this.channelStatus = channelStatus;
    }

    public String getMultiplexLimit() {
        return multiplexLimit;
    }

    public void setMultiplexLimit(String multiplexLimit) {
        this.multiplexLimit = multiplexLimit;
    }

    @Override
    public String toString() {
        return "ChannelPoolMonitorVO{" +
                "ip='" + ip + '\'' +
                ", maxConnection='" + maxConnection + '\'' +
                ", maxPendingAcquire='" + maxPendingAcquire + '\'' +
                ", acquiredChannelCount='" + acquiredChannelCount + '\'' +
                ", pendingAcquireCount='" + pendingAcquireCount + '\'' +
                ", closed='" + closed + '\'' +
                ", channelStatus='" + channelStatus + '\'' +
                ", multiplexLimit='" + multiplexLimit + '\'' +
                '}';
    }
}
