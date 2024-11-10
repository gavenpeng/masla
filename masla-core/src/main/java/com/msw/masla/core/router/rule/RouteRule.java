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
