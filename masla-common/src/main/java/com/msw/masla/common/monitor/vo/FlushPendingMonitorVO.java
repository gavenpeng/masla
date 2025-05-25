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
package com.msw.masla.common.monitor.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * created by lingxiaochen on 2020/1/19
 */
@Getter
@Setter
public class FlushPendingMonitorVO implements Comparable {

    private String gwGroup;
    private String host;
    private Long ts;
    private Long pendingSize;

    //nginx or redirect
    private String type;

    private String nginxIp;

    private String appName;
    private String appHost;


    public FlushPendingMonitorVO(String gwGroup, String host, Long ts, Long pendingSize, String nginxIp, String appName, String appHost) {
        this.gwGroup = gwGroup;
        this.host = host;
        this.ts = ts;
        this.nginxIp = nginxIp;
        this.appName = appName;
        this.appHost = appHost;
        this.pendingSize = pendingSize;

        this.type = nginxIp == null
                ? "redirect"
                : "nginx";
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
