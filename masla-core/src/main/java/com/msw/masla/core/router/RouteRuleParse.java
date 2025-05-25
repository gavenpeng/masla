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
