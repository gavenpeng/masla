package com.msw.masla.metrics.http;

import com.msw.masla.common.monitor.metrics.GroupServerMertic;
import com.msw.masla.metrics.frame.AbstractMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupServerMertics extends AbstractMetrics {

    private GroupServerMertic groupServerMertic;

    private GroupServerMertics(){
        this.groupServerMertic = new GroupServerMertic();
    }

    public static class GroupServerMerticsHolder{
        private static GroupServerMertics groupServerMertics = new GroupServerMertics();
    }

    public static GroupServerMertics getInstances(){
        return GroupServerMerticsHolder.groupServerMertics;
    }

    @Override
    public List getMetrics() {

        //如果分组的qps为0，则不发送，防止在下线时流量为0，
        if(this.groupServerMertic.getQps()<=0){
            return Collections.EMPTY_LIST;
        }

        ArrayList merticsDataList = new ArrayList(1);
        GroupServerMertic merticData = new GroupServerMertic();
        merticData.setOutSessions(this.groupServerMertic.getOutSessions());
        merticData.setInSessions(this.groupServerMertic.getInSessions());
        merticData.setQps(this.groupServerMertic.getQps());
        merticData.setAppNums(this.groupServerMertic.getAppNums());
        merticData.setServerHost(this.groupServerMertic.getServerHost());
        merticsDataList.add(merticData);
        this.clear();
        return merticsDataList;
    }

    public void setTimestamp(long timestamp){
        this.groupServerMertic.setTimestamp(timestamp);
    }



    public void setGroupServerHost(String host){
        this.groupServerMertic.setServerHost(host);
    }

    public void setGroupServerQps(long qps){
        this.groupServerMertic.setQps(qps);
    }

    public void setGroupServerAppNums(int appNums){
        this.groupServerMertic.setAppNums(appNums);
    }

    public void setGroupServerInSessions(int inSessions){
        this.groupServerMertic.setInSessions(inSessions);
    }

    public void setGroupServerOutSessions(int outSessions){
        this.groupServerMertic.setOutSessions(outSessions);
    }


    private void clear(){
        this.groupServerMertic.setQps(0);
        this.groupServerMertic.setAppNums(0);
        this.groupServerMertic.setInSessions(0);
        this.groupServerMertic.setOutSessions(0);
    }

}
