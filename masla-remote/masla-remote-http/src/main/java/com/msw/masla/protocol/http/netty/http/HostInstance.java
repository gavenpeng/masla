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
