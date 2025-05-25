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
package com.msw.masla.core.router;

import com.msw.masla.core.router.rule.RouteRule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.style.ToStringCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Gavin.peng
 * Date: 2024/7/27
 * Description:
 */
public class MaslaRouteRuleProperties {

    public static final String MASLA_GATEWAY_ROUTE_RULE_FILE_NAME = "route.properties";

    public static final String GATEWAY_ROUTE_PREFIX = "masla.gateway.routes.";

    public static final String GATEWAY_GLOBAL_FILTER_NAME = "global";

    public static final String GATEWAY_ROUTE_SERVICE_PREFIX = "masla.gateway.routes.service.";

    public static final String ROUTE_SERVICE_PATTERN_PREFIX = "masla.gateway.routes.service.pattern";

    public static final String ROUTE_REWRITE_PATH_PREFIX = "masla.gateway.routes.rewrite.path";

    public static final String ROUTE_SERVICE_DOMAIN_PREFIX = "masla.gateway.routes.service.domain";

    public static final String ROUTE_SERVICE_TIMEOUT_PREFIX = "masla.gateway.routes.service.timeout";

    private final Log logger = LogFactory.getLog(getClass());

    /**
     * List of Routes.
     */
    private List<RouteRule> routes = new ArrayList<>();


    public List<RouteRule> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteRule> routes) {
        this.routes = routes;
        if (routes != null && routes.size() > 0 && logger.isDebugEnabled()) {
            logger.debug("Routes supplied from Gateway Properties: " + routes);
        }
    }


    @Override
    public String toString() {
        return new ToStringCreator(this).append("routes", routes).toString();

    }


}
