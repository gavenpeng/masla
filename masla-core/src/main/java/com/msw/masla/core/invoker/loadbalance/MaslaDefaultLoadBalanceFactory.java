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
package com.msw.masla.core.invoker.loadbalance;

import com.msw.masla.common.util.StringUtil;
import com.msw.masla.protocol.http.netty.exception.MaslaException;
import com.msw.masla.protocol.http.netty.http.HostInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Gavin.peng
 * Date: 2024/7/14
 * Description:
 * masla load balance factory for register and manager
 */
public class MaslaDefaultLoadBalanceFactory<T> implements LoadBalanceFactory<T> {

    public Map<String, LoadBalance<T>> loadBalanceMap = new HashMap<>();

    @Override
    public LoadBalance<T> getLoadBalance(String loadBalanceName) throws MaslaException {
        if (StringUtil.isEmptyString(loadBalanceName)) {
            loadBalanceName = RoundRobinLoadBalance.NAME;
        }
        return this.loadBalanceMap.get(loadBalanceName);
    }

    @Override
    public void registerLoadBalance(LoadBalance<T> loadBalance) {
        this.loadBalanceMap.put(loadBalance.getName(), loadBalance);
    }

    private static class MaslaLoadBalanceFactoryHold{
        static LoadBalanceFactory<HostInstance> instance = new MaslaDefaultLoadBalanceFactory<>();
    }

    public static LoadBalanceFactory<HostInstance> getInstance(){
        return MaslaLoadBalanceFactoryHold.instance;
    }

}
