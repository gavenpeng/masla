package com.msw.masla.core.async.pool;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
@Component
public class MaslaPushExecutors {


    private static final int DEFAULT_POOL_CORESIZE = 20;
    private static final int DEFAULT_POOL_MAXSIZE = 100;
    private static final int DEFAULT_POOL_IDLETIME = 60;
    private static final int DEFAULT_POOL_QUEUESIZE = 500;


    @NacosValue("${masla.push.thread.pool.core.size}")
    private Integer coreSize;

    @NacosValue("${masla.push.thread.pool.core.maxsize}")
    private Integer maxSize;

    @NacosValue("${masla.push.thread.pool.core.queuesize}")
    private Integer queueSize;

    private ThreadPoolExecutor executor;

    public static ThreadPoolExecutor newCachedThreadPool() {
        //ProperitesContainer pc = getPropContainer(side);
        int coreSize = DEFAULT_POOL_CORESIZE;
        int maxSize = DEFAULT_POOL_MAXSIZE;
        long idleTime = DEFAULT_POOL_IDLETIME;
        int capacity = DEFAULT_POOL_QUEUESIZE;
        BlockingQueue<Runnable> eventQueue = new LinkedBlockingQueue<Runnable>(capacity);
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(coreSize, maxSize, idleTime, TimeUnit.SECONDS, eventQueue, new MaslaThreadFactory(), new MaslaDiscardOldestPolicy());
        return tpe;
    }

    @PostConstruct
    public void init(){
        BlockingQueue<Runnable> eventQueue = new LinkedBlockingQueue<Runnable>(queueSize);
        executor = new ThreadPoolExecutor(coreSize, maxSize, DEFAULT_POOL_IDLETIME, TimeUnit.SECONDS, eventQueue, new MaslaThreadFactory(), new MaslaDiscardOldestPolicy());
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }


}
