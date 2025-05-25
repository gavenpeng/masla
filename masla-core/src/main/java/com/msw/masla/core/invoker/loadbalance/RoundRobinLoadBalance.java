package com.msw.masla.core.invoker.loadbalance;

import com.msw.masla.common.util.StringUtil;
import com.msw.masla.core.discovery.nacos.HostProfile;
import com.msw.masla.protocol.http.netty.http.HostInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: Gavin.peng
 * Date: 2024/7/14
 * Description:
 */
@Slf4j
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "RR";

    private final AtomicInteger position;

    public RoundRobinLoadBalance() {
        this.position = new AtomicInteger(0);
    }

    @Override
    protected HostInstance doSelect(List<HostInstance> hostInstances) {
        if (hostInstances == null || hostInstances.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No servers host available for service: ");
            }
            return null;
        }

        int instanceCount = hostInstances.size();

        if (instanceCount == 1) {
            return hostInstances.get(0);
        }

        HostProfile selectedHost = null;
        while (instanceCount-- > 0) {

            int pos = this.position.incrementAndGet() & Integer.MAX_VALUE;
            HostProfile routeHost = (HostProfile) hostInstances.get(pos % hostInstances.size());
            if (!routeHost.isAvailable()) {
                continue;
            }
            if (routeHost.slowStartup()) {
                selectedHost = routeHost;
                continue;
            }

            return selectedHost;
        }

        return selectedHost;


    }

    @Override
    public String getName() {
        return NAME;
    }
}
