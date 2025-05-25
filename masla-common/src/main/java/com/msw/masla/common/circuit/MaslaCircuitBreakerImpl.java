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
package com.msw.masla.common.circuit;

import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.util.AtomicPositiveInteger;
import com.msw.masla.common.util.MaslaSpringContextUtil;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Gavin.peng on 2023/08/15.
 */
public class MaslaCircuitBreakerImpl implements MaslaCircuitBreaker {

    enum Status {
        CLOSED, OPEN, HALF_OPEN
    }

    public static int CIRCUIT_MOD_BASE_LEVEL = 10;


    public static int CIRCUIT_INIT_RECOVER_PERCENT_LEVEL = 4;//全部熔断后从50%开始恢复
    public static int DEFAULT_STEP_SIZE = 1;
    public static int DEFAULT_DOWN_STEP_SIZE = 2;
    public static int MAX_PERCENT_LEVEL = 9;

    private final AtomicReference<Status> status = new AtomicReference<>(Status.CLOSED);

    private final AtomicLong circuitOpened = new AtomicLong(-1);

    private final String key;

    private final AtomicPositiveInteger queryCount = new AtomicPositiveInteger(0);

    private final AtomicInteger serverTimeoutCount = new AtomicInteger(0);

    private final AtomicInteger successCount = new AtomicInteger(0);

    private final AtomicReference<Boolean> circuitUpdateFlag = new AtomicReference<>(false);

    private long updateServerTimeoutCountTime = System.currentTimeMillis();

    private volatile Integer circuitLevel = 0;

    private CircuitRuleDefine circuitRuleDefine;


    //创建Circuit 时默认打开状态
    protected MaslaCircuitBreakerImpl(String key, CircuitRuleDefine circuitRuleDefine) {
        this.key = key;
        this.circuitRuleDefine = circuitRuleDefine;
    }


    @Override
    public boolean markSuccess() {
        if (status.compareAndSet(Status.HALF_OPEN, Status.CLOSED)) {
            circuitOpened.set(-1L);
            return true;
        }
        return false;
    }

    @Override
    public void markNonSuccess() {
        if (status.compareAndSet(Status.HALF_OPEN, Status.OPEN)) {
            //This thread wins the race to re-open the circuit - it resets the start time for the sleep window
            circuitOpened.set(System.currentTimeMillis());
        }
    }

    @Override
    public boolean open() {
        if(status.compareAndSet(Status.CLOSED, Status.OPEN)) {
            this.circuitOpened.set(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    @Override
    public boolean isOpen() {
        return circuitOpened.get() >= 0;
    }

    @Override
    public boolean allowRequest() {
        if (circuitOpened.get() == -1) {
            return true;
        } else {
            if (status.get().equals(Status.HALF_OPEN)) {
                return false;
            } else {
                return isAfterSleepWindow();
            }
        }
    }

    private boolean isAfterSleepWindow() {
        final long circuitOpenTime = circuitOpened.get();
        final long currentTime = System.currentTimeMillis();
        final long sleepWindowTime = this.circuitRuleDefine.getCircuitAttendWindow();
        return currentTime > circuitOpenTime + sleepWindowTime;
    }


    private boolean isCircuitWindow() {
        final long currentTime = System.currentTimeMillis();
        return currentTime > this.updateServerTimeoutCountTime + this.circuitRuleDefine.getCircuitTriggerSecond() * 1000;
    }

    @Override
    public boolean attemptExecution() {
        if (circuitOpened.get() == -1) {
            return false;
        } else {
            if (isAfterSleepWindow()) {
                final long now  = System.currentTimeMillis();
                final long sleepWindowTime = this.circuitRuleDefine.getCircuitAttendWindow();
                if(status.get() == Status.HALF_OPEN && (now - this.circuitOpened.get())>=sleepWindowTime){
                    LOG.warn("Masla found attempt request is send but circuit status is half open,so need set open status");
                    status.compareAndSet(Status.HALF_OPEN, Status.OPEN);
                }

                final long circuitOpenTime = circuitOpened.get();
                if(circuitOpenTime < 0){
                    return true;
                }
                if((now - circuitOpenTime)>=sleepWindowTime){
                    this.circuitOpened.compareAndSet(circuitOpenTime,now);
                }

                if (status.compareAndSet(Status.OPEN, Status.HALF_OPEN)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }


    public String showPercent(){
        if(circuitLevel == 0){
            return "10%";
        }else if(circuitLevel == 1){
            return "20%";
        }else if(circuitLevel == 2){
            return "30%";
        }else if(circuitLevel == 3){
            return "40%";
        }else if(circuitLevel == 4 ){
            return "50%";
        }else if(circuitLevel == 5 ){
            return "60%";
        }else if(circuitLevel == 6 ){
            return "70%";
        }else if(circuitLevel == 7 ){
            return "80%";
        }else if(circuitLevel == 8 ){
            return "90%";
        }else if(circuitLevel == 9 ){
            return "100%";
        }else{
            return "0%";
        }
    }


    /**
     * 根据单位时间内服务的错误比例，是否继续熔断还是减少熔断。
     */
    @Override
    public void doUpgradOrDown(Throwable cause, int appType, String appName, int httpStatus, int clusterSize){
        if(this.circuitRuleDefine.getCircuit() >= 0 && this.circuitRuleDefine.isAutoMode()) {

            //只有超时异常才记录
            if(this.checkCircuitException(cause)) {
                this.serverTimeoutCount.incrementAndGet();
            }else{
                this.successCount.incrementAndGet();
            }

            if (isCircuitWindow() && circuitUpdateFlag.compareAndSet(false, true)) {
                int timeOutNums = serverTimeoutCount.getAndSet(0);
                int successNums = successCount.getAndSet(0);
                int requestNums = timeOutNums + successNums;
                updateServerTimeoutCountTime = System.currentTimeMillis();
//                serverTimeoutCount.set(0);

                float errorPercent = Float.valueOf(timeOutNums) / Float.valueOf(requestNums);

                try {
                    if(this.checkCircuitCondition(requestNums, errorPercent, clusterSize)){
                        upgradeCircuit(timeOutNums,requestNums,errorPercent);
                    } else {
                        float circuitLowWaterMark = MaslaSpringContextUtil.getMaslaConfConfigBean().getCircuitUpgradeThreshold();
                        if (circuitLowWaterMark < 0) {
                            circuitLowWaterMark = CircuitConfig.CIRCUIT_LOW_WATER_MARK;
                        }
                        if (this.circuitRuleDefine.isDoDisalbed() && errorPercent <= circuitLowWaterMark) {
                            downCircuit(timeOutNums, requestNums,errorPercent,circuitLowWaterMark);
                        } else {
                            String curPercent = this.showPercent();
//                            String sidName = this.circuitRuleDefine.getCircuitApiUrl() == null ? this.circuitRuleDefine.getAppName() : this.circuitRuleDefine.getCircuitApiUrl();
                            if(this.circuitRuleDefine.isDoDisalbed()) {
                                LOG.warn("Masla found  in {} minute happened {} numbers failed in {} numbers error percent {} > {},so not down also circuit {}", this.circuitRuleDefine.getCircuitTriggerSecond() / 60, timeOutNums, requestNums,errorPercent, circuitLowWaterMark, curPercent);
                            }
                        }
                    }
                }catch (Throwable e){
                    LOG.error("Masla found circuit app {} failed:",this.circuitRuleDefine.getAppName(),e);
                    this.closeCircuit();
                }
                finally {
                    circuitUpdateFlag.set(false);
                }

            }
        }
    }

    private boolean checkCircuitException(Throwable cause){
        return cause != null && (cause instanceof TimeoutException
                || cause instanceof ConnectException
                || Constants.ACQUIRE_CONN_QUEUE_FULL_EXCEPTION.equals(cause.getMessage())
                || Constants.WAIT_TIMEOUT_EXCEPTION.equals(cause.getMessage()));

    }

    private boolean checkCircuitCondition(long requestNums,float errorPercent, int clusterSize){
        float circuitHighWaterMark = this.circuitRuleDefine.getCircuitTriggerPercent();
        if (clusterSize <= 0) {
            return false;
        }
        long hostQpsThreshold = this.circuitRuleDefine.getCircuitThreshold() / clusterSize;
        long seconds = this.circuitRuleDefine.getCircuitTriggerSecond();
        if (seconds <= 0) {
            seconds = 60;
        }
        long runninAppQps = requestNums/seconds;
        if (runninAppQps > hostQpsThreshold
                && errorPercent > circuitHighWaterMark) {
            return true;
        }
        return false;
    }


    private void downCircuit(int timeOutNums,int requestNums,float errorPercent,float upgradeThreshold){
        if(errorPercent <=0 && this.circuitLevel<CircuitConfig.MIDDLE_PERCENT_LEVEL){
            this.closeCircuit();
        }else{
            this.down();
        }
        String curPercent = this.showPercent();
        String sidName = this.circuitRuleDefine.getCircuitApiUrl()==null?this.circuitRuleDefine.getAppName():this.circuitRuleDefine.getCircuitApiUrl();
        LOG.warn("Masla found api {} in {} seconds happened {} numbers timeout in {} numbers error percent {} < {},so do circuit continue down {}", sidName, this.circuitRuleDefine.getCircuitTriggerSecond(), timeOutNums,requestNums,errorPercent,upgradeThreshold,curPercent);
    }


    private void upgradeCircuit(int timeOutNums,int requestNums,float errorPercent){


        if (this.circuitRuleDefine.isDoDisalbed()) {
            if(!supportUpgradOrDown()){
                this.openALLCircuit();
            }else{
                this.upgrade();
            }

        } else {
            this.reset(this.circuitRuleDefine);
            this.circuitRuleDefine.setDoDisalbed(true);
            if (!supportUpgradOrDown()|| this.circuitRuleDefine.getCircuit() == MAX_PERCENT_LEVEL) {
                this.openALLCircuit();
            }
        }
        String curPercent = this.showPercent();
        LOG.warn("Masla found api {} in {} seconds happened {} numbers timeout total numbers {} error percent {} > {}%,so start circuit from {}", this.circuitRuleDefine.getCircuitApiUrl() == null?this.circuitRuleDefine.getAppName():this.circuitRuleDefine.getCircuitApiUrl(), this.circuitRuleDefine.getCircuitTriggerSecond(), timeOutNums, requestNums,errorPercent, this.circuitRuleDefine.getCircuitTriggerPercent() * 100, curPercent);
    }

    @Override
    public void upgrade() {
        //100%
        this.circuitLevel = this.getNextUpgradeLevel(this.circuitLevel);
        if(this.circuitLevel == MAX_PERCENT_LEVEL){
            this.openALLCircuit();
        }

    }

    @Override
    public void fastRecovery() {
        LOG.warn("Masla found api {} mark success so in fast recovery status,so do circuit continue down 50%",circuitRuleDefine.getCircuitApiUrl() == null?circuitRuleDefine.getAppName():circuitRuleDefine.getCircuitApiUrl());
        this.circuitLevel = CIRCUIT_INIT_RECOVER_PERCENT_LEVEL;
        this.successCount.set(0);
        this.updateServerTimeoutCountTime = System.currentTimeMillis();
        this.serverTimeoutCount.set(0);
        this.circuitRuleDefine.setAllDisalbed(false);
        this.circuitRuleDefine.setAutoDiscardOff(true);
    }

    private void openALLCircuit(){
        this.circuitRuleDefine.setAllDisalbed(true);
        if(this.circuitLevel != MAX_PERCENT_LEVEL) {
            this.circuitLevel = MAX_PERCENT_LEVEL;
        }
        //全部熔断后，需要打开自动探测机制
        if(!this.isOpen()) {
            this.open();
        }
    }

    @Override
    public void down() {
        if(this.circuitLevel > DEFAULT_STEP_SIZE){
            this.circuitLevel = this.getNextDownLevel(this.circuitLevel);
        }else{
            closeCircuit();
        }

    }

    @Override
    public void closeCircuit(){
        circuitLevel = -1;
//        this.allowCount.set(0);
        this.successCount.set(0);
        this.serverTimeoutCount.set(0);
        LOG.warn("Masla found api {} auto down finish,so close circuit",circuitRuleDefine.getCircuitApiUrl() == null?circuitRuleDefine.getAppName():circuitRuleDefine.getCircuitApiUrl());
        circuitRuleDefine.reset();
    }


    @Override
    public boolean doCircuit() {

        int requestNums = this.queryCount.incrementAndGet();


        if(!this.circuitRuleDefine.isDoDisalbed()){
            return false;
        }

        if(checkCircuitExpired()){
            return false;
        }

        if(this.circuitRuleDefine.isAllDisalbed()){
            if(this.circuitRuleDefine.isAutoMode()) {
                if (this.attemptExecution()) {
                    return false;
                }
            }
            return true;
        }


        int rs = requestNums % CIRCUIT_MOD_BASE_LEVEL;

        if (rs <= this.circuitLevel) {
            return true;
        }
        return false;
    }

    @Override
    public void reset(CircuitRuleDefine circuitRuleDefine) {
        this.circuitLevel = circuitRuleDefine.getCircuit();
    }


    @Override
    public boolean supportUpgradOrDown(){
        return this.circuitRuleDefine.getUpgradeOrDown()>0?true:false;
    }

    private int getNextDownLevel(int currentLevel){
        return currentLevel - DEFAULT_DOWN_STEP_SIZE;
    }

    private int getNextUpgradeLevel(int currentLevel){
        return currentLevel+DEFAULT_STEP_SIZE;
    }

    private boolean checkCircuitExpired(){
        if(this.circuitRuleDefine.getCircuitTime().get() != -1 && !this.circuitRuleDefine.isAutoMode()){
            long circuitTime = System.currentTimeMillis() - this.circuitRuleDefine.getCircuitTime().get();
            if(this.circuitRuleDefine.getCircuitExpireMinute() != null && circuitTime > this.circuitRuleDefine.getCircuitExpireMinute()){
                LOG.warn("Masla found api {} circuit expired time is exceed,so disabled circuit!!!",this.circuitRuleDefine.getAppName());
                this.circuitRuleDefine.resetCircuitTime();
                this.circuitRuleDefine.setDoDisalbed(false);
                this.circuitRuleDefine.setAutoDiscardOff(true);
                return true;
            }
        }
        return false;
    }


    private int getGroupHostSize(long appId){
        return 1;
    }

    public CircuitRuleDefine getApiCircuitDO() {
        return circuitRuleDefine;
    }

    public void setApiCircuitDO(CircuitRuleDefine apiCircuitDO) {
        this.circuitRuleDefine = apiCircuitDO;
    }
}
