package com.msw.masla.common.stream;

import com.msw.masla.common.pojo.ServiceApi;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.util.HttpUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Gavin.peng on 2017/11/27.
 */
public class DefaultDiscardPolicy implements DiscardPolicy {

    private long startTime;
    private static final int DEFAULT_QPS = 3000;
    private static final int periodTime = 60000;
    private AtomicInteger discardCount;
    private int curDiscard;
    private  boolean doDiscard = true;
    private  boolean allDiscard = false;


    static class DefaultDiscardPolicyHolder{
        static  DiscardPolicy instance = new DefaultDiscardPolicy();
    }

    public static DiscardPolicy getInstance(){
        return DefaultDiscardPolicyHolder.instance;
    }



    @Override
    public boolean discard() {
        //在一个周期内,熔断不考虑并发问题。
        if(doDiscard) {
            //完全熔断
            if(allDiscard){
                return true;
            }
            if (System.currentTimeMillis() - this.startTime <= this.periodTime) {
                if (discardCount.decrementAndGet() > 0) {
                    //前面的请求放行,熔断后面的请求
                    return false;
                } else {
                    return true;
                }
            } else {
                this.startTime = System.currentTimeMillis();
                this.discardCount.set(this.curDiscard);
            }
        }
        return false;
    }

    @Override
    public void reset(ServiceApi apiDO) {

        int discardPercent = apiDO.getDiscard();
        if(discardPercent>0) {

            if (discardPercent == 1) {
                apiDO.setAllDisalbed(true);
            } else {
                apiDO.setAllDisalbed(false);
            }

            //如果配置了自动触发熔断的条件，则需要等条件满足才能做熔断，否则只要配置了就做熔断
            if(apiDO.getDiscardTriggerMinute() != null && apiDO.getDiscardTriggerMinute() > 0
                && apiDO.getDiscardTriggerNums() != null && apiDO.getDiscardTriggerNums() > 0){
            }else{
                if(!apiDO.isAutoDiscardOff()) {
                    apiDO.setDoDisalbed(true);
                }
            }

        }else{
            apiDO.setDoDisalbed(false);
            apiDO.setAllDisalbed(false);
            apiDO.resetCircuitTime();
        }

        //init(apiDO.getDiscard(),apiDO.getQueryCount().get());
    }

    @Override
    public void reset(ServiceApp appDO) {
        //app level 没有统计qps
        int discardPercent = appDO.getDiscard();
        if(discardPercent>0) {
//            appDO.setDoDisalbed(true);
//            if (discardPercent == 1) {
//                appDO.setAllDisalbed(true);
//            } else {
//                appDO.setAllDisalbed(false);
//            }
            //this.startTime = System.currentTimeMillis();
//            int currentQps = appDO.getQueryCount().get();
//            if (currentQps <= 0) {
//                currentQps = DEFAULT_QPS;
//            }
//            this.curDiscard = currentQps * (100 - discardPercent) / 100;
//            appDO.setAllowCount(curDiscard);
        }else{
//            appDO.setDoDisalbed(false);
//            appDO.setAllDisalbed(false);
        }
        //init(appDO.getDiscard(),2000);
    }

    private void init(int discardPercent,int currentQps){
        if(discardPercent>0) {
            if(discardPercent == 100){
                this.allDiscard = true;
            }else {
                this.allDiscard = false;
            }
            this.startTime = System.currentTimeMillis();
            if(currentQps <= 0){
                currentQps = DEFAULT_QPS;
            }
            this.curDiscard = currentQps * discardPercent / 100;
            if(this.discardCount == null){
                this.discardCount = new AtomicInteger(curDiscard);
            }
        }else{
            doDiscard = false;
        }

    }

    @Override
    public String genernateDiscardResponse(String url) {
        try {
            return HttpUtil.sendGetReturnString(url, null);
        }catch (Throwable e){
            return null;
        }
    }
}
