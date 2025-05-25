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
public class NettyPoolArenaMonitorVO implements Comparable{
    private String name;

    private String numActiveAllocations;
    private String numActiveTinyAllocations;
    private String numActiveSmallAllocations;
    private String numActiveNormalAllocations;
    private String numActiveHugeAllocations;

    private String numAllocations;
    private String numTinyAllocations;
    private String numSmallAllocations;
    private String numNormalAllocations;
    private String numHugeAllocations;

    private String numDeallocations;
    private String numTinyDeallocations;
    private String numSmallDeallocations;
    private String numNormalDeallocations;
    private String numHugeDeallocations;

    private String numThreadCaches;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumActiveAllocations() {
        return numActiveAllocations;
    }

    public void setNumActiveAllocations(String numActiveAllocations) {
        this.numActiveAllocations = numActiveAllocations;
    }

    public String getNumActiveTinyAllocations() {
        return numActiveTinyAllocations;
    }

    public void setNumActiveTinyAllocations(String numActiveTinyAllocations) {
        this.numActiveTinyAllocations = numActiveTinyAllocations;
    }

    public String getNumActiveSmallAllocations() {
        return numActiveSmallAllocations;
    }

    public void setNumActiveSmallAllocations(String numActiveSmallAllocations) {
        this.numActiveSmallAllocations = numActiveSmallAllocations;
    }

    public String getNumActiveNormalAllocations() {
        return numActiveNormalAllocations;
    }

    public void setNumActiveNormalAllocations(String numActiveNormalAllocations) {
        this.numActiveNormalAllocations = numActiveNormalAllocations;
    }

    public String getNumActiveHugeAllocations() {
        return numActiveHugeAllocations;
    }

    public void setNumActiveHugeAllocations(String numActiveHugeAllocations) {
        this.numActiveHugeAllocations = numActiveHugeAllocations;
    }

    public String getNumAllocations() {
        return numAllocations;
    }

    public void setNumAllocations(String numAllocations) {
        this.numAllocations = numAllocations;
    }

    public String getNumTinyAllocations() {
        return numTinyAllocations;
    }

    public void setNumTinyAllocations(String numTinyAllocations) {
        this.numTinyAllocations = numTinyAllocations;
    }

    public String getNumSmallAllocations() {
        return numSmallAllocations;
    }

    public void setNumSmallAllocations(String numSmallAllocations) {
        this.numSmallAllocations = numSmallAllocations;
    }

    public String getNumNormalAllocations() {
        return numNormalAllocations;
    }

    public void setNumNormalAllocations(String numNormalAllocations) {
        this.numNormalAllocations = numNormalAllocations;
    }

    public String getNumHugeAllocations() {
        return numHugeAllocations;
    }

    public void setNumHugeAllocations(String numHugeAllocations) {
        this.numHugeAllocations = numHugeAllocations;
    }

    public String getNumDeallocations() {
        return numDeallocations;
    }

    public void setNumDeallocations(String numDeallocations) {
        this.numDeallocations = numDeallocations;
    }

    public String getNumTinyDeallocations() {
        return numTinyDeallocations;
    }

    public void setNumTinyDeallocations(String numTinyDeallocations) {
        this.numTinyDeallocations = numTinyDeallocations;
    }

    public String getNumSmallDeallocations() {
        return numSmallDeallocations;
    }

    public void setNumSmallDeallocations(String numSmallDeallocations) {
        this.numSmallDeallocations = numSmallDeallocations;
    }

    public String getNumNormalDeallocations() {
        return numNormalDeallocations;
    }

    public void setNumNormalDeallocations(String numNormalDeallocations) {
        this.numNormalDeallocations = numNormalDeallocations;
    }

    public String getNumHugeDeallocations() {
        return numHugeDeallocations;
    }

    public void setNumHugeDeallocations(String numHugeDeallocations) {
        this.numHugeDeallocations = numHugeDeallocations;
    }

    public String getNumThreadCaches() {
        return numThreadCaches;
    }

    public void setNumThreadCaches(String numThreadCaches) {
        this.numThreadCaches = numThreadCaches;
    }


    @Override
    public int compareTo(Object o) {
        return this.getName().compareTo(((NettyPoolArenaMonitorVO)o).getName());
    }

    @Override
    public String toString() {
        return "NettyPoolArenaMonitorVO{" +
                "name='" + name + '\'' +
                ", numActiveAllocations='" + numActiveAllocations + '\'' +
                ", numActiveTinyAllocations='" + numActiveTinyAllocations + '\'' +
                ", numActiveSmallAllocations='" + numActiveSmallAllocations + '\'' +
                ", numActiveNormalAllocations='" + numActiveNormalAllocations + '\'' +
                ", numActiveHugeAllocations='" + numActiveHugeAllocations + '\'' +
                ", numAllocations='" + numAllocations + '\'' +
                ", numTinyAllocations='" + numTinyAllocations + '\'' +
                ", numSmallAllocations='" + numSmallAllocations + '\'' +
                ", numNormalAllocations='" + numNormalAllocations + '\'' +
                ", numHugeAllocations='" + numHugeAllocations + '\'' +
                ", numDeallocations='" + numDeallocations + '\'' +
                ", numTinyDeallocations='" + numTinyDeallocations + '\'' +
                ", numSmallDeallocations='" + numSmallDeallocations + '\'' +
                ", numNormalDeallocations='" + numNormalDeallocations + '\'' +
                ", numHugeDeallocations='" + numHugeDeallocations + '\'' +
                ", numThreadCaches='" + numThreadCaches + '\'' +
                '}';
    }
}
