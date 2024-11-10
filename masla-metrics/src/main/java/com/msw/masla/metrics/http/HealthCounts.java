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
