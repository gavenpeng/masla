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
