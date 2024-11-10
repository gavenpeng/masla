package com.msw.masla.core.monitor;

import com.msw.masla.common.monitor.vo.NettyPoolArenaMonitorVO;
import io.netty.buffer.PoolArenaMetric;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gavin.peng on 17/7/18.
 */
@Component
public class NettyPoolArenaMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(NettyPoolArenaMonitor.class);

    public List<NettyPoolArenaMonitorVO> getPoolArenaInfoVOList() {
        PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
        PooledByteBufAllocatorMetric metric = allocator.metric();
        List<NettyPoolArenaMonitorVO> nettyPoolArenaMonitorVOList = new ArrayList<NettyPoolArenaMonitorVO>();
        int idx = 0;
        for (PoolArenaMetric poolArenaMetric : metric.directArenas()) {
            NettyPoolArenaMonitorVO nettyPoolArenaMonitorVO = new NettyPoolArenaMonitorVO();
            nettyPoolArenaMonitorVO.setName("Direct[" + idx++ + "]");
            buileVO(poolArenaMetric,nettyPoolArenaMonitorVO);
            nettyPoolArenaMonitorVOList.add(nettyPoolArenaMonitorVO);
        }
        for (PoolArenaMetric poolArenaMetric : metric.heapArenas()) {
            NettyPoolArenaMonitorVO nettyPoolArenaMonitorVO = new NettyPoolArenaMonitorVO();
            nettyPoolArenaMonitorVO.setName("Heap[" + idx++ + "]");
            buileVO(poolArenaMetric,nettyPoolArenaMonitorVO);
            nettyPoolArenaMonitorVOList.add(nettyPoolArenaMonitorVO);
        }
        return nettyPoolArenaMonitorVOList;
    }

    private void buileVO(PoolArenaMetric poolArenaMetric,NettyPoolArenaMonitorVO nettyPoolArenaMonitorVO) {
        // numActiveXXXAllocations
        nettyPoolArenaMonitorVO.setNumActiveAllocations(String.valueOf(poolArenaMetric.numActiveAllocations()));
        nettyPoolArenaMonitorVO.setNumActiveTinyAllocations(String.valueOf(poolArenaMetric.numActiveTinyAllocations()));
        nettyPoolArenaMonitorVO.setNumActiveSmallAllocations(String.valueOf(poolArenaMetric.numActiveSmallAllocations()));
        nettyPoolArenaMonitorVO.setNumActiveNormalAllocations(String.valueOf(poolArenaMetric.numActiveNormalAllocations()));
        nettyPoolArenaMonitorVO.setNumActiveHugeAllocations(String.valueOf(poolArenaMetric.numActiveHugeAllocations()));

        // numXXXAllocations
        nettyPoolArenaMonitorVO.setNumAllocations(String.valueOf(poolArenaMetric.numAllocations()));
        nettyPoolArenaMonitorVO.setNumTinyAllocations(String.valueOf(poolArenaMetric.numTinyAllocations()));
        nettyPoolArenaMonitorVO.setNumSmallAllocations(String.valueOf(poolArenaMetric.numSmallAllocations()));
        nettyPoolArenaMonitorVO.setNumNormalAllocations(String.valueOf(poolArenaMetric.numNormalAllocations()));
        nettyPoolArenaMonitorVO.setNumHugeAllocations(String.valueOf(poolArenaMetric.numHugeAllocations()));

        // numXXXDeallocations
        nettyPoolArenaMonitorVO.setNumDeallocations(String.valueOf(poolArenaMetric.numDeallocations()));
        nettyPoolArenaMonitorVO.setNumTinyDeallocations(String.valueOf(poolArenaMetric.numTinyDeallocations()));
        nettyPoolArenaMonitorVO.setNumSmallDeallocations(String.valueOf(poolArenaMetric.numSmallDeallocations()));
        nettyPoolArenaMonitorVO.setNumNormalDeallocations(String.valueOf(poolArenaMetric.numNormalDeallocations()));
        nettyPoolArenaMonitorVO.setNumHugeDeallocations(String.valueOf(poolArenaMetric.numHugeDeallocations()));

        return;

    }

}
