/*
 * Copyright 2025 msw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.msw.masla.metrics.http;

import com.msw.masla.protocol.http.netty.config.NettyConfig;
import com.msw.masla.protocol.http.netty.factory.MaslaEventLoopGroupFactory;
import com.msw.masla.protocol.http.netty.util.BufferUtils;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocatorMetric;

import java.util.List;

/**
 * Created by Gavin.peng on 2017/12/29.
 */
public class DefaultMaslaMetric implements MaslaMetric {


    private static class DefaultMaslaMetricHold{
        static DefaultMaslaMetric instance = new DefaultMaslaMetric();
    }

    public static MaslaMetric getInstance(){
        return DefaultMaslaMetricHold.instance;
    }


    @Override
    public long getAvailablePoolChunks() {
        try {
            PooledByteBufAllocatorMetric serverMetric = BufferUtils.SERVER_POOL_ALLOCATOR.metric();
            PooledByteBufAllocatorMetric clientMetric = PooledByteBufAllocator.DEFAULT.metric();
            long clientUseMemory = clientMetric.usedDirectMemory();
            long serverUseMemory = serverMetric.usedDirectMemory();
            long appUseMemory = 0;
            List<PooledByteBufAllocator> appAllocatorList = MaslaEventLoopGroupFactory.getInstance().getUsedBufAllocatorList();
            if(appAllocatorList != null && appAllocatorList.size() >0){
                for(PooledByteBufAllocator allocator:appAllocatorList){
                    appUseMemory += allocator.metric().usedDirectMemory();
                }
            }
            long chunkSize = clientMetric.chunkSize();
            //long chunkCount = useMemory/ chunkSize;
            NettyConfig nettyConfig = MaslaEventLoopGroupFactory.getInstance().getNettyConfig();
            long totalDirectMemory = nettyConfig.getDirectMemorySize()*1024*1024*1024l;
            long freeUseMemory = totalDirectMemory - (serverUseMemory + clientUseMemory + appUseMemory);
            long availableChunkCount = freeUseMemory/chunkSize;
            return availableChunkCount;
        }catch (Throwable e){

        }
        return 0;
    }
}
