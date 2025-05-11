package com.msw.masla.core.invoker.loadbalance;

import com.msw.masla.common.util.StringUtil;
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
        if (hostInstances.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No servers host available for service: ");
            }
            return null;
        }

        int pos = this.position.incrementAndGet() & Integer.MAX_VALUE;

        return hostInstances.get(pos % hostInstances.size());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
