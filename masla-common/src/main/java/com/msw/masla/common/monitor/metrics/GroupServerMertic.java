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
package com.msw.masla.common.monitor.metrics;

import lombok.Data;

@Data
public class GroupServerMertic {

    /**
     * 网关当前机器IP
     */
    private String serverHost;

    /**
     * 时间戳
     */
    private long timestamp;
    /**
     * 接入的app数
     */
    private int appNums;

    /**
     * qps
     */
    private long qps;

    /**
     * 接入端连接数
     */
    private int inSessions;

    /**
     * 后端应用连接数
     */
    private int outSessions;



}
