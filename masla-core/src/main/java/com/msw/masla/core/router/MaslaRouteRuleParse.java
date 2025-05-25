package com.msw.masla.core.router;

import com.msw.masla.common.util.StringUtil;
import com.msw.masla.core.router.rule.FlowSelectorRule;
import com.msw.masla.core.router.rule.RouteRule;
import com.msw.masla.core.router.rule.RouteRuleCache;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.msw.masla.core.router.MaslaRouteRuleProperties.GATEWAY_GLOBAL_FILTER_NAME;


/**
 * Author: Gavin.peng
 * Date: 2024/8/3
 * Description:
 */
@Slf4j
public class MaslaRouteRuleParse implements RouteRuleParse {


    @Override
    public Collection<RouteRule> parseRouteRule(Properties routeFileProperties) {
        return doParseRouteRule(routeFileProperties);
    }


    private Collection<RouteRule> doParseRouteRule(Properties properties){
        Map<String, RouteRule> routeRuleMap = new HashMap<>();
        Set<String> keys = properties.stringPropertyNames();
        keys.stream()
                .filter(key -> key.startsWith(MaslaRouteRuleProperties.GATEWAY_ROUTE_SERVICE_PREFIX))
                .forEach(key -> {
                    String[] parts = key.split("\\.");
                    if (parts.length > 4) {

                        String serviceName = parts[4];
                        if (!routeRuleMap.containsKey(serviceName)) {
                            RouteRule routeRule = new RouteRule();
                            routeRule.setAppName(serviceName);
                            String pattern = properties.getProperty(MaslaRouteRuleProperties.GATEWAY_ROUTE_SERVICE_PREFIX + serviceName + ".pattern");
                            if (!StringUtil.isEmptyString(pattern)) {
                                routeRule.setPattern(pattern);
                            } else {
                                routeRule.setPattern("/" + serviceName);
                            }

                            String rewritePath = properties.getProperty(MaslaRouteRuleProperties.GATEWAY_ROUTE_SERVICE_PREFIX + serviceName + ".rewritePath");
                            if (!StringUtil.isEmptyString(rewritePath)) {
                                routeRule.setRewritePath(rewritePath);
                            }

                            String domain = properties.getProperty(MaslaRouteRuleProperties.GATEWAY_ROUTE_SERVICE_PREFIX + serviceName + ".domain");
                            if (!StringUtil.isEmptyString(domain)) {
                                routeRule.setDomain(domain);
                            }

                            String lb = properties.getProperty(MaslaRouteRuleProperties.GATEWAY_ROUTE_SERVICE_PREFIX + serviceName + ".lb");
                            if (!StringUtil.isEmptyString(lb)) {
                                routeRule.setLoadBalance(lb);
                            }

                            String timeout = properties.getProperty(MaslaRouteRuleProperties.GATEWAY_ROUTE_SERVICE_PREFIX + serviceName + ".timeout");
                            if (!StringUtil.isEmptyString(timeout)) {
                                routeRule.setTimeout(Long.parseLong(timeout));
                            }

                            parseServiceFilterConfig(serviceName, properties, routeRule, false);
                            routeRuleMap.put(serviceName, routeRule);
                        }
                    }
                });

        parseServiceFilterConfig(GATEWAY_GLOBAL_FILTER_NAME, properties, null, true);

        return routeRuleMap.values();
    }

    @Override
    public Collection<RouteRule> parseRouteRule(String routeFile) throws Exception {

        Properties properties = new Properties();
        properties.load(Files.newBufferedReader(Paths.get(routeFile)));
        return doParseRouteRule(properties);

    }

    private void parseServiceFilterConfig(String serviceName, Properties properties, RouteRule routeRule, boolean global) {
        Set<String> keys = properties.stringPropertyNames();
        AtomicReference<String> filterStrategyName = new AtomicReference<>();
        AtomicReference<FlowSelectorRule> flowSelectorRule = new AtomicReference<>();
        String filterPrefix = global? MaslaRouteRuleProperties.GATEWAY_ROUTE_PREFIX : MaslaRouteRuleProperties.GATEWAY_ROUTE_SERVICE_PREFIX;
        final String serviceFilterPrefix = filterPrefix + serviceName + ".filter.";
        keys.stream()
                .filter(strategy -> strategy.startsWith(serviceFilterPrefix))
                .forEach(strategy -> {
                    if (flowSelectorRule.get() == null) {
                        flowSelectorRule.set(new FlowSelectorRule());
                        String filterName = extractFilterValueKey(strategy, serviceFilterPrefix);
                        if (!global) {
                            log.info("Masla init service {} filter {} config", serviceName, filterName);
                        } else {
                            log.info("Masla init {} filter {} config", serviceName, filterName);
                        }
                        filterStrategyName.set(filterName);
                    }

                    String value = properties.getProperty(strategy);
                    if (!StringUtil.isEmptyString(value)) {

                        if (strategy.endsWith(".path")) {
                            flowSelectorRule.get().setPath(value);

                        } else if (strategy.endsWith(".ip")) {
                            flowSelectorRule.get().setIp(value);

                        } else if (strategy.endsWith(".header.key")) {
                            flowSelectorRule.get().setHeaderKey(value);

                        } else if (strategy.endsWith(".header.value")) {
                            flowSelectorRule.get().setHeaderKeyValue(value);

                        } else if (strategy.endsWith(".maxfreq")) {
                            flowSelectorRule.get().setMaxFreq(Integer.parseInt(value));

                        } else if (strategy.endsWith(".interval")) {
                            flowSelectorRule.get().setInterval(Integer.parseInt(value));

                        }

                    }

                });

        if (flowSelectorRule.get() != null && !global) {
            routeRule.getStrategy().put(filterStrategyName.get(), flowSelectorRule.get());
        } else if (global) {
            RouteRuleCache.addGlobalFilter(filterStrategyName.get(), flowSelectorRule.get());
        }

    }

    public static String extractFilterValueKey(String fullKey, String prefix) {
        if (fullKey.startsWith(prefix)) {
            String suffix = fullKey.substring(prefix.length());
            int dotIndex = suffix.indexOf('.');
            if (dotIndex != -1) {
                return suffix.substring(0, dotIndex);  // 取第一个字段
            } else {
                return suffix;  // 后面没有点，直接返回整个剩余部分
            }
        }
        return null;
    }


}
