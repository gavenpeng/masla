package com.msw.masla.core.invoker.loadbalance;

import com.msw.masla.protocol.http.netty.exception.MaslaException;

/**
 * Author: Gavin.peng
 * Date: 2024/7/14
 * Description:
 */
public interface LoadBalanceFactory<T> {

    LoadBalance<T> getLoadBalance(String loadBalanceName) throws MaslaException;

    void registerLoadBalance(LoadBalance<T> loadBalance);

}
