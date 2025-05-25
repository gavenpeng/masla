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
package com.msw.masla.common.monitor.vo;

import java.util.Date;

public class GroupMerticVO {

    private Long appId;
    private String appName;
    private Long acquireCost;//获取连接的时间
    private Long sendIOCost;//获取连接的时间
    private Long pushCost;//获取连接的时间
    private Long serverCost;//服务耗时时间
    private Long tp50;//top 50 服务耗时时间
    private Long tp90;//top 90 服务耗时时间
    private Long tp99;//top 99 服务耗时时间
    private Long tp999;//top 999 服务耗时时间
    private Long max;//最大 服务耗时时间
    private Long min;//最小 服务耗时时间
    private Long qps;
    private Long successNums;
    private Long rejectNums;
    private Long slowNums;
    private Long timeoutNums;
    private Long connClosedNums;
    private Long exceptionNums;
    private Long circuitNums;
    private int hostNums;
    private Long fiveXXCodeNums;
    private Long fourXXCodeNums;
    private Long outBandWidth;
    private Long inBandWidth;
    private Date gmtCreate;
    private Date gmtModify;
    private float errorPercent;
    private String healthState;
    private String circuitState;
    private String serverIP;//gateway server host
    private String group;


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

    public Long getAcquireCost() {
        return acquireCost;
    }

    public void setAcquireCost(Long acquireCost) {
        this.acquireCost = acquireCost;
    }

    public Long getSendIOCost() {
        return sendIOCost;
    }

    public void setSendIOCost(Long sendIOCost) {
        this.sendIOCost = sendIOCost;
    }

    public Long getPushCost() {
        return pushCost;
    }

    public void setPushCost(Long pushCost) {
        this.pushCost = pushCost;
    }

    public Long getServerCost() {
        return serverCost;
    }

    public void setServerCost(Long serverCost) {
        this.serverCost = serverCost;
    }

    public Long getTp50() {
        return tp50;
    }

    public void setTp50(Long tp50) {
        this.tp50 = tp50;
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

    public Long getTp999() {
        return tp999;
    }

    public void setTp999(Long tp999) {
        this.tp999 = tp999;
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

    public Long getQps() {
        return qps;
    }

    public void setQps(Long qps) {
        this.qps = qps;
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

    public Long getConnClosedNums() {
        return connClosedNums;
    }

    public void setConnClosedNums(Long connClosedNums) {
        this.connClosedNums = connClosedNums;
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

    public float getErrorPercent() {
        return errorPercent;
    }

    public void setErrorPercent(float errorPercent) {
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

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
