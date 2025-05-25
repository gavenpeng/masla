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

/**
 * Author: Gavin.peng
 * Date: 2024/7/14
 * Description:
 */
public interface LoadBalanceFactory<T> {

    LoadBalance<T> getLoadBalance(String loadBalanceName) throws MaslaException;

    void registerLoadBalance(LoadBalance<T> loadBalance);

}
