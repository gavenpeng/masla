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
