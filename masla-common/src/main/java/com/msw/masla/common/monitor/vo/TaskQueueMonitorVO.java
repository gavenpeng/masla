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
 * created by lingxiaochen on 2019/12/27
 */
@Getter
@Setter
public class TaskQueueMonitorVO implements Comparable {
    private String gwGroup;
    private String host;
    private String loopGroupType;
    private Integer pendingTask;
    private Integer loopGroupSize;
    private Long ts;

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
