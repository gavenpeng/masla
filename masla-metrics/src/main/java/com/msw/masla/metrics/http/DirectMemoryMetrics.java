package com.msw.masla.metrics.http;

import com.msw.masla.common.monitor.vo.DirectMemoryMonitorVO;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.metrics.frame.AbstractMetrics;
import com.msw.masla.protocol.http.netty.config.NettyConfig;
import com.msw.masla.protocol.http.netty.factory.MaslaEventLoopGroupFactory;
import com.msw.masla.protocol.http.netty.util.BufferUtils;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class DirectMemoryMetrics extends AbstractMetrics {

    private static final long MEMORY_LOWER_WATER_SIZE = 1000 * 1000 * 3000l;

    private DirectMemoryMetrics() {
    }

    public static DirectMemoryMetrics getInstance() {
        return DirectMemoryMetricsHolder.instance;
    }

    private static class DirectMemoryMetricsHolder {
        private static DirectMemoryMetrics instance = new DirectMemoryMetrics();
    }

    @Override
    public List getMetrics() {
        return doGetMetrics();
    }

    private List doGetMetrics() {
        long timestamp = getTimestamp();
        DirectMemoryMonitorVO vo = new DirectMemoryMonitorVO();
        MaslaEventLoopGroupFactory loopGroupFactory = MaslaEventLoopGroupFactory.getInstance();
        NettyConfig nettyConfig = loopGroupFactory.getNettyConfig();
        Map<String, PooledByteBufAllocator> allocatorMap = loopGroupFactory.getAllocatorMap();
        try {
            PooledByteBufAllocatorMetric metric = PooledByteBufAllocator.DEFAULT.metric();
            if(metric != null) {
                log.warn("Masla netty client site memory detail:{}", metric.toString());
                long useMemory = metric.usedDirectMemory();
                long configMaxSize = nettyConfig.getDirectMemorySize();
                configMaxSize = configMaxSize * 1024 *1024 * 1024l;
                long appUsedMemory = useMemory;
                if(allocatorMap != null && allocatorMap.size() >0){
                    for(Map.Entry<String,PooledByteBufAllocator> entry:allocatorMap.entrySet()){
                        String appName = entry.getKey();
                        PooledByteBufAllocator pooledByteBufAllocator = entry.getValue();
                        appUsedMemory += pooledByteBufAllocator.metric().usedDirectMemory();
                        log.warn("masla app {} memory detail:{}", appName,pooledByteBufAllocator.metric().toString());
                    }
                }
                long serverUsedMemory = BufferUtils.SERVER_POOL_ALLOCATOR.metric().usedDirectMemory();
                long totalUsed = appUsedMemory + serverUsedMemory;
                vo.setHost(MaslaSpringContextUtil.getMaslaConfConfigBean().getLocalIp());
                vo.setTimestamp(timestamp);
                vo.setAppUsedMemory(appUsedMemory);
                vo.setServerUsedMemory(serverUsedMemory);
                vo.setTotalUsedMemory(totalUsed);
                return Arrays.asList(vo);
            }
            return Collections.emptyList();
        }catch (Throwable e){
            log.error("get direct memory metrics error, ", e);
        }
        return Collections.emptyList();
    }

}
