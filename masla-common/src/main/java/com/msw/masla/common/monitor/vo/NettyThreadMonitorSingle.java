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
 * Created by gaoyue on 17/7/28.
 */
public class NettyThreadMonitorSingle implements Comparable{
    private String name;
    private String pendingTask;
    private String state;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPendingTask() {
        return pendingTask;
    }

    public void setPendingTask(String pendingTask) {
        this.pendingTask = pendingTask;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public int compareTo(Object o) {
        NettyThreadMonitorSingle b = (NettyThreadMonitorSingle)o;
        if(b.getPendingTask().equals(this.getPendingTask())){
            return this.getName().compareTo(b.getName());
        }else{
            return b.getPendingTask().compareTo(this.getPendingTask());
        }
    }

    @Override
    public String toString() {
        return "NettyThreadMonitorSingle{" +
                "name='" + name + '\'' +
                ", pendingTask='" + pendingTask + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
