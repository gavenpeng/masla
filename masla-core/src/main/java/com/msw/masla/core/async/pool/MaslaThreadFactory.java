package com.msw.masla.core.async.pool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
public class MaslaThreadFactory implements ThreadFactory {

    private static final AtomicInteger nameCounter = new AtomicInteger(0);

    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "Masla-push-thread-"+nameCounter.incrementAndGet());
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
