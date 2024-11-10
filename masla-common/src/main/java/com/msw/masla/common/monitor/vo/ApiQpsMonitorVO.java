package com.msw.masla.common.monitor.vo;

/**
 * Created by gaoyue on 17/8/4.
 */
public class ApiQpsMonitorVO implements Comparable{
    private String apiName;
    private String appName;
    private String contextRoot;
    private String qps;
    private String tp90;
    private String totalCost;
    private String serverCostTimeSum;
    private String tomcatTimeOutCount;
    private String acquireConnectTimeSum;
    private String queueWatiTimeSum;
    private String isBan;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    public String getQps() {
        return qps;
    }

    public void setQps(String qps) {
        this.qps = qps;
    }

    public String getServerCostTimeSum() {
        return serverCostTimeSum;
    }

    public void setServerCostTimeSum(String serverCostTimeSum) {
        this.serverCostTimeSum = serverCostTimeSum;
    }

    public String getTomcatTimeOutCount() {
        return tomcatTimeOutCount;
    }

    public void setTomcatTimeOutCount(String tomcatTimeOutCount) {
        this.tomcatTimeOutCount = tomcatTimeOutCount;
    }

    public String getAcquireConnectTimeSum() {
        return acquireConnectTimeSum;
    }

    public void setAcquireConnectTimeSum(String acquireConnectTimeSum) {
        this.acquireConnectTimeSum = acquireConnectTimeSum;
    }

    public String getQueueWatiTimeSum() {
        return queueWatiTimeSum;
    }

    public void setQueueWatiTimeSum(String queueWatiTimeSum) {
        this.queueWatiTimeSum = queueWatiTimeSum;
    }

    public String getTp90() {
        return tp90;
    }

    public void setTp90(String tp90) {
        this.tp90 = tp90;
    }

    public String getIsBan() {
        return isBan;
    }

    public void setIsBan(String isBan) {
        this.isBan = isBan;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    @Override
    public int compareTo(Object o) {
        ApiQpsMonitorVO b = (ApiQpsMonitorVO)o;
        int aInt = Integer.parseInt(this.getQps());
        int bInt = Integer.parseInt(b.getQps());
        if(aInt == bInt){
            return this.getApiName().compareTo(b.getApiName());
        }else{
            return bInt - aInt;
        }
    }



    @Override
    public String toString() {
        return "ApiQpsMonitorVO{" +
                "apiName='" + apiName + '\'' +
                ", appName='" + appName + '\'' +
                ", contextRoot='" + contextRoot + '\'' +
                ", qps='" + qps + '\'' +
                ", totalCost='" + totalCost + '\'' +
                ", serverCostTimeSum='" + serverCostTimeSum + '\'' +
                ", tomcatTimeOutCount='" + tomcatTimeOutCount + '\'' +
                ", acquireConnectTimeSum='" + acquireConnectTimeSum + '\'' +
                ", queueWaitTimeSum='" + queueWatiTimeSum + '\'' +
                '}';
    }
}
