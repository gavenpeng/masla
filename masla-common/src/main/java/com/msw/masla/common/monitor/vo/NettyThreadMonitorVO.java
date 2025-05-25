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
 * Created by gaoyue on 17/7/20.
 */
public class NettyThreadMonitorVO implements Comparable{
    private String name1 = "";
    private String pendingTask1 = "";
    private String state1 = "";
    private String name2 = "";
    private String pendingTask2 = "";
    private String state2 = "";
    private String name3 = "";
    private String pendingTask3 = "";
    private String state3 = "";

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getPendingTask1() {
        return pendingTask1;
    }

    public void setPendingTask1(String pendingTask1) {
        this.pendingTask1 = pendingTask1;
    }

    public String getState1() {
        return state1;
    }

    public void setState1(String state1) {
        this.state1 = state1;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getPendingTask2() {
        return pendingTask2;
    }

    public void setPendingTask2(String pendingTask2) {
        this.pendingTask2 = pendingTask2;
    }

    public String getState2() {
        return state2;
    }

    public void setState2(String state2) {
        this.state2 = state2;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }

    public String getPendingTask3() {
        return pendingTask3;
    }

    public void setPendingTask3(String pendingTask3) {
        this.pendingTask3 = pendingTask3;
    }

    public String getState3() {
        return state3;
    }

    public void setState3(String state3) {
        this.state3 = state3;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public String toString() {
        return "NettyThreadMonitorVO{" +
                "name1='" + name1 + '\'' +
                ", pendingTask1='" + pendingTask1 + '\'' +
                ", state1='" + state1 + '\'' +
                ", name2='" + name2 + '\'' +
                ", pendingTask2='" + pendingTask2 + '\'' +
                ", state2='" + state2 + '\'' +
                ", name3='" + name3 + '\'' +
                ", pendingTask3='" + pendingTask3 + '\'' +
                ", state3='" + state3 + '\'' +
                '}';
    }
}
