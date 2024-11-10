package com.msw.masla.core.router;

import com.google.common.util.concurrent.RateLimiter;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.core.router.rule.FlowSelectorRule;
import com.msw.masla.core.router.rule.RouteRule;
import com.msw.masla.core.router.rule.RouteRuleCache;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Author: Gavin.peng
 * Date: 2024/8/3
 * Description:
 */
@Slf4j
public class MaslaRouteRuleParse implements RouteRuleParse {

    @Override
    public List<RouteRule> parseRouteRule(String routeFile) {

        List<RouteRule> routeRules = new ArrayList<>();

        try {
            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(Paths.get(routeFile)));

            // 获取所有键
            Set<String> keys = properties.stringPropertyNames();

            // 解析 service 名称
            keys.stream()
                    .filter(key -> key.startsWith(MaslaRouteRuleProperties.GATEWAY_ROUTE_SERVICE_PREFIX))
                    .forEach(key -> {
                        String[] parts = key.split("\\.");
                        if (parts.length > 4) {
                            RouteRule routeRule = new RouteRule();
                            String serviceName = parts[4];
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

                            String startegyPrefix = MaslaRouteRuleProperties.GATEWAY_ROUTE_SERVICE_PREFIX + serviceName + ".filter.";
                            keys.stream()
                                    .filter(strategy -> strategy.startsWith(startegyPrefix))
                                    .forEach(strategy -> {

                                        FlowSelectorRule flowSelectorRule = new FlowSelectorRule();
                                        flowSelectorRule.setAppName(serviceName);
                                        String serviceFilterPrefix = startegyPrefix + "." + strategy;
                                        String path = properties.getProperty(serviceFilterPrefix + ".path");
                                        if (!StringUtil.isEmptyString(path)) {
                                            flowSelectorRule.setPath(path);
                                        }

                                        String ip = properties.getProperty(serviceFilterPrefix + ".ip");
                                        if (!StringUtil.isEmptyString(ip)) {
                                            flowSelectorRule.setIp(ip);
                                        }

                                        String headerKey = properties.getProperty(serviceFilterPrefix + ".header.key");
                                        if (!StringUtil.isEmptyString(headerKey)) {
                                            flowSelectorRule.setHeaderKey(headerKey);

                                        }

                                        String headerValue = properties.getProperty(serviceFilterPrefix + ".header.value");
                                        if (!StringUtil.isEmptyString(headerValue)) {
                                            flowSelectorRule.setHeaderKeyValue(headerValue);
                                        }

                                        if (strategy.equals("flowLimit")) {
                                            String maxFreq = properties.getProperty(serviceFilterPrefix + ".maxfreq");
                                            if (!StringUtil.isEmptyString(maxFreq)) {
                                                flowSelectorRule.setMaxFreq(Integer.parseInt(maxFreq));
                                            }

                                            String interval = properties.getProperty(serviceFilterPrefix + ".interval");
                                            if (!StringUtil.isEmptyString(interval)) {
                                                flowSelectorRule.setInterval(Integer.parseInt(interval));
                                            }
                                        }

                                        routeRule.getStrategy().put(strategy, flowSelectorRule);
                                    });
                            routeRules.add(routeRule);
                        }
                    });

            //parse global black filter
            String globalFilterPrefix = MaslaRouteRuleProperties.GATEWAY_ROUTE_PREFIX + ".filter.";
            keys.stream()
                    .filter(filter -> filter.startsWith(globalFilterPrefix))
                    .forEach(filter -> {


                        FlowSelectorRule flowSelectorRule = new FlowSelectorRule();
                        String path = properties.getProperty(globalFilterPrefix + ".path");
                        if (!StringUtil.isEmptyString(path)) {
                            flowSelectorRule.setPath(path);
                        }

                        String ip = properties.getProperty(globalFilterPrefix + ".ip");
                        if (!StringUtil.isEmptyString(ip)) {
                            flowSelectorRule.setIp(ip);
                        }

                        String headerKey = properties.getProperty(globalFilterPrefix + ".header.key");
                        if (!StringUtil.isEmptyString(headerKey)) {
                            flowSelectorRule.setHeaderKey(headerKey);

                        }

                        String headerValue = properties.getProperty(globalFilterPrefix + ".header.value");
                        if (!StringUtil.isEmptyString(headerValue)) {
                            flowSelectorRule.setHeaderKeyValue(headerValue);
                        }

                        RouteRuleCache.addGlobalFilter(filter, flowSelectorRule);

                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return routeRules;

    }


}
