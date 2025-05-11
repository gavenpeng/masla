package com.msw.masla.common.circuit;


import com.msw.masla.common.util.MaslaSpringContextUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Gavin.peng on 2024/2/11.
 */

@Slf4j
@Data
public class CircuitRuleDefine {

    private static final String AUTO_UPGRADE_OR_DOWN_OPEN = "on";

    private final long DEFAULT_SLEEP_WINDOW_TIME = 5000l;

    private Long id;

    //对应的appId
    private Long appId;

    private String appName;

    private String ifaceUrl;

    private int self = 1;

    private Integer circuit;

    private Integer status;//0 禁用，1 启用

    private Integer circuitThreshold;//熔断阀值
    private Long circuitAttendWindow;//完全熔断后自动探测时间窗口
    private Integer upgradeOrDown;//是否做自动升降级
    private Float circuitTriggerPercent;//自动触发熔断的异常比例
    private Long circuitTriggerMinute;//触发熔断的单位时间
    private Long circuitExpireMinute;//熔断的有效期，过该时间自动取消熔断

    //自定义响应参数
    private String customizedResponseParams;

    /**
     * 自定义响应参数map, 在定时刷新任务中customizedResponseParams
     * 转化为map，避免每次请求都重复转换
     */
    private Map<String, String> customizedResponseParamMap;


    private Date gmtCreate;


    private Date gmtModify;

    private boolean allDisalbed;

    private boolean doDisalbed;

    private boolean autoDiscardOff;

    private String circuitApiUrl;


    private AtomicLong circuitTime = new AtomicLong(-1);

    private AtomicLong allCostTimeSum = new AtomicLong(0);

    private AtomicLong serverCostTimeSum = new AtomicLong(0);

    private AtomicLong acquireConnectCostTimeSum = new AtomicLong(0);

    //队列排队时间
    private AtomicLong queueWaitCostTimeSum = new AtomicLong(0);

    private MaslaCircuitBreaker circuitBreaker;

    public CircuitRuleDefine(){
    }

    public CircuitRuleDefine(Float circuitTriggerPercent, String appName){

        this.circuitTriggerPercent = circuitTriggerPercent;
        //默认30秒检查一次
        this.circuitTriggerMinute = 30000L;
        this.circuitThreshold = MaslaSpringContextUtil.getMaslaConfConfigBean().getCircuitOpenMinRequestThreshold();
        this.upgradeOrDown = AUTO_UPGRADE_OR_DOWN_OPEN.equals(MaslaSpringContextUtil.getMaslaConfConfigBean().getCircuitAutoUpgradeOrDownSwitch())? 1 : 0;
        this.circuitAttendWindow = DEFAULT_SLEEP_WINDOW_TIME;
        this.circuit = CircuitConfig.MIDDLE_PERCENT_LEVEL;//默认从50%开始熔断
        this.appName = appName;
        this.self = 0;
        this.circuitBreaker = CircuitFactory.getInstance(this.appName,this);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getIfaceUrl() {
        return ifaceUrl;
    }

    public void setIfaceUrl(String ifaceUrl) {
        this.ifaceUrl = ifaceUrl;
    }

    public Integer getCircuit() {
        return circuit;
    }

    public void setCircuit(Integer circuit) {
        this.circuit = circuit;
    }

    public Float getCircuitTriggerPercent() {
        return circuitTriggerPercent;
    }

    public void setCircuitTriggerPercent(Float circuitTriggerPercent) {
        this.circuitTriggerPercent = circuitTriggerPercent;
    }

    public Long getCircuitTriggerMinute() {
        return circuitTriggerMinute;
    }

    public void setCircuitTriggerMinute(Long circuitTriggerMinute) {
        this.circuitTriggerMinute = circuitTriggerMinute;
    }

    public Long getCircuitExpireMinute() {
        return circuitExpireMinute;
    }

    public void setCircuitExpireMinute(Long circuitExpireMinute) {
        this.circuitExpireMinute = circuitExpireMinute;
    }

    public Integer getCircuitThreshold() {
        return circuitThreshold;
    }

    public void setCircuitThreshold(Integer circuitThreshold) {
        this.circuitThreshold = circuitThreshold;
    }

    public Long getCircuitAttendWindow() {
        return circuitAttendWindow;
    }

    public void setCircuitAttendWindow(Long circuitAttendWindow) {
        this.circuitAttendWindow = circuitAttendWindow;
    }

    public Integer getUpgradeOrDown() {
        return upgradeOrDown;
    }

    public void setUpgradeOrDown(Integer upgradeOrDown) {
        this.upgradeOrDown = upgradeOrDown;
    }

    public String getCustomizedResponseParams() {
        return customizedResponseParams;
    }

    public void setCustomizedResponseParams(String customizedResponseParams) {
        this.customizedResponseParams = customizedResponseParams;
    }

    public Map<String, String> getCustomizedResponseParamMap() {
        return customizedResponseParamMap;
    }

    public void setCustomizedResponseParamMap(Map<String, String> customizedResponseParamMap) {
        this.customizedResponseParamMap = customizedResponseParamMap;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    public boolean isAllDisalbed() {
        return allDisalbed;
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


    public String getCircuitApiUrl() {
        return this.circuitApiUrl;
    }

    public void setCircuitApiUrl(String circuitApiUrl) {
        this.circuitApiUrl = circuitApiUrl;
    }



    public MaslaCircuitBreaker getCircuitBreaker(){
        return this.circuitBreaker;
    }


//    public void incrementOutBandWidth(long size) {
//        this.outBandWidth.addAndGet(size);
//    }










    public AtomicLong getAllCostTimeSum() {
        return allCostTimeSum;
    }

    public void addAllCostTimeSum(long costTime) {
        this.allCostTimeSum.addAndGet(costTime);
    }

    public AtomicLong getServerCostTimeSum() {
        return serverCostTimeSum;
    }

    public void addServerCostTimeSum(long costTime) {
        this.serverCostTimeSum.addAndGet(costTime);
    }

    public AtomicLong getAcquireConnectCostTimeSum(){
        return acquireConnectCostTimeSum;
    }

    public void addAcquireConnectCostTime(long acquireTime){
        this.acquireConnectCostTimeSum.addAndGet(acquireTime);
    }

    public AtomicLong getqueueWaitCostTimeSum(){
        return queueWaitCostTimeSum;
    }

    public void addQueueWaitCostTimeSum(long queueWaitTime){
        this.queueWaitCostTimeSum.addAndGet(queueWaitTime);
    }


//    public void setAllowCount(Integer allowCount) {
//        this.allowCount = allowCount;
//    }

    public AtomicLong getCircuitTime() {
        return circuitTime;
    }

    public void setCircuitTime() {
        this.circuitTime.compareAndSet(-1,System.currentTimeMillis());
    }

    public void resetCircuitTime() {
        this.circuitTime.set(-1);
    }

    public boolean isAutoDiscardOff() {
        return autoDiscardOff;
    }

    public void setAutoDiscardOff(boolean autoDiscardOff) {
        this.autoDiscardOff = autoDiscardOff;
    }

    public void clone(CircuitRuleDefine apiCircuitDO) {
        if (!this.isAutoMode()) {
            //手动切换为自动时
            if (apiCircuitDO.isAutoMode()) {
                //手动切换为自动时，判断是之前是否为全部熔断，而自动不是，则需要修改
                if (this.allDisalbed && apiCircuitDO.getCircuit() < 9) {
                    this.allDisalbed = false;
                }

            }
        }
        this.setCircuit(apiCircuitDO.getCircuit());
        this.setCircuitTriggerMinute(apiCircuitDO.getCircuitTriggerMinute());
        this.setCircuitTriggerPercent(apiCircuitDO.getCircuitTriggerPercent());
        this.setCircuitExpireMinute(apiCircuitDO.getCircuitExpireMinute());
        this.setUpgradeOrDown(apiCircuitDO.getUpgradeOrDown());
        this.setCircuitThreshold(apiCircuitDO.getCircuitThreshold());
        this.setCircuitAttendWindow(apiCircuitDO.getCircuitAttendWindow());
        this.setCustomizedResponseParams(apiCircuitDO.customizedResponseParams);
        this.setCustomizedResponseParamMap(apiCircuitDO.getCustomizedResponseParamMap());

    }

    public boolean isAutoMode(){
        if(this.circuitTriggerMinute != null
                && this.circuitTriggerMinute > 0
                && this.circuitTriggerPercent != null
                && this.circuitTriggerPercent > 0){
            return true;
        }
        return false;
    }




    public void reset(){
        this.setDoDisalbed(false);
        this.setAutoDiscardOff(false);
        this.setAllDisalbed(false);
        this.getCircuitTime().set(-1);

    }




}
