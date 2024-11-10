/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.msw.masla.protocol.http.netty.pool;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.util.concurrent.*;
import io.netty.util.internal.ThrowableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * number of concurrent connections.
 */
public class FixedChannelPool extends SimpleChannelPool {
    protected static final Logger LOG = LoggerFactory.getLogger(FixedChannelPool.class);

    private static final String UNHEALTHY_NOT_OFFERING_BACK_TO_POOL = "Channel is unhealthy not offering it back to pool";

    public static final IllegalStateException FULL_EXCEPTION = ThrowableUtil.unknownStackTrace(
            new IllegalStateException("Too many outstanding acquire operations"),
            FixedChannelPool.class, "acquire0(...)");
    private static final TimeoutException TIMEOUT_EXCEPTION = ThrowableUtil.unknownStackTrace(
            new TimeoutException("Acquire operation took longer then configured maximum time"),
            FixedChannelPool.class, "<init>(...)");

    private static final int RANDOM_ACQUIRE_MILLISECONDS = 50;
    private static final int PENDING_RANDOM_ACQUIRE_MILLISECONDS = 100;
    //protected Random random = new Random();

    public enum AcquireTimeoutAction {
        /**
         * Create a new connection when the timeout is detected.
         */
        NEW,

        /**
         * Fail the {@link Future} of the acquire call with a {@link TimeoutException}.
         */
        FAIL
    }

    private final EventExecutor executor;
    private final EventLoopGroup group;

    private final long acquireTimeoutNanos;
    private final Runnable timeoutTask;

    // There is no need to worry about synchronization as everything that modified the queue or counts is done
    // by the above EventExecutor.
    private final Queue<AcquireTask> pendingAcquireQueue = new ArrayDeque<AcquireTask>();
    private final int maxConnections;
    private final int maxPendingAcquires;
    private int acquiredChannelCount;
    private int pendingAcquireCount;
    private boolean closed;

//    private int maxAcquiredChannelCount = 0;
    private int maxPendingAcquireCount = 0;

    //计算这个机器上的链接池30s内总共需要发送的字节数
    private AtomicLong maxPendingSendBytes = new AtomicLong(0);
    //计算这个机器上的链接池30s内总共发出去的字节数
    private AtomicLong maxSendOKBytes = new AtomicLong(0);

//    private final Lock lock;

    /**
     * Creates a new instance using the {@link ChannelHealthChecker#ACTIVE}.
     *
     * @param bootstrap         the {@link Bootstrap} that is used for connections
     * @param handler           the {@link ChannelPoolHandler} that will be notified for the different pool actions
     * @param maxConnections    the numnber of maximal active connections, once this is reached new tries to acquire
     *                          a {@link Channel} will be delayed until a connection is returned to the pool again.
     */
    public FixedChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler, int maxConnections) {
        this(bootstrap, handler, maxConnections, Integer.MAX_VALUE);
    }

    /**
     * Creates a new instance using the {@link ChannelHealthChecker#ACTIVE}.
     *
     * @param bootstrap             the {@link Bootstrap} that is used for connections
     * @param handler               the {@link ChannelPoolHandler} that will be notified for the different pool actions
     * @param maxConnections        the numnber of maximal active connections, once this is reached new tries to
     *                              acquire a {@link Channel} will be delayed until a connection is returned to the
     *                              pool again.
     * @param maxPendingAcquires    the maximum number of pending acquires. Once this is exceed acquire tries will
     *                              be failed.
     */
    public FixedChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler, int maxConnections, int maxPendingAcquires) {
        this(bootstrap, handler, ChannelHealthChecker.ACTIVE, null, -1, maxConnections, maxPendingAcquires);
    }

    /**
     * Creates a new instance.
     *
     * @param bootstrap             the {@link Bootstrap} that is used for connections
     * @param handler               the {@link ChannelPoolHandler} that will be notified for the different pool actions
     * @param healthCheck           the {@link ChannelHealthChecker} that will be used to check if a {@link Channel} is
     * @param action                the {@link AcquireTimeoutAction} to use or {@code null} if non should be used.
     *                              In this case {@param acquireTimeoutMillis} must be {@code -1}.
     * @param acquireTimeoutMillis  the time (in milliseconds) after which an pending acquire must complete or
     *                              the {@link AcquireTimeoutAction} takes place.
     * @param maxConnections        the numnber of maximal active connections, once this is reached new tries to
     *                              acquire a {@link Channel} will be delayed until a connection is returned to the
     *                              pool again.
     * @param maxPendingAcquires    the maximum number of pending acquires. Once this is exceed acquire tries will
     *                              be failed.
     */
    public FixedChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler,
                            ChannelHealthChecker healthCheck, AcquireTimeoutAction action,
                            final long acquireTimeoutMillis,
                            int maxConnections, int maxPendingAcquires) {
        this(bootstrap, handler, healthCheck, action, acquireTimeoutMillis, maxConnections, maxPendingAcquires, true);
    }

    /**
     * Creates a new instance.
     *
     * @param bootstrap             the {@link Bootstrap} that is used for connections
     * @param handler               the {@link ChannelPoolHandler} that will be notified for the different pool actions
     * @param healthCheck           the {@link ChannelHealthChecker} that will be used to check if a {@link Channel} is
     * @param action                the {@link AcquireTimeoutAction} to use or {@code null} if non should be used.
     *                              In this case {@param acquireTimeoutMillis} must be {@code -1}.
     * @param acquireTimeoutMillis  the time (in milliseconds) after which an pending acquire must complete or
     *                              the {@link AcquireTimeoutAction} takes place.
     * @param maxConnections        the numnber of maximal active connections, once this is reached new tries to
     *                              acquire a {@link Channel} will be delayed until a connection is returned to the
     *                              pool again.
     * @param maxPendingAcquires    the maximum number of pending acquires. Once this is exceed acquire tries will
     *                              be failed.
     * @param releaseHealthCheck    will check channel health before offering back if this parameter set to
     *                              {@code true}.
     */
    public FixedChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler,
                            ChannelHealthChecker healthCheck, AcquireTimeoutAction action,
                            final long acquireTimeoutMillis,
                            int maxConnections, int maxPendingAcquires, final boolean releaseHealthCheck) {
        this(null,bootstrap, handler, healthCheck,action,acquireTimeoutMillis,maxConnections,maxPendingAcquires, releaseHealthCheck);
    }


    /**
     * 支持指定group为链接池的线程，和io 读写的线程的group分开
     * @param group
     * @param bootstrap
     * @param handler
     * @param healthCheck
     * @param action
     * @param acquireTimeoutMillis
     * @param maxConnections
     * @param maxPendingAcquires
     * @param releaseHealthCheck
     */
    public FixedChannelPool(EventLoopGroup group,Bootstrap bootstrap,
                            ChannelPoolHandler handler,
                            ChannelHealthChecker healthCheck, AcquireTimeoutAction action,
                            final long acquireTimeoutMillis,
                            int maxConnections, int maxPendingAcquires, final boolean releaseHealthCheck) {
        super(bootstrap, handler, healthCheck, releaseHealthCheck);
        if (maxConnections < 1) {
            throw new IllegalArgumentException("maxConnections: " + maxConnections + " (expected: >= 1)");
        }
        if (maxPendingAcquires < 1) {
            throw new IllegalArgumentException("maxPendingAcquires: " + maxPendingAcquires + " (expected: >= 1)");
        }
        if (action == null && acquireTimeoutMillis == -1) {
            timeoutTask = null;
            acquireTimeoutNanos = -1;
        } else if (action == null && acquireTimeoutMillis != -1) {
            throw new NullPointerException("action");
        } else if (action != null && acquireTimeoutMillis < 0) {
            throw new IllegalArgumentException("acquireTimeoutMillis: " + acquireTimeoutMillis + " (expected: >= 1)");
        } else {
            acquireTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(acquireTimeoutMillis);
            switch (action) {
                case FAIL:
                    timeoutTask = new TimeoutTask() {
                        @Override
                        public void onTimeout(AcquireTask task) {
                            // Fail the promise as we timed out.
                            task.promise.setFailure(TIMEOUT_EXCEPTION);
                        }
                    };
                    break;
                case NEW:
                    timeoutTask = new TimeoutTask() {
                        @Override
                        public void onTimeout(AcquireTask task) {
                            // Increment the acquire count and delegate to super to actually acquire a Channel which will
                            // create a new connetion.
                            task.acquired();

                            FixedChannelPool.super.acquire(task.promise);
                        }
                    };
                    break;
                default:
                    throw new Error();
            }
        }
        if(group != null) {
            executor = group.next();
        }else{
            executor = bootstrap.config().group().next();
        }
        this.group = group;
        this.maxConnections = maxConnections;
        this.maxPendingAcquires = maxPendingAcquires;
        LOG.info("Channel pool max connection {} , max pending acquires {}",this.maxConnections,this.maxPendingAcquires);

    }


    public long getMaxPendingSendBytes() {
        return maxPendingSendBytes.getAndSet(0);
    }

    public void addMaxSendOKBytes(long sendedBytes) {
        this.maxSendOKBytes.addAndGet(sendedBytes);
    }

    public long getMaxSendOKBytes() {
        return maxSendOKBytes.getAndSet(0);
    }


    @Override
    public Future<Channel> acquire(final Promise<Channel> promise) {
        try {
//            acquire0(promise);
            if (executor.inEventLoop()) {
                acquire0(promise);
            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        acquire0(promise);
                    }
                });
            }
        } catch (Throwable cause) {
            promise.setFailure(cause);
        }
        return promise;
    }



    private void acquire0(final Promise<Channel> promise) {
        assert executor.inEventLoop();

        if (closed) {
            promise.setFailure(new IllegalStateException("FixedChannelPooled was closed"));
            return;
        }
        //尽可能用已经有的连接，http 不同于RPC长连接，连接不会close，http的话，服务端会
        //在一定的时间内主动关闭
        //super.acquire(promise);
//        this.lock.lock();
        try {
            if (acquiredChannelCount < maxConnections) {
                assert acquiredChannelCount >= 0;

                // We need to create a new promise as we need to ensure the AcquireListener runs in the correct
                // EventLoop
                Promise<Channel> p = executor.newPromise();
                AcquireListener l = new AcquireListener(promise);
                l.acquired();
                p.addListener(l);

                //super.acquire(promise);
                super.acquire(p);
            } else {
                if (pendingAcquireCount >= maxPendingAcquires) {
                    promise.setFailure(FULL_EXCEPTION);
                } else {
                    AcquireTask task = new AcquireTask(promise);
                    if (pendingAcquireQueue.offer(task)) {

                        ++pendingAcquireCount;

                        if (pendingAcquireCount > maxPendingAcquireCount) {
                            maxPendingAcquireCount = pendingAcquireCount;
                        }

                        if (timeoutTask != null) {
                            /*
                            如果瞬间有大量的连接，超时时间都一样的话，还是回导致瞬间向服务端建大量的连接
                            这里如果排队时间改为随机值，则认为的减少集中建立连接的问题
                            */
                            task.timeoutFuture = executor.schedule(timeoutTask, acquireTimeoutNanos, TimeUnit.NANOSECONDS);
                            //Random threadLocalRandom = PlatformDependent.threadLocalRandom();
                            //LOG.warn("Wait random time to acquire channel while channel is not available");
                            //task.timeoutFuture = executor.schedule(timeoutTask, threadLocalRandom.nextInt(PENDING_RANDOM_ACQUIRE_MILLISECONDS)+RANDOM_ACQUIRE_MILLISECONDS, TimeUnit.MILLISECONDS);
                        }
                    } else {
                        promise.setFailure(FULL_EXCEPTION);
                    }
                }

                assert pendingAcquireCount > 0;
            }
        }finally {
//            this.lock.unlock();
        }
    }

    @Override
    public Future<Void> release(final Channel channel, final Promise<Void> promise) {
        final Promise<Void> p = executor.newPromise();
        //final Promise<Void> p = channel.newPromise();
        super.release(channel, p.addListener(new FutureListener<Void>() {

            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                assert executor.inEventLoop();

                if (closed) {
                    promise.setFailure(new IllegalStateException("FixedChannelPooled was closed"));
                    return;
                }

                if (future.isSuccess()) {
                    decrementAndRunTaskQueue();
                    promise.setSuccess(null);
                } else {
                    Throwable cause = future.cause();
                    // Check if the exception was not because of we passed the Channel to the wrong pool.
                    // 是否连接的时有可能已经满了，或者已经关闭了,这种情况也需求减少在用中的连接数
                    if (!(cause instanceof IllegalArgumentException)) {
                        decrementAndRunTaskQueue();
                    }
                    /*channel pool 做健康检查，防止已经关闭的连接再放会到队列，如果是释放的时候发现连接
                      已经关闭，则会抛 "Channel is unhealthy not offering it back to pool"异常，
                      这是正常的，所以这里忽略该异常,而且是VoidChannelPromise
                    */
                    if(!future.cause().getMessage().equalsIgnoreCase(UNHEALTHY_NOT_OFFERING_BACK_TO_POOL)) {
                        promise.setFailure(future.cause());
                    }
                }
            }
        }));
        return p;
    }

    public void addPendingOutboundBytes(final long pendingSendBytes){
        //这里用cas实现并发增加，不创建一个任务到队列用单线程增加，是添加任务也需要cas操作
        //还需要new 一个task，还是直接用cas。
        this.maxPendingSendBytes.addAndGet(pendingSendBytes);
    }


    public void decrementAndRunTaskQueue() {

        acquiredChannelCount--;
        // We should never have a negative value.
        assert acquiredChannelCount >= 0;

        // Run the pending acquire tasks before notify the original promise so if the user would
        // try to acquire again from the ChannelFutureListener and the pendingAcquireCount is >=
        // maxPendingAcquires we may be able to run some pending tasks first and so allow to add
        // more.
        runTaskQueue();

    }

    public void runTaskQueue() {
        while (acquiredChannelCount < maxConnections) {
            AcquireTask task = pendingAcquireQueue.poll();
            if (task == null) {
                break;
            }

            long acquirePendingTime = System.currentTimeMillis() - task.getStartAcquireTime();
            if(acquirePendingTime >=50){
                LOG.info("Acquire channel inner pending time {}ms",acquirePendingTime);
            }
            // Cancel the timeout if one was scheduled
            ScheduledFuture<?> timeoutFuture = task.timeoutFuture;
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }

            --pendingAcquireCount;
            task.acquired();

            super.acquire(task.promise);
        }

        // We should never have a negative value.
        assert pendingAcquireCount >= 0;
        assert acquiredChannelCount >= 0;
    }

    // AcquireTask extends AcquireListener to reduce object creations and so GC pressure
    private final class AcquireTask extends AcquireListener {
        final Promise<Channel> promise;
        final long expireNanoTime = System.nanoTime() + acquireTimeoutNanos;
        ScheduledFuture<?> timeoutFuture;
        final long startAcquireTime = System.currentTimeMillis();

        public AcquireTask(Promise<Channel> promise) {
            super(promise);
            // We need to create a new promise as we need to ensure the AcquireListener runs in the correct
            // EventLoop.
            this.promise = executor.<Channel>newPromise().addListener(this);
        }

        public long getStartAcquireTime(){
            return startAcquireTime;
        }
    }

    private abstract class TimeoutTask implements Runnable {
        @Override
        public final void run() {
            assert executor.inEventLoop();
            long nanoTime = System.nanoTime();
            for (;;) {
                AcquireTask task = pendingAcquireQueue.peek();
                // Compare nanoTime as descripted in the javadocs of System.nanoTime()
                //
                // See https://docs.oracle.com/javase/7/docs/api/java/lang/System.html#nanoTime()
                // See https://github.com/netty/netty/issues/3705
                if (task == null || nanoTime - task.expireNanoTime < 0) {
                    break;
                }
                pendingAcquireQueue.remove();

                pendingAcquireCount--;
                onTimeout(task);
            }
        }

        public abstract void onTimeout(AcquireTask task);
    }



    private class AcquireListener implements FutureListener<Channel> {
        private final Promise<Channel> originalPromise;
        protected boolean acquired;

        //protected long normalAcquireTime = System.currentTimeMillis();


        AcquireListener(Promise<Channel> originalPromise) {
            this.originalPromise = originalPromise;
        }

        @Override
        public void operationComplete(Future<Channel> future) throws Exception {
            assert executor.inEventLoop();

            if (closed) {
                originalPromise.setFailure(new IllegalStateException("FixedChannelPooled was closed"));
                return;
            }

            if (future.isSuccess()) {
                originalPromise.setSuccess(future.getNow());
            } else {
                if (acquired) {
                    decrementAndRunTaskQueue();
                } else {
                    runTaskQueue();
                }

                originalPromise.setFailure(future.cause());
            }
        }

        public void acquired() {
            if (acquired) {
                return;
            }
            acquiredChannelCount++;
            acquired = true;
        }
    }

    @Override
    public void close() {
        LOG.info("FixChannelPool is closed!!!");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (!closed) {
                    closed = true;
                    for (;;) {
                        AcquireTask task = pendingAcquireQueue.poll();
                        if (task == null) {
                            break;
                        }
                        ScheduledFuture<?> f = task.timeoutFuture;
                        if (f != null) {
                            f.cancel(false);
                        }
                        task.promise.setFailure(new ClosedChannelException());
                    }
                    acquiredChannelCount = 0;
                    pendingAcquireCount = 0;
                    FixedChannelPool.super.close();
                }
            }
        });
    }

    public int decrementChannelCount(){
        return this.activeChannelCount.decrementAndGet();
    }
    public void incrementChannelCount(){

        int maxChannelCount = this.activeChannelCount.incrementAndGet();
        //记录统计周期内创建链接数的最大值
        if(maxChannelCount > this.maxActiveChannelCount){
            this.maxActiveChannelCount = maxChannelCount;
        }

    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getMaxPendingAcquires() {
        return maxPendingAcquires;
    }

    public boolean isClosed() {
        return closed;
    }

    public void clearMaxMonitorCount() {

        this.maxPendingAcquireCount = 0;
        this.maxActiveChannelCount = 0;
    }

    public int getMaxAcquiredChannelCount() {
        return maxActiveChannelCount;
    }

    public int getMaxPendingAcquireCount() {
        return maxPendingAcquireCount;
    }




}

