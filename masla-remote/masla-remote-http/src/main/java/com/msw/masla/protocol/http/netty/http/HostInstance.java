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
package com.msw.masla.protocol.http.netty.http;

import java.util.Map;

/**
 * Author: Gavin.peng
 * Date: 2024/7/20
 * Description:
 * service host instance
 */
public interface HostInstance {

    default String getInstanceId() {
        return null;
    }

    /**
     * @return The service ID as registered.
     */
    String getServiceId();

    /**
     * @return The hostname of the registered service instance.
     */
    String getHost();

    /**
     * @return The port of the registered service instance.
     */
    int getPort();

    /**
     * @return The key / value pair metadata associated with the service instance.
     */
    Map<String, String> getMetadata();

    /**
     * @return The scheme of the service instance.
     */
    default String getScheme() {
        return null;
    }
}
