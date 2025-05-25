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

import com.google.common.util.concurrent.RateLimiter;
import com.msw.masla.core.discovery.nacos.MaslaNacosServiceDiscovery;
import com.msw.masla.core.router.config.NacosRouteConfig;
import com.msw.masla.core.router.rule.FlowSelectorRule;
import com.msw.masla.core.router.rule.RouteRule;
import com.msw.masla.core.router.rule.RouteRuleCache;
import com.msw.masla.protocol.http.netty.http.HostInstance;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Author: Gavin.peng
 * Date: 2024/7/28
 * Description:
 */
@Data
public class DefaultRouteRuleFactory {

    public static final String FLUSH_CONFIG_KEY = "flushGlobalConfig";

    private MaslaRouteRuleProperties maslaRouteRuleProperties;

    private MaslaNacosServiceDiscovery maslaNacosServiceDiscovery;

    private NacosRouteConfig nacosRouteConfig;

    private final RouteRuleCache routeRuleCache;

    private final RouteRuleParse routeRuleParse;

    private static DefaultRouteRuleFactory factory = new DefaultRouteRuleFactory();

    public static DefaultRouteRuleFactory getDefaultRouteRuleFactoryInstance() {
        return factory;
    }

    private DefaultRouteRuleFactory() {
        this.routeRuleParse = new MaslaRouteRuleParse();
        this.routeRuleCache = new RouteRuleCache();
    }

    public void intRouteRuleFile(MaslaNacosServiceDiscovery maslaNacosServiceDiscovery) throws Exception{
        this.maslaNacosServiceDiscovery = maslaNacosServiceDiscovery;
        this.nacosRouteConfig = new NacosRouteConfig(this);
        this.nacosRouteConfig.init();
        this.loadRouteRule();
    }

    public List<RouteRule> getRouteRule() {
        return maslaRouteRuleProperties.getRoutes();
    }

    public void loadRouteRule() throws Exception {
        Properties newProperties = nacosRouteConfig.getRouteProperties();
        Collection<RouteRule> routeRules = routeRuleParse.parseRouteRule(newProperties);
        this.routeRuleCache.refreshApiCache(routeRules);
    }

    public void initRateLimiter(FlowSelectorRule selectorRule, RouteRule routeRule) {
        synchronized (this) {
            if (routeRule.getRateLimiter() == null) {
                int instanceCount = maslaNacosServiceDiscovery.getServiceInstanceSize(routeRule.getAppName());
                double permitsPerSecond = (double) selectorRule.getMaxFreq() / selectorRule.getInterval() / instanceCount;
                //创建令牌痛限流器
                RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond);
                routeRule.setRateLimiter(rateLimiter);
            }
        }

    }

}
