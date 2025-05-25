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
package com.msw.masla.common.pojo;

import java.util.HashMap;
import java.util.Map;

public class ServiceAppCache {

    private static Map<String,ServiceApp> serviceAppMap = new HashMap<>();

    public static void registerServiceApp(ServiceApp serviceApp) {
        serviceAppMap.put(serviceApp.getName(), serviceApp);
    }

    public static Map<String,ServiceApp> getAppCache() {
        return serviceAppMap;
    }
}
