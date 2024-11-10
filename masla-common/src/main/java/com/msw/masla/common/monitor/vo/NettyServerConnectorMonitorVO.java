package com.msw.masla.common.monitor.vo;

/**
 * Created by gaoyue on 17/10/27.
 */
public class NettyServerConnectorMonitorVO implements Comparable{

    private String maxSessionCount;

    private String submitWorkThreadCount;

    private String workThreadActiveCount;

    private String workThreadPoolSize;

    private String workThreadQueueSize;


    public String getMaxSessionCount() {
        return maxSessionCount;
    }

    public void setMaxSessionCount(String maxSessionCount) {
        this.maxSessionCount = maxSessionCount;
    }


    public String getSubmitWorkThreadCount() {
        return submitWorkThreadCount;
    }

    public String getWorkThreadActiveCount() {
        return workThreadActiveCount;
    }

    public void setWorkThreadActiveCount(String workThreadActiveCount) {
        this.workThreadActiveCount = workThreadActiveCount;
    }

    public String getWorkThreadPoolSize() {
        return workThreadPoolSize;
    }

    public void setWorkThreadPoolSize(String workThreadPoolSize) {
        this.workThreadPoolSize = workThreadPoolSize;
    }

    public String getWorkThreadQueueSize() {
        return workThreadQueueSize;
    }

    public void setWorkThreadQueueSize(String workThreadQueueSize) {
        this.workThreadQueueSize = workThreadQueueSize;
    }

    public void setSubmitWorkThreadCount(String submitWorkThreadCount) {
        this.submitWorkThreadCount = submitWorkThreadCount;
    }

    @Override
    public String toString() {
        return "NettyServerConnectorMonitorVO{" +
                "maxSessionCount='" + maxSessionCount + '\'' +
                ", submitWorkThreadCount='" + submitWorkThreadCount + '\'' +
                ", workThreadActiveCount='" + workThreadActiveCount + '\'' +
                ", workThreadPoolSize='" + workThreadPoolSize + '\'' +
                ", workThreadQueueSize='" + workThreadQueueSize + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        NettyServerConnectorMonitorVO b = (NettyServerConnectorMonitorVO)o;
        return b.getMaxSessionCount().compareTo(this.getMaxSessionCount());

    }


}
