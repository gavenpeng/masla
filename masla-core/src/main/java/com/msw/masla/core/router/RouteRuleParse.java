package com.msw.masla.core.router;

import com.msw.masla.core.router.rule.RouteRule;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Author: Gavin.peng
 * Date: 2024/8/3
 * Description:
 */
public interface RouteRuleParse {

    public Collection<RouteRule> parseRouteRule(Properties properties) throws Exception;

    public Collection<RouteRule> parseRouteRule(String routeFile) throws Exception;
}
