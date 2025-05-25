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
package com.msw.masla.core.router.rule;

import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.msw.masla.common.constant.Constants.HTTP_SCHEMA;

/**
 * Author: Gavin.peng
 * Date: 2024/8/3
 * Description:
 */
@Slf4j
public class RouteRuleCache  {

    private static Map<String, ServiceApp> ROUTE_APP_CACHE;

    private static Map<String, RouteRule> DIRECT_ROUTE_RULE_CACHE;

    private static Map<String, FlowSelectorRule> GLOBAL_ROUTE_RULE_CACHE = new HashMap<>();

    private static Map<String, Pattern> URL_PATTERN_CACHE;

    private static Map<Pattern, RouteRule> NO_CONTEXT_API_PATTERN_CACHE;

    private static Map<String, TreeMap<Pattern, RouteRule>> APP_PATTERN_ROUTE_RULE_CACHE;

    private final RouteRuleCache.RouteRuleComparator routeRuleComparator = new RouteRuleCache.RouteRuleComparator();



    public void flush() {

    }


    public void reset() {

    }

    public void refreshApiCache(Collection<RouteRule> routeRules){

        //如果数据库出现异常，一个都查不到，则不更新缓存，防止数据被清空
        if (null == routeRules || routeRules.isEmpty()) {
            log.error("masla route rule cache load failed, route rule is empty!!!");
            return;
        }

        if(ROUTE_APP_CACHE == null){
            ROUTE_APP_CACHE = new ConcurrentHashMap<>();
        }

        if(DIRECT_ROUTE_RULE_CACHE == null){
            DIRECT_ROUTE_RULE_CACHE = new ConcurrentHashMap<>();
        }

        if(URL_PATTERN_CACHE == null){
            URL_PATTERN_CACHE = new HashMap<>();
        }

        if(APP_PATTERN_ROUTE_RULE_CACHE == null){
            APP_PATTERN_ROUTE_RULE_CACHE = new ConcurrentHashMap<>();
        }

        Map<String, RouteRule> allDOMap = new HashMap<>();
        Map<String, RouteRule> directRuleMap = new HashMap<>();
        Map<String, Pattern> patternMap = new HashMap<String, Pattern>();


        for (RouteRule routeRule : routeRules) {
            allDOMap.put(routeRule.getAppName(), routeRule);
            ServiceApp serviceApp = new ServiceApp();
            serviceApp.setName(routeRule.getAppName());
            serviceApp.setLoadBalanceName(routeRule.getLoadBalance());
            serviceApp.initDefaultCircuit();

            if (!ROUTE_APP_CACHE.containsKey(routeRule.getAppName())) {
                ROUTE_APP_CACHE.put(routeRule.getAppName(), serviceApp);
            }

            String directPath = routeRule.getDirectPath();
            if (!StringUtil.isEmptyString(directPath)) {
                DIRECT_ROUTE_RULE_CACHE.put(directPath, routeRule);
            }

            String patternRule = routeRule.getPattern();
            if (StringUtil.isEmptyString(patternRule)) {
                DIRECT_ROUTE_RULE_CACHE.put(routeRule.getDomain(), routeRule);
            }
            if (!patternRule.startsWith(Constants.PATTEN_START_CHAR)) {
                DIRECT_ROUTE_RULE_CACHE.put(patternRule, routeRule);
                directRuleMap.put(patternRule, routeRule);
            } else {
                Pattern pattern = URL_PATTERN_CACHE.get(patternRule);
                //是新增的正则api
                if (pattern == null) {
                    pattern = Pattern.compile(patternRule, Pattern.CASE_INSENSITIVE);
                    URL_PATTERN_CACHE.put(patternRule, pattern);
                    addRouteRuleCommonPattern(routeRule, pattern);
                } else {
                    //存在则更新
                    updateCacheRouteRuleFromCommon(routeRule, pattern);
                }
                patternMap.put(routeRule.getPattern(), pattern);
            }
        }


        //TODO 需要考虑重用，在api 数量较多的时候

        delOldRouteRuleCache(directRuleMap,DIRECT_ROUTE_RULE_CACHE);
        deleteOldRegexApiCache(patternMap, URL_PATTERN_CACHE);
        directRuleMap.clear();
        patternMap.clear();
        allDOMap =  null;

    }


    private void addRouteRuleCommonPattern(RouteRule routeRule, Pattern pattern){
        TreeMap<Pattern, RouteRule> patternApiDOMap = APP_PATTERN_ROUTE_RULE_CACHE.get(routeRule.getAppName());
        if (patternApiDOMap == null) {
            patternApiDOMap = new TreeMap<>(routeRuleComparator);
            APP_PATTERN_ROUTE_RULE_CACHE.put(routeRule.getAppName(), patternApiDOMap);
            patternApiDOMap.entrySet();
        }
        patternApiDOMap.put(pattern, routeRule);
    }

    private void updateCacheRouteRuleFromCommon(RouteRule routeRule, Pattern pattern){
        Map<Pattern, RouteRule> patternRouteRuleMap = APP_PATTERN_ROUTE_RULE_CACHE.get(routeRule.getAppName());
        if(patternRouteRuleMap != null) {
            patternRouteRuleMap.put(pattern, routeRule);
        }

    }



    private RouteRule deleteApiPatternFromCommon(RouteRule routeRule, Pattern pattern){
        if(!StringUtil.isEmptyString(routeRule.getAppName())) {
            Map<Pattern, RouteRule> patternApiDOMap = APP_PATTERN_ROUTE_RULE_CACHE.get(routeRule.getAppName());
            if(patternApiDOMap != null) {
                return patternApiDOMap.remove(pattern);
            }else{
                //TODO 这里是context root被修改，总的url没有变导致的
                return null;
            }
        }else{
            return NO_CONTEXT_API_PATTERN_CACHE.remove(pattern);
        }
    }


    private void delOldApiCache(Map<String, RouteRule> apiDOMap, Map<String, RouteRule> cacheApiDOMap){
        Set<Map.Entry<String, RouteRule>> hostEntry = cacheApiDOMap.entrySet();
        List<String> delHostList = new ArrayList<String>(4);
        for(Map.Entry<String, RouteRule> entry:hostEntry){
            if (!apiDOMap.containsKey(entry.getKey())) {
                delHostList.add(entry.getKey());
            }

        }
        if(delHostList.size()>0) {
            for (String path : delHostList) {
                cacheApiDOMap.remove(path);
            }
        }
    }

    private void delOldRouteRuleCache(Map<String, RouteRule> routeRuleMap, Map<String, RouteRule> cacheRouteRuleMap){
        Set<Map.Entry<String, RouteRule>> hostEntry = cacheRouteRuleMap.entrySet();
        List<String> delHostList = new ArrayList<String>(4);
        for(Map.Entry<String, RouteRule> entry:hostEntry){
            if (!routeRuleMap.containsKey(entry.getKey())) {
                delHostList.add(entry.getKey());
            }

        }
        if(delHostList.size()>0) {
            for (String appName : delHostList) {
                cacheRouteRuleMap.remove(appName);
            }
        }
    }

    private void deleteOldRegexApiCache(Map<String, Pattern> routeRuleRegexMap,Map<String, Pattern> cacheRouteRuleMap){
        Set<Map.Entry<String,Pattern>> hostEntry = cacheRouteRuleMap.entrySet();
        List<String> delHostList = new ArrayList<String>(cacheRouteRuleMap.size());
        for(Map.Entry<String,Pattern> entry:hostEntry){
            if (!routeRuleRegexMap.containsKey(entry.getKey())) {
                delHostList.add(entry.getKey());
            }

        }
        if(delHostList.size()>0) {
            for (String path : delHostList) {
                Pattern pattern = cacheRouteRuleMap.remove(path);
                if(pattern != null){
                    NO_CONTEXT_API_PATTERN_CACHE.remove(pattern);
                    String contextRoot = "/"+path.split("/")[1];
                    Map<Pattern, RouteRule> apiDOMap = APP_PATTERN_ROUTE_RULE_CACHE.get(contextRoot);
                    if(apiDOMap != null){
                        apiDOMap.remove(pattern);
                    }
                }

            }
            delHostList.clear();
        }
    }

    private class RouteRuleComparator implements Comparator<Pattern> {

        @Override
        public int compare(Pattern o1, Pattern o2) {
            int diff = o2.pattern().length() - o1.pattern().length();
            if(diff != 0){
                return diff;
            }
            //长度相等
            return o2.hashCode() - o1.hashCode();
        }
    }

    public static Map<String, RouteRule> getDirectRouteRuleCache() {
         return DIRECT_ROUTE_RULE_CACHE;
    }

    public static Map<String, Pattern> getPatterRouteRule() {
        return URL_PATTERN_CACHE;
    }

    public static Map<String, TreeMap<Pattern, RouteRule>> getAppPatterRouteRule() {
        return APP_PATTERN_ROUTE_RULE_CACHE;
    }

    public static ServiceApp getRouteAppCache(String serviceName) {
        return ROUTE_APP_CACHE.get(serviceName);
    }

    public static FlowSelectorRule getGlobalFilter(String filter) {
        return GLOBAL_ROUTE_RULE_CACHE.get(filter);
    }

    public static void addGlobalFilter(String filterName, FlowSelectorRule flowSelectorRule) {
        GLOBAL_ROUTE_RULE_CACHE.put(filterName, flowSelectorRule);
    }

}
