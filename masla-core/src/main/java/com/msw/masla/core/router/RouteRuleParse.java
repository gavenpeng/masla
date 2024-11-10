package com.msw.masla.core.router;

import com.msw.masla.core.router.rule.RouteRule;

import java.io.File;
import java.util.List;

/**
 * Author: Gavin.peng
 * Date: 2024/8/3
 * Description:
 */
public interface RouteRuleParse {

    public List<RouteRule> parseRouteRule(String routeFile);
}
