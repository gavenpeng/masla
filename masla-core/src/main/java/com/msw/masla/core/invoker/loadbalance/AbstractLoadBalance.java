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
import com.msw.masla.core.invoker.loadbalance.LoadBalance;
import com.msw.masla.protocol.http.netty.exception.MaslaException;
import com.msw.masla.protocol.http.netty.http.HostInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.msw.masla.common.constant.Constants.MASLA_ROUTE_TAG;

/**
 * Author: Gavin.peng
 * Date: 2024/7/14
 * Description:
 */
public abstract class AbstractLoadBalance implements LoadBalance<HostInstance> {

    @Override
    public HostInstance select(List<HostInstance> stableHosts, List<HostInstance> tagHosts, String routeTag) throws MaslaException {
        HostInstance t = null;
        if (!StringUtil.isEmptyString(routeTag) && tagHosts != null && !tagHosts.isEmpty()) {
            t = doTagRoute(tagHosts, routeTag);
        }
        if (t != null) {
            return t;
        }

        return doSelect(stableHosts);

    }

    protected abstract HostInstance doSelect(List<HostInstance> hosts);

    @Override
    public HostInstance retrySelect(List<HostInstance> stableHosts, List<HostInstance> tagHosts, List<HostInstance> selectedHost) throws MaslaException {
        HostInstance t = null;
        List<HostInstance> noUsedHostList = new ArrayList<>(stableHosts.size());
        selectedHost.forEach(hostInstance -> {
            if (!selectedHost.contains(hostInstance)) {
                noUsedHostList.add(hostInstance);
            }
        });
        return doSelect(noUsedHostList);

    }

    protected HostInstance doTagRoute(List<HostInstance> hosts, String routeTag) {
        List<HostInstance> tagList = new ArrayList<>(hosts.size());
        for (HostInstance hostInstance : hosts) {
            Map<String, String> metaData = hostInstance.getMetadata();
            if (metaData.containsKey(MASLA_ROUTE_TAG) && routeTag.equals(metaData.get(MASLA_ROUTE_TAG))) {
                tagList.add(hostInstance);
            }
        }

        if (tagList.size() == 1) {
            return tagList.get(0);
        }
        if (!tagList.isEmpty()) {
            return tagList.get(ThreadLocalRandom.current().nextInt(tagList.size()));
        }

        return null;

    }

}
