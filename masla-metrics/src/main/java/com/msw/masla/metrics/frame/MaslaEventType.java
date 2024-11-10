package com.msw.masla.metrics.frame;

/**
 * Created by Gavin.peng on 2018/1/15.
 */
public enum MaslaEventType {
    EMIT(false),
    SUCCESS(true),
    FAILURE(false),
    TIMEOUT(false),
    BAD_REQUEST(true),
    SHORT_CIRCUITED(false),
    THREAD_POOL_REJECTED(false),
    SEMAPHORE_REJECTED(false);

    private final boolean isTerminal;

    MaslaEventType(boolean isTerminal) {
        this.isTerminal = isTerminal;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    }
