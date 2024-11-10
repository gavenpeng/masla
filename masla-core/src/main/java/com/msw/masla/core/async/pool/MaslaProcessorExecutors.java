package com.msw.masla.core.async.pool;


import java.util.concurrent.*;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
public class MaslaProcessorExecutors {

    private Integer coreSize = 10;

    protected ExecutorService executorService;


    static class MaslaProcessorExecutorsHolder{
        static MaslaProcessorExecutors executors = new MaslaProcessorExecutors();
    }

    public static MaslaProcessorExecutors getInstance(){
        return MaslaProcessorExecutorsHolder.executors;
    }


    public void init(int coreSize){
        this.coreSize = coreSize;
        executorService =  Executors.newFixedThreadPool(this.coreSize);
    }

    public void submitProcessor(Runnable task){
        this.executorService.execute(task);
    }


    public void release(){
        this.executorService.shutdown();
    }
}
