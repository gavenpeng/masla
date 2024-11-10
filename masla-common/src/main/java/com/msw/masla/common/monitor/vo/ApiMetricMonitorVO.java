package com.msw.masla.common.monitor.vo;

import java.util.Date;

/**
 * Created by Gavin.peng on 2018/4/13.
 */
public class ApiMetricMonitorVO {


    private Long appId;
    private String appName;
    private String dcName;
    private String serviceId;
    private float acquireCost;//获取连接的时间
    private float sendIOCost;//获取连接的时间
    private float pushCost;//获取连接的时间
    private float serverCost;//服务耗时时间
    private long tp50;//top 50 服务耗时时间
    private long tp90;//top 90 服务耗时时间
    private long tp99;//top 99 服务耗时时间
    private long tp999;//top 999 服务耗时时间
    private long tp9999;//top 9999 服务耗时时间
    private long max;//最大 服务耗时时间
    private long min;//最小 服务耗时时间
    private long qps;
    private long peakQps;
    private long h2Qps;//HTTP2 qps
    private long successNums;
    private long rejectNums;
    private long slowNums;
    private long timeoutNums;
    private long connClosedNums;
    private long exceptionNums;
    private long circuitNums;
    private long flowControllerNums;
    private int hostNums;
    private long fiveXXCodeNums;
    private long fourXXCodeNums;
    private long code400;
    private long code401;
    private long code404;
    private long codeAppDefine;
    private long outBandWidth;
    private long inBandWidth;
    private long varnishCacheMiss;
    private Date gmtCreate;
    private Date gmtModify;
    private float errorPercent;
    private String healthState;
    private String circuitState;
    private String host;
    private String serverIP;//gateway server host
    private String group;

    //后端应用拒绝连接数量
    private long connRefusedNums;

    //后端reset连接
    private long connResetNums;

    //后端连接超时
    private long connTimeoutNums;

    //连接池队列已满拒绝连接数量
    private long connPoolFullRejectNums;

    //连接池等待超时数量
    private long connPoolWaitTimeoutNums;


    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Float getAcquireCost() {
        return acquireCost;
    }

    public void setAcquireCost(Float acquireCost) {
        this.acquireCost = acquireCost;
    }

    public Float getServerCost() {
        return serverCost;
    }

    public void setServerCost(Float serverCost) {
        this.serverCost = serverCost;
    }

    public Long getTp90() {
        return tp90;
    }

    public void setTp90(Long tp90) {
        this.tp90 = tp90;
    }

    public Long getTp99() {
        return tp99;
    }

    public void setTp99(Long tp99) {
        this.tp99 = tp99;
    }

    public Long getQps() {
        return qps;
    }

    public void setQps(Long qps) {
        this.qps = qps;
    }

    public Long getPeakQps() {
        return peakQps;
    }

    public void setPeakQps(long peakQps) {
        this.peakQps = peakQps;
    }

    public Long getH2Qps() {
        return h2Qps;
    }

    public void setH2Qps(Long h2Qps) {
        this.h2Qps = h2Qps;
    }

    public Long getOutBandWidth() {
        return outBandWidth;
    }

    public void setOutBandWidth(Long outBandWidth) {
        this.outBandWidth = outBandWidth;
    }

    public Long getInBandWidth() {
        return inBandWidth;
    }

    public void setInBandWidth(Long inBandWidth) {
        this.inBandWidth = inBandWidth;
    }

    public Long getSuccessNums() {
        return successNums;
    }

    public void setSuccessNums(Long successNums) {
        this.successNums = successNums;
    }

    public Long getRejectNums() {
        return rejectNums;
    }

    public void setRejectNums(Long rejectNums) {
        this.rejectNums = rejectNums;
    }

    public Long getSlowNums() {
        return slowNums;
    }

    public void setSlowNums(Long slowNums) {
        this.slowNums = slowNums;
    }

    public Long getTimeoutNums() {
        return timeoutNums;
    }

    public void setTimeoutNums(Long timeoutNums) {
        this.timeoutNums = timeoutNums;
    }

    public Long getExceptionNums() {
        return exceptionNums;
    }

    public void setExceptionNums(Long exceptionNums) {
        this.exceptionNums = exceptionNums;
    }

    public Long getCircuitNums() {
        return circuitNums;
    }

    public void setCircuitNums(Long circuitNums) {
        this.circuitNums = circuitNums;
    }


    public int getHostNums() {
        return hostNums;
    }

    public void setHostNums(int hostNums) {
        this.hostNums = hostNums;
    }

    public Float getErrorPercent() {
        return errorPercent;
    }

    public void setErrorPercent(Float errorPercent) {
        this.errorPercent = errorPercent;
    }

    public String getHealthState() {
        return healthState;
    }

    public void setHealthState(String healthState) {
        this.healthState = healthState;
    }

    public String getCircuitState() {
        return circuitState;
    }

    public void setCircuitState(String circuitState) {
        this.circuitState = circuitState;
    }

    public Long getFiveXXCodeNums() {
        return fiveXXCodeNums;
    }

    public void setFiveXXCodeNums(Long fiveXXCodeNums) {
        this.fiveXXCodeNums = fiveXXCodeNums;
    }

    public Long getFourXXCodeNums() {
        return fourXXCodeNums;
    }

    public void setFourXXCodeNums(Long fourXXCodeNums) {
        this.fourXXCodeNums = fourXXCodeNums;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModify() {
        return gmtModify;
    }

    public void setGmtModify(Date gmtModify) {
        this.gmtModify = gmtModify;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public Long getTp999() {
        return tp999;
    }

    public void setTp999(Long tp999) {
        this.tp999 = tp999;
    }

    public Long getTp9999() {
        return tp9999;
    }

    public void setTp9999(Long tp9999) {
        this.tp9999 = tp9999;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public Long getMin() {
        return min;
    }

    public void setMin(Long min) {
        this.min = min;
    }

    public Long getTp50() {
        return tp50;
    }

    public void setTp50(Long tp50) {
        this.tp50 = tp50;
    }

    public Long getConnClosedNums() {
        return connClosedNums;
    }

    public void setConnClosedNums(Long connClosedNums) {
        this.connClosedNums = connClosedNums;
    }

    public Float getSendIOCost() {
        return sendIOCost;
    }

    public void setSendIOCost(Float sendIOCost) {
        this.sendIOCost = sendIOCost;
    }

    public Float getPushCost() {
        return pushCost;
    }

    public void setPushCost(Float pushCost) {
        this.pushCost = pushCost;
    }


    public Long getConnRefusedNums() {
        return connRefusedNums;
    }

    public void setConnRefusedNums(Long connRefusedNums) {
        this.connRefusedNums = connRefusedNums;
    }

    public Long getConnResetNums() {
        return connResetNums;
    }

    public void setConnResetNums(Long connResetNums) {
        this.connResetNums = connResetNums;
    }

    public Long getConnTimeoutNums() {
        return connTimeoutNums;
    }

    public void setConnTimeoutNums(Long connTimeoutNums) {
        this.connTimeoutNums = connTimeoutNums;
    }

    public Long getConnPoolFullRejectNums() {
        return connPoolFullRejectNums;
    }

    public void setConnPoolFullRejectNums(Long connPoolFullRejectNums) {
        this.connPoolFullRejectNums = connPoolFullRejectNums;
    }

    public Long getConnPoolWaitTimeoutNums() {
        return connPoolWaitTimeoutNums;
    }

    public void setConnPoolWaitTimeoutNums(Long connPoolWaitTimeoutNums) {
        this.connPoolWaitTimeoutNums = connPoolWaitTimeoutNums;
    }

    public Long getCode400() {
        return code400;
    }

    public void setCode400(Long code400) {
        this.code400 = code400;
    }

    public Long getCode401() {
        return code401;
    }

    public void setCode401(Long code401) {
        this.code401 = code401;
    }

    public Long getCode404() {
        return code404;
    }

    public void setCode404(Long code404) {
        this.code404 = code404;
    }

    public Long getCodeAppDefine() {
        return codeAppDefine;
    }

    public void setCodeAppDefine(Long codeAppDefine) {
        this.codeAppDefine = codeAppDefine;
    }

    public long getVarnishCacheMiss() {
        return varnishCacheMiss;
    }

    public void setVarnishCacheMiss(long varnishCacheMiss) {
        this.varnishCacheMiss = varnishCacheMiss;
    }

    public long getFlowControllerNums() {
        return flowControllerNums;
    }

    public void setFlowControllerNums(long flowControllerNums) {
        this.flowControllerNums = flowControllerNums;
    }

    public String getDcName() {
        return dcName;
    }

    public void setDcName(String dcName) {
        this.dcName = dcName;
    }

    @Override
    public String toString() {
        return "ApiMetricMonitorVO{" +
            ", appName='" + appName + '\'' +
            ", serviceId='" + serviceId + '\'' +
            ", fiveXXCodeNums=" + fiveXXCodeNums +
            ", fourXXCodeNums=" + fourXXCodeNums +
            ", code400=" + code400 +
            ", code401=" + code401 +
            ", code404=" + code404 +
            ", host='" + host + '\'' +
            ", serverIP='" + serverIP + '\'' +
            ", group='" + group + '\'' +
            '}';
    }
}
