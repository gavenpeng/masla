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
package com.msw.masla.common.stream;

import com.msw.masla.common.circuit.CircuitFactory;
import com.msw.masla.common.circuit.CircuitRuleDefine;
import com.msw.masla.common.circuit.MaslaCircuitBreaker;
import com.msw.masla.common.pojo.ServiceApi;
import com.msw.masla.common.pojo.ServiceApp;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Gavin.peng on 2017/11/27.
 */
public class DefaultCircuitPolicy implements CircuitPolicy {

    private long startTime;
    private static final int DEFAULT_QPS = 3000;
    private static final int periodTime = 60000;
    private AtomicInteger discardCount;
    private int curDiscard;
    private  boolean doDiscard = true;
    private  boolean allDiscard = false;


    static class DefaultDiscardPolicyHolder{
        static CircuitPolicy instance = new DefaultCircuitPolicy();
    }

    public static CircuitPolicy getInstance(){
        return DefaultDiscardPolicyHolder.instance;
    }


    @Override
    public void reset(ServiceApi apiDO) {

        int discardPercent = apiDO.getDiscard();
        if(discardPercent>0) {

//            MaslaCircuitBreaker circuitBreaker = CircuitFactory.getCircuitBreaker(apiDO.getName());
//            if(circuitBreaker == null) {
//                circuitBreaker = CircuitFactory.getInstance(apiDO.getName(),apiDO.getDiscard());
//            }
//            //如果配置了自动触发熔断的条件，则需要等条件满足才能做熔断，否则只要配置了就做熔断
//            if(apiDO.getDiscardTriggerMinute() != null && apiDO.getDiscardTriggerMinute() > 0
//                && apiDO.getDiscardTriggerNums() != null && apiDO.getDiscardTriggerNums() > 0){
//            }else{
//                if(!apiDO.isAutoDiscardOff()) {
//                    if (discardPercent == 1) {
//                        //如果是100% 熔断，则开启定时探测器
//                        apiDO.setAllDisalbed(true);
//                        circuitBreaker.open();
//                    } else {
//                        apiDO.setAllDisalbed(false);
//                    }
//                    apiDO.setDoDisalbed(true);
//                }
//            }

        }else{
            apiDO.setDoDisalbed(false);
            apiDO.setAllDisalbed(false);
            apiDO.getCircuitTime().set(-1);
        }

        //init(apiDO.getDiscard(),apiDO.getQueryCount().get());
    }

    @Override
    public void reset(ServiceApp appDO) {
        //app level 没有统计qps
        int discardPercent = appDO.getDiscard();
        if(discardPercent>0) {
            if(!appDO.isAutoDiscardOff()) {
//                appDO.setDoDisalbed(true);
//                if (discardPercent == 1) {
//                    appDO.setAllDisalbed(true);
//                } else {
//                    appDO.setAllDisalbed(false);
//                }
            }
        }else{
//            appDO.setDoDisalbed(false);
        }
        //init(appDO.getDiscard(),2000);
    }

    @Override
    public void configApiCircuit(CircuitRuleDefine apiCircuitDO) {
        int discardPercent = apiCircuitDO.getCircuit();
        if(discardPercent>=0) {

            MaslaCircuitBreaker circuitBreaker = CircuitFactory.getCircuitBreaker(String.valueOf(apiCircuitDO.getId()));
            if(circuitBreaker == null) {
                circuitBreaker = CircuitFactory.getInstance(String.valueOf(apiCircuitDO.getId()),apiCircuitDO);
            }
            //如果配置了自动触发熔断的条件，则需要等条件满足才能做熔断，否则只要配置了就做熔断
            if(!apiCircuitDO.isAutoMode()){
                circuitBreaker.reset(apiCircuitDO);
                if(apiCircuitDO.getCircuitTime().get() == -1
                        && !apiCircuitDO.isAutoDiscardOff()) {
                    if (discardPercent == 9) {
                        //如果是100% 熔断，则开启定时探测器
                        apiCircuitDO.setAllDisalbed(true);
//                        circuitBreaker.open();
                    } else {
                        apiCircuitDO.setAllDisalbed(false);
                    }
                    apiCircuitDO.setDoDisalbed(true);
                    apiCircuitDO.setCircuitTime();
                }
            }

        }else{
            apiCircuitDO.setDoDisalbed(false);
            apiCircuitDO.setAllDisalbed(false);
            apiCircuitDO.getCircuitTime().set(-1);
        }
    }

    @Override
    public void upgrade(ServiceApi apiDO) {

    }

    @Override
    public void down(ServiceApi apiDO) {

    }
}
