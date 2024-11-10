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
