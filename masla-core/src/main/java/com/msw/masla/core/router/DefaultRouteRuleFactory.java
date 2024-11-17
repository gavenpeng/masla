package com.msw.masla.core.router;

import com.google.common.util.concurrent.RateLimiter;
import com.msw.masla.core.discovery.nacos.MaslaNacosServiceDiscovery;
import com.msw.masla.core.router.config.NacosRouteConfig;
import com.msw.masla.core.router.rule.FlowSelectorRule;
import com.msw.masla.core.router.rule.RouteRule;
import com.msw.masla.core.router.rule.RouteRuleCache;
import com.msw.masla.protocol.http.netty.http.HostInstance;
import lombok.Data;

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

    public void intRouteRuleFile() throws Exception{
        this.nacosRouteConfig = new NacosRouteConfig();
        this.nacosRouteConfig.init();
        this.loadRouteRule();
    }

    public List<RouteRule> getRouteRule() {
        return maslaRouteRuleProperties.getRoutes();
    }

    private void loadRouteRule() throws Exception {
        //String routeFileName = MaslaRouteRuleProperties.MASLA_GATEWAY_ROUTE_RULE_FILE_NAME;
        List<RouteRule> routeRules = routeRuleParse.parseRouteRule(nacosRouteConfig.getRouteProperties());
        this.routeRuleCache.refreshApiCache(routeRules);
    }

    public void initRateLimiter(FlowSelectorRule selectorRule, RouteRule routeRule) {
        synchronized (selectorRule) {
            if (routeRule.getRateLimiter() == null) {
                int instanceCount = maslaNacosServiceDiscovery.getServiceInstanceSize(routeRule.getAppName());
                long permitsPerSecond = selectorRule.getMaxFreq() / selectorRule.getInterval() / instanceCount;
                //创建令牌痛限流器
                RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond);
                routeRule.setRateLimiter(rateLimiter);
            }
        }

    }

}
