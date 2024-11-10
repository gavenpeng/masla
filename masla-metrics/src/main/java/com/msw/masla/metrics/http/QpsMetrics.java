package com.msw.masla.metrics.http;

import com.msw.masla.common.pojo.ApiMetric;
import com.msw.masla.common.pojo.ServiceApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gavin.peng on 2018/5/4.
 */
public class QpsMetrics {

    protected static final Logger LOG = LoggerFactory.getLogger(QpsMetrics.class);

    private static final Map<String,String> appHostMap = new ConcurrentHashMap<String, String>();


    static class MetricStaticsHolder{
        static QpsMetrics instance = new QpsMetrics();
    }

    public static QpsMetrics getInstance(){
        return MetricStaticsHolder.instance;
    }


    public static void recordFaildMertic(String serviceIdentify, ServiceApp appDO, String host){

        ApiMetric metric = appDO.getApiMetricByServiceIdAndHost(serviceIdentify, host);
        if(metric != null) {
            metric.getExceptionNums().incrementAndGet();
        }

    }

    public static void recordQpsMertic(String serviceIdentify, ServiceApp appDO, String host, boolean isStressReq){

        ApiMetric metric = appDO.getApiMetricByServiceIdAndHost(serviceIdentify, host);
        if(metric != null) {
            metric.getQps().incrementAndGet();
            if(isStressReq){
                metric.getPeakQps().incrementAndGet();
            }
        }

    }

    public static void recordRejectMertic(String serviceIdentify, ServiceApp appDO, String host){

        ApiMetric metric = appDO.getApiMetricByServiceIdAndHost(serviceIdentify, host);
        if(metric != null) {
            metric.getRejectNums().incrementAndGet();
        }

    }


    public static void recordInqueueTimeMertic(String serviceIdentify, ServiceApp appDO, String host){

        ApiMetric metric = appDO.getApiMetricByServiceIdAndHost(serviceIdentify,
            host);
        metric.getWaitCost().incrementAndGet();

    }


    public static void recordTimeoutMertic(String serviceIdentify, ServiceApp appDO, String host){
        ApiMetric metric = appDO.getApiMetricByServiceIdAndHost(serviceIdentify, host);
        metric.getTimeoutNums().incrementAndGet();
    }

    public static void recordSuccessMertic(String serviceIdentify, ServiceApp appDO, String host){
        ApiMetric metric = appDO.getApiMetricByServiceIdAndHost(serviceIdentify, host);
        metric.getSuccessNums().incrementAndGet();
    }

    public static void recordFourXXMertic(String serviceIdentify, ServiceApp appDO, String host){
        ApiMetric metric = appDO.getApiMetricByServiceIdAndHost(serviceIdentify, host);
        metric.getFourXXCodeNums().incrementAndGet();
    }

    public static void recordFiveXXMertic(String serviceIdentify, ServiceApp appDO, String host){
        ApiMetric metric = appDO.getApiMetricByServiceIdAndHost(serviceIdentify, host);

        metric.getFiveXXCodeNums().incrementAndGet();
    }

}
