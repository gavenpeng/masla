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

/**
 * Author: Gavin.peng
 * Date: 2024/7/28
 * Description:
 */
@Data
public class DefaultRouteRuleFactory {

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
        Collection<RouteRule> routeRules = routeRuleParse.parseRouteRule(nacosRouteConfig.getRouteProperties());
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
