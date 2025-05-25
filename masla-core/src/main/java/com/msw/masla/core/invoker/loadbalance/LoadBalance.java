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
