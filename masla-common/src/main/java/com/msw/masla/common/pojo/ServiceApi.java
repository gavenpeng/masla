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

import com.msw.masla.common.enums.UrlPassThroughType;
import com.msw.masla.common.stream.CircuitPolicy;
import lombok.ToString;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by gavin.peng
 */
@ToString
public class ServiceApi {

    private static final long MINUTE_UNIT = 1000 * 60l;

    private Long id;

    private String name;

    private String host;

    private Integer port;

    private String contextRoot;

    private String path;

    //目标转发path，支持自定义映射
    private String redirectPath;

    //用来在网关层添加参数，比如时间戳
    private String queryString;

    private Long appId;

    //1:http   2:rpc   3:varnish
    private Integer protocol;

    private String varnishQueryString;

    private Date varnishStartTime;

    private Date varnishEndTime;

    //on off
    private Integer status;

    //熔断比例 -1 关闭，
    // 10 丢弃10%的请求
    // 20 丢弃20%的请求
    // 30 丢弃30%的请求
    // 50 丢弃50%的请求
    // 100 全部丢弃
    private Integer discard = 0;

    private Integer passThrough = UrlPassThroughType.CONTEXT_ROOT_AND_PATH_PASS_THROUGH.getCode();

    private Integer cutIndex = -1;

    //private Discard



    /**
     * 关联流控规则ID
     */
    private String flowRuleIds;

    /**
     * 关联黑名单规则ID
     */
    private String blackSelectorIds;

    private String addUser;
    private Date gmtCreate;
    private Date gmtModify;

    private Integer sso;

    private Boolean regex;

    private boolean autoDiscardOff;

    private Integer checkParameters;

    private Long timeout = 5000l;

    private Long downStreamTimeout = 0l;

    private AtomicLong circuitTime = new AtomicLong(-1);

    //


    private Integer discardTriggerNums;//自动触发熔断的超时次数
    private Long discardTriggerMinute;//触发熔断的单位时间
    private Long discardExpireMinute;//熔断的有效期，过该时间自动取消熔断


    private Integer grayRatio;

    private boolean allDisalbed;

    private boolean doDisalbed;

    private CircuitPolicy circuitPolicy;

//    private static final ThreadLocal<ServiceApi> API_DO_THREAD_LOCAL = new ThreadLocal<ServiceApi>();

    private AtomicInteger queryCount = new AtomicInteger(0);

    private AtomicLong outBandWidth = new AtomicLong(0);

    private Integer allowCount;

    private AtomicInteger serverTimeoutCount = new AtomicInteger(0);

    private AtomicReference<Boolean> circuitDownFlag = new AtomicReference<Boolean>(false);

    private AtomicReference<Boolean> circuitUpgradeFlag = new AtomicReference<Boolean>(false);

    private volatile long updateServerTimeoutCountTime = System.currentTimeMillis();

    private AtomicLong allCostTimeSum = new AtomicLong(0);

    private AtomicLong serverCostTimeSum = new AtomicLong(0);

    private AtomicLong acquireConnectCostTimeSum = new AtomicLong(0);

    //队列排队时间
    private AtomicLong queueWaitCostTimeSum = new AtomicLong(0);


    public String getBlackSelectorIds() {
        return blackSelectorIds;
    }

    public void setBlackSelectorIds(String blackSelectorIds) {
        this.blackSelectorIds = blackSelectorIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Integer getProtocol() {
        return protocol;
    }

    public void setProtocol(Integer protocol) {
        this.protocol = protocol;
    }

    public String getVarnishQueryString() {
        return varnishQueryString;
    }

    public void setVarnishQueryString(String varnishQueryString) {
        this.varnishQueryString = varnishQueryString;
    }

    public Date getVarnishStartTime() {
        return varnishStartTime;
    }

    public void setVarnishStartTime(Date varnishStartTime) {
        this.varnishStartTime = varnishStartTime;
    }

    public Date getVarnishEndTime() {
        return varnishEndTime;
    }

    public void setVarnishEndTime(Date varnishEndTime) {
        this.varnishEndTime = varnishEndTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFlowRuleIds() {
        return flowRuleIds;
    }

    public void setFlowRuleIds(String flowRuleIds) {
        this.flowRuleIds = flowRuleIds;
    }

    public String getAddUser() {
        return addUser;
    }

    public void setAddUser(String addUser) {
        this.addUser = addUser;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSso() {
        return sso;
    }

    public void setSso(Integer sso) {
        this.sso = sso;
    }

    public Boolean getRegex() {
        return regex;
    }

    public AtomicLong getOutBandWidth() {
        return outBandWidth;
    }

    public void setOutBandWidth(AtomicLong outBandWidth) {
        this.outBandWidth = outBandWidth;
    }

    public void setRegex(Boolean regex) {
        this.regex = regex;
    }

    public Integer getCheckParameters() {
        return checkParameters;
    }

    public void setCheckParameters(Integer checkParameters) {
        this.checkParameters = checkParameters;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Integer getGrayRatio() {
        return grayRatio;
    }

    public void setGrayRatio(Integer grayRatio) {
        this.grayRatio = grayRatio;
    }

    public AtomicInteger getQueryCount() {
        return queryCount;
    }

    public Integer getDiscardTriggerNums() {
        return discardTriggerNums;
    }

    public void setDiscardTriggerNums(Integer discardTriggerNums) {
        this.discardTriggerNums = discardTriggerNums;
    }

    public Long getDiscardTriggerMinute() {
        return discardTriggerMinute;
    }

    public void setDiscardTriggerMinute(Long discardTriggerMinute) {
        this.discardTriggerMinute = discardTriggerMinute;
    }

    public Long getDiscardExpireMinute() {
        return discardExpireMinute;
    }

    public void setDiscardExpireMinute(Long discardExpireMinute) {
        this.discardExpireMinute = discardExpireMinute;
    }

    public boolean isAutoDiscardOff() {
        return autoDiscardOff;
    }

    public void setAutoDiscardOff(boolean autoDiscardOff) {
        this.autoDiscardOff = autoDiscardOff;
    }

    private boolean isAutoDiscard(){
        if(this.discard > 0 && this.getDiscardTriggerMinute() != null
                && this.getDiscardTriggerMinute() >0
                && this.getDiscardTriggerNums() != null
                && this.getDiscardTriggerNums() >0){
            return true;
        }else{
            return false;
        }
    }


    public Integer getDiscard() {
        return discard;
    }

    public void setDiscard(Integer discard) {
        this.discard = discard;
    }

    public CircuitPolicy getCircuitPolicy() {
        return circuitPolicy;
    }

    public void setCircuitPolicy(CircuitPolicy circuitPolicy) {
        this.circuitPolicy = circuitPolicy;
    }


    public void setAllDisalbed(boolean allDisalbed) {
        this.allDisalbed = allDisalbed;
    }

    public boolean isDoDisalbed() {
        return doDisalbed;
    }

    public void setDoDisalbed(boolean doDisalbed) {
        this.doDisalbed = doDisalbed;
    }

    public AtomicLong getCircuitTime() {
        return circuitTime;
    }

    public void setCircuitTime() {
        this.circuitTime.compareAndSet(-1,System.currentTimeMillis());
    }

    public void resetCircuitTime() {
        this.circuitTime.set(-1);
    }

    public Integer getPassThrough() {
        return passThrough;
    }

    public void setPassThrough(Integer passThrough) {
        this.passThrough = passThrough;
    }

    public Integer getCutIndex() {
        return cutIndex;
    }

    public void setCutIndex(Integer cutIndex) {
        this.cutIndex = cutIndex;
    }

    public ServiceApi() {
    }

    public String getRedirectPath() {
        return redirectPath;
    }

    public void setRedirectPath(String redirectPath) {
        this.redirectPath = redirectPath;
    }

    public void clone(ServiceApi apiDO){
        this.setName(apiDO.getName());
        this.setContextRoot(apiDO.getContextRoot());
        this.setDiscard(apiDO.getDiscard());
        this.setAddUser(apiDO.getAddUser());
        this.setCheckParameters(apiDO.getCheckParameters());
        this.setFlowRuleIds(apiDO.getFlowRuleIds());
        this.setBlackSelectorIds(apiDO.getBlackSelectorIds());
        this.setCircuitPolicy(apiDO.getCircuitPolicy());
        this.setGrayRatio(apiDO.getGrayRatio());
        this.setHost(apiDO.getHost());
        this.setPath(apiDO.getPath());
        this.setPort(apiDO.getPort());
        this.setProtocol(apiDO.getProtocol());
        this.setQueryString(apiDO.getQueryString());
        this.setRegex(apiDO.getRegex());
        this.setPassThrough(apiDO.getPassThrough());
        this.setSso(apiDO.getSso());
        this.setTimeout(apiDO.getTimeout());
        this.setDownStreamTimeout(apiDO.getDownStreamTimeout());
        this.setAppId(apiDO.getAppId());
        this.setGmtModify(apiDO.getGmtModify());
        this.setStatus(apiDO.getStatus());
        this.setDiscardExpireMinute(apiDO.getDiscardExpireMinute());
        this.setDiscardTriggerMinute(apiDO.getDiscardTriggerMinute());
        this.setDiscardTriggerNums(apiDO.getDiscardTriggerNums());
        this.setVarnishStartTime(apiDO.getVarnishStartTime());
        this.setVarnishEndTime(apiDO.getVarnishEndTime());
        this.setVarnishQueryString(apiDO.getVarnishQueryString());
        this.setRedirectPath(apiDO.getRedirectPath());

    }

    public void reset(){
        this.setDoDisalbed(false);
        this.setAutoDiscardOff(false);
        this.setAllDisalbed(false);
        this.getCircuitTime().set(-1);
    }

    public Long getDownStreamTimeout() {
        return downStreamTimeout;
    }

    public void setDownStreamTimeout(Long downStreamTimeout) {
        this.downStreamTimeout = downStreamTimeout;
    }

    @Override
    public int hashCode(){
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object == null){
            return false;
        }
        ServiceApi apiDo = (ServiceApi)object;
        if(this.getId() == apiDo.getId()){
            return true;
        }else{
            return false;
        }
    }

}
