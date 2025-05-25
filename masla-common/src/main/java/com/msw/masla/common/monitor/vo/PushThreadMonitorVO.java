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

/**
 * Created by gaoyue on 17/7/14.
 */
public class PushThreadMonitorVO {
    private String corePoolSize;
    private String maximumPoolSize;
    private String workQueueSize;
    private String poolInfo;

    public String getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(String corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public String getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(String maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public String getWorkQueueSize() {
        return workQueueSize;
    }

    public void setWorkQueueSize(String workQueueSize) {
        this.workQueueSize = workQueueSize;
    }

    public String getPoolInfo() {
        return poolInfo;
    }

    public void setPoolInfo(String poolInfo) {
        this.poolInfo = poolInfo;
    }

    @Override
    public String toString() {
        return "PushThreadMonitorVO{" +
                "corePoolSize='" + corePoolSize + '\'' +
                ", maximumPoolSize='" + maximumPoolSize + '\'' +
                ", workQueueSize='" + workQueueSize + '\'' +
                ", poolInfo='" + poolInfo + '\'' +
                '}';
    }
}
