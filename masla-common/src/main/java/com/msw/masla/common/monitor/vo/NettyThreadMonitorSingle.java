package com.msw.masla.common.monitor.vo;

/**
 * Created by gaoyue on 17/7/28.
 */
public class NettyThreadMonitorSingle implements Comparable{
    private String name;
    private String pendingTask;
    private String state;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPendingTask() {
        return pendingTask;
    }

    public void setPendingTask(String pendingTask) {
        this.pendingTask = pendingTask;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public int compareTo(Object o) {
        NettyThreadMonitorSingle b = (NettyThreadMonitorSingle)o;
        if(b.getPendingTask().equals(this.getPendingTask())){
            return this.getName().compareTo(b.getName());
        }else{
            return b.getPendingTask().compareTo(this.getPendingTask());
        }
    }

    @Override
    public String toString() {
        return "NettyThreadMonitorSingle{" +
                "name='" + name + '\'' +
                ", pendingTask='" + pendingTask + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
