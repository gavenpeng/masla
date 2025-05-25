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

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Author: Gavin.peng
 * Date: 2024/7/27
 * Description:
 */
@Data
public class RouteRule {

    /**
     * app name
     */
    private String appName;

    /**
     *
     */
    private String contextRoot;

    /**
     * patter rule
     */
    private String pattern;

    /**
     * request domain match if exist
     */
    private String domain;

    /**
     * rewrite target path support add or reduce path
     */
    private String rewritePath;

    /**
     * rule status
     */
    private boolean enable;

    /**
     * direct match request path
     */
    private String directPath;

    /**
     * direct match request path support rewrite path
     */
    private String directRewritePath;

    /**
     *
     */
    private String loadBalance = "RR";

    /**
     * Application filter
     */
    private Map<String, FlowSelectorRule> strategy = new HashMap<>();

    /**
     * Global filter
     */
    private Map<String, FlowSelectorRule> globalStrategy = new HashMap<>();

    private long timeout = 10000;

    private RateLimiter rateLimiter;

    private Map<String, Object> metadata = new HashMap<>();


    private int order = 0;

    public RouteRule() {

    }





}
