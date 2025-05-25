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
package com.msw.masla.common.pojo;


import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Gavin.peng on 2018/2/24.
 */
public class ApiMetric {

    private static final long METRIC_INTERVAL = 5 * 60 * 1000l;

//    private String appName;
//    private long appId;
    private String serviceId;
    private String host;
    private AtomicLong waitCost = new AtomicLong(0);//队列等待时间
    private AtomicLong acquireCost = new AtomicLong(0);//获取连接的时间
    private AtomicLong serverCost = new AtomicLong(0);//服务耗时时间
    private AtomicLong sendIOCost = new AtomicLong(0);//服务耗时时间
    private AtomicLong pushCost = new AtomicLong(0);//服务耗时时间
    private AtomicLong qps = new AtomicLong(0);
    private AtomicLong peakQps = new AtomicLong(0);
    private int outBandWidth = 0;//记录响应消息体大小
    private int inBandWidth = 0;//记录请求消息体大小
    private AtomicLong successNums = new AtomicLong(0);
    private AtomicLong rejectNums = new AtomicLong(0);
    private AtomicLong slowNums = new AtomicLong(0);
    private AtomicLong timeoutNums = new AtomicLong(0);
    private AtomicLong exceptionNums = new AtomicLong(0);
    //转发失败异常次数
    private AtomicLong forwardFailedNums = new AtomicLong(0);
    private AtomicLong connClosedNums = new AtomicLong(0);
    private AtomicLong circuitNums = new AtomicLong(0);
    private AtomicLong fiveXXCodeNums = new AtomicLong(0);
    private AtomicLong fourXXCodeNums = new AtomicLong(0);
    //400 401 404 code 单独进行统计
    private AtomicLong code400 = new AtomicLong(0);
    private AtomicLong code401 = new AtomicLong(0);
    private AtomicLong code404 = new AtomicLong(0);

    private AtomicLong varnishCacheMiss = new AtomicLong(0);

    //用户自定义响应码统计
    private AtomicLong codeAppDefine = new AtomicLong(0);




    //后端应用拒绝连接数量
    private AtomicLong connRefusedNums = new AtomicLong(0);

    //后端reset连接
    private AtomicLong connResetNums = new AtomicLong(0);

    //后端连接超时
    private AtomicLong connTimeoutNums = new AtomicLong(0);

    //连接池队列已满拒绝连接数量
    private AtomicLong connPoolFullRejectNums = new AtomicLong(0);

    //连接池等待超时数量
    private AtomicLong connPoolWaitTimeoutNums = new AtomicLong(0);

    //private boolean circuitState;

    private Long preQps = 0l;
    private Short tp90;

    private long lastActiveTime = -1;



    public ApiMetric(String serviceId){
        this.serviceId = serviceId;
        this.lastActiveTime = System.currentTimeMillis();
//        this.appName = appName;
//        this.appId = appId;
    }

    public AtomicLong getQps() {
        return qps;
    }

    public void setQps(AtomicLong qps) {
        this.qps = qps;
    }

    public AtomicLong getPeakQps() {
        return peakQps;
    }

    public void setPeakQps(AtomicLong peakQps) {
        this.peakQps = peakQps;
    }

    public Short getTp90() {
        return tp90;
    }

    public void setTp90(Short tp90) {
        this.tp90 = tp90;
    }

    public AtomicLong getWaitCost() {
        return waitCost;
    }

    public void setWaitCost(AtomicLong waitCost) {
        this.waitCost = waitCost;
    }

    public AtomicLong getAcquireCost() {
        return acquireCost;
    }

    public void setAcquireCost(AtomicLong acquireCost) {
        this.acquireCost = acquireCost;
    }

    public AtomicLong getServerCost() {
        return serverCost;
    }

    public void setServerCost(AtomicLong serverCost) {
        this.serverCost = serverCost;
    }

    public String getServiceId() {
        return serviceId;
    }

//    public String getAppName() {
//        return appName;
//    }

    public Long getPreQps() {
        return preQps;
    }

    public void setPreQps(Long preQps) {
        this.preQps = preQps;
    }

    public int getOutBandWidth() {
        return outBandWidth;
    }

    public void setOutBandWidth(int outBandWidth) {
        //这里不需要加锁
        if(outBandWidth > this.outBandWidth) {
            this.outBandWidth = outBandWidth;
        }
    }


    public int getInBandWidth() {
        return inBandWidth;
    }

    public void setInBandWidth(int inBandWidth) {
        if(inBandWidth > this.inBandWidth) {
            this.inBandWidth = inBandWidth;
        }
    }

    public void resetBandWidth(){
        this.inBandWidth = 0;
        this.outBandWidth = 0;
    }


    public AtomicLong getCode400() {
        return code400;
    }

    public void setCode400(AtomicLong code400) {
        this.code400 = code400;
    }

    public AtomicLong getCode401() {
        return code401;
    }

    public void setCode401(AtomicLong code401) {
        this.code401 = code401;
    }

    public AtomicLong getCode404() {
        return code404;
    }

    public void setCode404(AtomicLong code404) {
        this.code404 = code404;
    }

    public AtomicLong getSuccessNums() {
        return successNums;
    }

    public void setSuccessNums(AtomicLong successNums) {
        this.successNums = successNums;
    }

    public AtomicLong getRejectNums() {
        return rejectNums;
    }

    public void setRejectNums(AtomicLong rejectNums) {
        this.rejectNums = rejectNums;
    }

    public AtomicLong getSlowNums() {
        return slowNums;
    }

    public void setSlowNums(AtomicLong slowNums) {
        this.slowNums = slowNums;
    }

    public AtomicLong getTimeoutNums() {
        return timeoutNums;
    }

    public void setTimeoutNums(AtomicLong timeoutNums) {
        this.timeoutNums = timeoutNums;
    }

    public AtomicLong getExceptionNums() {
        return exceptionNums;
    }

    public AtomicLong getConnClosedNums() {
        return connClosedNums;
    }

    public AtomicLong getSendIOCost() {
        return sendIOCost;
    }

    public void setSendIOCost(AtomicLong sendIOCost) {
        this.sendIOCost = sendIOCost;
    }

    public AtomicLong getPushCost() {
        return pushCost;
    }

    public void setPushCost(AtomicLong pushCost) {
        this.pushCost = pushCost;
    }

    public void setConnClosedNums(AtomicLong connClosedNums) {
        this.connClosedNums = connClosedNums;
    }

    public void setExceptionNums(AtomicLong exceptionNums) {
        this.exceptionNums = exceptionNums;
    }

    public AtomicLong getCircuitNums() {
        return circuitNums;
    }

    public void setCircuitNums(AtomicLong circuitNums) {
        this.circuitNums = circuitNums;
    }

    public AtomicLong getFiveXXCodeNums() {
        return fiveXXCodeNums;
    }

    public void setFiveXXCodeNums(AtomicLong fiveXXCodeNums) {
        this.fiveXXCodeNums = fiveXXCodeNums;
    }

    public AtomicLong getFourXXCodeNums() {
        return fourXXCodeNums;
    }

    public void setFourXXCodeNums(AtomicLong fourXXCodeNums) {
        this.fourXXCodeNums = fourXXCodeNums;
    }

    public AtomicLong getForwardFailedNums() {
        return forwardFailedNums;
    }

    public void setForwardFailedNums(AtomicLong forwardFailedNums) {
        this.forwardFailedNums = forwardFailedNums;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    //    public long getAppId() {
//        return appId;
//    }


    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public void reset(){
        this.qps.set(0);
        this.peakQps.set(0);
        this.waitCost.set(0);
        this.acquireCost.set(0);
        this.serverCost.set(0);
        this.outBandWidth = 0;
        this.code400.set(0);
        this.code401.set(0);
        this.code404.set(0);
    }


    public AtomicLong getConnRefusedNums() {
        return connRefusedNums;
    }

    public void setConnRefusedNums(AtomicLong connRefusedNums) {
        this.connRefusedNums = connRefusedNums;
    }

    public AtomicLong getConnResetNums() {
        return connResetNums;
    }

    public void setConnResetNums(AtomicLong connResetNums) {
        this.connResetNums = connResetNums;
    }

    public AtomicLong getConnTimeoutNums() {
        return connTimeoutNums;
    }

    public void setConnTimeoutNums(AtomicLong connTimeoutNums) {
        this.connTimeoutNums = connTimeoutNums;
    }

    public AtomicLong getConnPoolFullRejectNums() {
        return connPoolFullRejectNums;
    }

    public void setConnPoolFullRejectNums(AtomicLong connPoolFullRejectNums) {
        this.connPoolFullRejectNums = connPoolFullRejectNums;
    }

    public AtomicLong getConnPoolWaitTimeoutNums() {
        return connPoolWaitTimeoutNums;
    }

    public void setConnPoolWaitTimeoutNums(AtomicLong connPoolWaitTimeoutNums) {
        this.connPoolWaitTimeoutNums = connPoolWaitTimeoutNums;
    }

    public AtomicLong getCodeAppDefine() {
        return codeAppDefine;
    }

    public void setCodeAppDefine(AtomicLong codeAppDefine) {
        this.codeAppDefine = codeAppDefine;
    }

    public AtomicLong getVarnishCacheMiss() {
        return varnishCacheMiss;
    }

    public void setVarnishCacheMiss(AtomicLong varnishCacheMiss) {
        this.varnishCacheMiss = varnishCacheMiss;
    }
}
