package com.msw.masla.common.monitor.vo;

/**
 * Created by gaoyue on 17/7/14.
 */
public class PushThreadMonitorVO {
    private String corePoolSize;
    private String maximumPoolSize;
    private String workQueueSize;
    private String poolInfo;

    public String getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(String corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public String getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(String maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public String getWorkQueueSize() {
        return workQueueSize;
    }

    public void setWorkQueueSize(String workQueueSize) {
        this.workQueueSize = workQueueSize;
    }

    public String getPoolInfo() {
        return poolInfo;
    }

    public void setPoolInfo(String poolInfo) {
        this.poolInfo = poolInfo;
    }

    @Override
    public String toString() {
        return "PushThreadMonitorVO{" +
                "corePoolSize='" + corePoolSize + '\'' +
                ", maximumPoolSize='" + maximumPoolSize + '\'' +
                ", workQueueSize='" + workQueueSize + '\'' +
                ", poolInfo='" + poolInfo + '\'' +
                '}';
    }
}
