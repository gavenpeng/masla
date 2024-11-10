package com.msw.masla.metrics.http;


import com.msw.masla.common.pojo.ApiMetric;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.monitor.vo.TotalMetricMonitorVO;
import com.msw.masla.metrics.frame.AbstractMetrics;
import lombok.extern.slf4j.Slf4j;
import org.HdrHistogram.Histogram;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 收集每台网关上的所有数据
 */
@Slf4j
public class TotalMetrics extends AbstractMetrics {

    private TotalMetricMonitorVO vo;
    private int execNums = 0;

    private TotalMetrics() {
        this.vo = new TotalMetricMonitorVO();
    }

    private static class TotalMetricsHolder {
        private static TotalMetrics totalMetrics = new TotalMetrics();
    }

    public static TotalMetrics getInstance() {
        return TotalMetricsHolder.totalMetrics;
    }


    public void convert2TotalMetricMonitorVO(TotalMetricMonitorVO totalMetricMonitorVO, ApiMetric metric, ServiceApp appDO, String host, String serviceId,
                                             long circuitCount, long flowControllerCount) {
        // setTotalMetrics
        totalMetricMonitorVO.setQps(totalMetricMonitorVO.getQps() + metric.getQps().get());
        totalMetricMonitorVO.setPeakQps(totalMetricMonitorVO.getPeakQps() + metric.getPeakQps().get());
        totalMetricMonitorVO.setServerCost(totalMetricMonitorVO.getServerCost() + metric.getServerCost().floatValue());
        totalMetricMonitorVO.setAcquireCost(totalMetricMonitorVO.getAcquireCost() + metric.getAcquireCost().floatValue());
        totalMetricMonitorVO.setPushCost(totalMetricMonitorVO.getPushCost() + metric.getPushCost().floatValue());
        totalMetricMonitorVO.setSuccessNums(totalMetricMonitorVO.getSuccessNums() + metric.getSuccessNums().get());
        totalMetricMonitorVO.setTimeoutNums(totalMetricMonitorVO.getTimeoutNums() + metric.getTimeoutNums().get());
        totalMetricMonitorVO.setRejectNums(totalMetricMonitorVO.getRejectNums() + metric.getRejectNums().get());
        totalMetricMonitorVO.setSlowNums(totalMetricMonitorVO.getSlowNums() + metric.getSlowNums().get());
        totalMetricMonitorVO.setExceptionNums(totalMetricMonitorVO.getExceptionNums() + metric.getExceptionNums().get());
        totalMetricMonitorVO.setFiveXXCodeNums(totalMetricMonitorVO.getFiveXXCodeNums() + metric.getFiveXXCodeNums().get());
        totalMetricMonitorVO.setFourXXCodeNums(totalMetricMonitorVO.getFourXXCodeNums() + metric.getFourXXCodeNums().get());
        totalMetricMonitorVO.setCode400(totalMetricMonitorVO.getCode400() + metric.getCode400().get());
        totalMetricMonitorVO.setCode401(totalMetricMonitorVO.getCode401() + metric.getCode401().get());
        totalMetricMonitorVO.setCode404(totalMetricMonitorVO.getCode404() + metric.getCode404().get());
        totalMetricMonitorVO.setCodeAppDefine(totalMetricMonitorVO.getCodeAppDefine() + metric.getCodeAppDefine().get());
        totalMetricMonitorVO.setOutBandWidth(Math.max(totalMetricMonitorVO.getOutBandWidth(), metric.getOutBandWidth()));
        totalMetricMonitorVO.setInBandWidth(Math.max(totalMetricMonitorVO.getInBandWidth(), metric.getInBandWidth()));
        totalMetricMonitorVO.setConnClosedNums(totalMetricMonitorVO.getConnClosedNums() + metric.getConnClosedNums().get());
        totalMetricMonitorVO.setConnRefusedNums(totalMetricMonitorVO.getConnRefusedNums() + metric.getConnRefusedNums().get());
        totalMetricMonitorVO.setConnResetNums(totalMetricMonitorVO.getConnResetNums() + metric.getConnResetNums().get());
        totalMetricMonitorVO.setConnTimeoutNums(totalMetricMonitorVO.getConnTimeoutNums() + metric.getConnTimeoutNums().get());
        totalMetricMonitorVO.setConnPoolFullRejectNums(totalMetricMonitorVO.getConnPoolFullRejectNums() + metric.getConnPoolFullRejectNums().get());
        totalMetricMonitorVO.setConnPoolWaitTimeoutNums(totalMetricMonitorVO.getConnPoolWaitTimeoutNums() + metric.getConnPoolWaitTimeoutNums().get());
        totalMetricMonitorVO.setVarnishCacheMiss(totalMetricMonitorVO.getVarnishCacheMiss() + metric.getVarnishCacheMiss().get());
        totalMetricMonitorVO.setCircuitNums(totalMetricMonitorVO.getCircuitNums() + circuitCount);
        totalMetricMonitorVO.setFlowControllerNums(totalMetricMonitorVO.getFlowControllerNums() + flowControllerCount);

        ConcurrentHashMap<String, Histogram> hostTP90Map = appDO.getAppHostServiceIdTP90Map().get(host);
        if (hostTP90Map != null) {
            Histogram histogram = hostTP90Map.get(serviceId);
            if(histogram != null) {
                totalMetricMonitorVO.setTp50(totalMetricMonitorVO.getTp50() + histogram.getValueAtPercentile(50));
                totalMetricMonitorVO.setTp90(totalMetricMonitorVO.getTp90() + histogram.getValueAtPercentile(90));
                totalMetricMonitorVO.setTp99(totalMetricMonitorVO.getTp99() + histogram.getValueAtPercentile(99));
                totalMetricMonitorVO.setTp999(totalMetricMonitorVO.getTp999() + histogram.getValueAtPercentile(99.9));
                totalMetricMonitorVO.setTp9999(totalMetricMonitorVO.getTp9999() + histogram.getValueAtPercentile(99.99));
                totalMetricMonitorVO.setMax(totalMetricMonitorVO.getMax() + histogram.getMaxValue());
                totalMetricMonitorVO.setMin(totalMetricMonitorVO.getMin() + histogram.getMinValue());
            }
        }
//        ServiceIdCache<String, Histogram> hostTP90Map = appDO.getAppHostServiceIdTP90Map().get(host);
//        if (hostTP90Map != null) {
//            try {
//                Histogram histogram = hostTP90Map.get(serviceId);
//                if (histogram != null) {
//                    totalMetricMonitorVO.setTp50(totalMetricMonitorVO.getTp50() + histogram.getValueAtPercentile(50));
//                    totalMetricMonitorVO.setTp90(totalMetricMonitorVO.getTp90() + histogram.getValueAtPercentile(90));
//                    totalMetricMonitorVO.setTp99(totalMetricMonitorVO.getTp99() + histogram.getValueAtPercentile(99));
//                    totalMetricMonitorVO.setTp999(totalMetricMonitorVO.getTp999() + histogram.getValueAtPercentile(99.9));
//                    totalMetricMonitorVO.setTp9999(totalMetricMonitorVO.getTp9999() + histogram.getValueAtPercentile(99.99));
//                    totalMetricMonitorVO.setMax(totalMetricMonitorVO.getMax() + histogram.getMaxValue());
//                    totalMetricMonitorVO.setMin(totalMetricMonitorVO.getMin() + histogram.getMinValue());
//                }
//            } catch (Exception ignored) {
//            }
//        }
    }

    @Override
    public List<TotalMetricMonitorVO> getMetrics() {
        return Arrays.asList(this.vo);
    }

    public TotalMetricMonitorVO getVo() {
        return vo;
    }

    public void setVo(TotalMetricMonitorVO vo) {
        this.vo = vo;
    }

}
