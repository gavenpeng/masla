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
package com.msw.masla.metrics.http;


import com.msw.masla.metrics.frame.MaslaEventType;

/**
 * Created by Gavin.peng on 2018/1/15
 * Number of requests during rolling window.
 * Number that failed (failure + success + timeout + threadPoolRejected + semaphoreRejected).
 * Error percentage;
 */
public class HealthCounts {

    private final long totalCount;
    private final long errorCount;
    private final int errorPercentage;

    HealthCounts(long total, long error) {
        this.totalCount = total;
        this.errorCount = error;
        if (totalCount > 0) {
            this.errorPercentage = (int) ((double) errorCount / totalCount * 100);
        } else {
            this.errorPercentage = 0;
        }
    }

    private static final HealthCounts EMPTY = new HealthCounts(0, 0);

    public long getTotalRequests() {
        return totalCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public int getErrorPercentage() {
        return errorPercentage;
    }

    public HealthCounts plus(long[] eventTypeCounts) {
        long updatedTotalCount = totalCount;
        long updatedErrorCount = errorCount;

        long successCount = eventTypeCounts[MaslaEventType.SUCCESS.ordinal()];
        long failureCount = eventTypeCounts[MaslaEventType.FAILURE.ordinal()];
        long timeoutCount = eventTypeCounts[MaslaEventType.TIMEOUT.ordinal()];
        long threadPoolRejectedCount = eventTypeCounts[MaslaEventType.THREAD_POOL_REJECTED.ordinal()];
        long semaphoreRejectedCount = eventTypeCounts[MaslaEventType.SEMAPHORE_REJECTED.ordinal()];

        updatedTotalCount += (successCount + failureCount + timeoutCount + threadPoolRejectedCount + semaphoreRejectedCount);
        updatedErrorCount += (failureCount + timeoutCount + threadPoolRejectedCount + semaphoreRejectedCount);
        return new HealthCounts(updatedTotalCount, updatedErrorCount);
    }

    public static HealthCounts empty() {
        return EMPTY;
    }

    public String toString() {
        return "HealthCounts[" + errorCount + " / " + totalCount + " : " + getErrorPercentage() + "%]";
    }
}
