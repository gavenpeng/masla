package com.msw.masla.core.invoker.loadbalance;

import com.msw.masla.protocol.http.netty.exception.MaslaException;
import com.msw.masla.protocol.http.netty.http.HostInstance;

import java.util.List;

/**
 * Author: Gavin.peng
 * Date: 2024/7/14
 * Description:
 */
public interface LoadBalance<T> {


    public String getName();

    /**
     *
     * @param stableHosts basic hosts
     * @param tagHosts tag hosts
     * @param routeTag route tag
     * @return selected host instance
     * @throws MaslaException
     */
    T select(List<T> stableHosts, List<T> tagHosts, String routeTag) throws MaslaException;


    /**
     * retry select host instance
     * @param stableHosts basic host instance
     * @param tagHosts tag hosts
     * @param hostInstance selected host instance
     * @param routeTag route tag
     * @return selected host instance
     * @throws MaslaException
     */
    T retrySelect(List<T> stableHosts, List<T> tagHosts, List<HostInstance> selectedHost) throws MaslaException;

}
