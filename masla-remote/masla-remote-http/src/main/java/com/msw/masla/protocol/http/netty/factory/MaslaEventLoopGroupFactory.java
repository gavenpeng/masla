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
package com.msw.masla.protocol.http.netty.factory;

import com.msw.masla.protocol.http.netty.config.NettyConfig;
import com.msw.masla.protocol.http.netty.util.SystemUtil;
import io.netty.buffer.*;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gavin.peng on 2017/7/14.
 */
public class MaslaEventLoopGroupFactory implements EventLoopFactory {

    private static final Logger LOG = LoggerFactory.getLogger(MaslaEventLoopGroupFactory.class);
    //private PooledByteBufAllocator DEFAULT_ALLOCATOR = PooledByteBufAllocator.DEFAULT;
    private EventLoopGroup[] defaultLoopGroupArray = null;
    private EventLoopGroup fastLoopGroup = null;
    private EventLoopGroup slowLoopGroup = null;
    private Map<String,EventLoopGroup> eventLoopGroupMap = null;
    private Map<String,PooledByteBufAllocator> allocatorMap = null;
    private Class<? extends io.netty.channel.socket.SocketChannel> SocketChannel;
    private static final int MASLA_DEFAULT_IO_GROUP = 2;//IO线程组
    private static final int MASLA_DEFAULT_IO_THREAD = 0;
    private static final int MASLA_DEFAULT_IO_RATE = 100;
    private int routeIndex;
    private NettyConfig nettyConfig;
    private boolean doInit;


    private MaslaEventLoopGroupFactory(){

    }

    private static class MaslaEventLoopGroupFactoryHold{
        static MaslaEventLoopGroupFactory instance = new MaslaEventLoopGroupFactory();
    }

    public static MaslaEventLoopGroupFactory getInstance(){
        return MaslaEventLoopGroupFactoryHold.instance;
    }

    public void initEventLoopGroup(final NettyConfig nettyConfig){

        if(this.doInit){
            return;
        }
        this.doInit = true;
        this.eventLoopGroupMap = new ConcurrentHashMap<String, EventLoopGroup>(5);
        this.allocatorMap = new ConcurrentHashMap<String,PooledByteBufAllocator>(5);
        this.nettyConfig = nettyConfig;

        //disable thread local cache
        long maxDirectMemory = this.nettyConfig.getDirectMemorySize()*1024*1024*1024l;
        /*
        io.netty.maxDirectMemory 默认是-1，即用jvm maxDirectMemory参数指定的大小，默认是堆大小的2倍,
        如果设置为0，那netty会用jdk的DirectByteBuffer来创建堆外内存，并且通过jdk的clear来回收。-1和大于0都
        是netty 通过unsafe来创建堆外内存，也没有clear这种来回收，都是netty自己管理，对于内存池就是这样。
         */
        System.setProperty("io.netty.maxDirectMemory", String.valueOf(maxDirectMemory));

        System.setProperty("io.netty.recycler.maxCapacity","0");
        System.setProperty("io.netty.allocator.tinyCacheSize","0");
        System.setProperty("io.netty.allocator.smallCacheSize","0");
        System.setProperty("io.netty.allocator.normalCacheSize","0");

        System.setProperty("io.netty.allocator.numDirectArenas", String.valueOf(nettyConfig.getNumDirectArenas()));
        System.setProperty("io.netty.allocator.numHeapArenas", String.valueOf(nettyConfig.getNumHeapArenas()));
        System.setProperty("io.netty.allocator.pageSize", String.valueOf(nettyConfig.getPageSize()));
        //io task queue 改小,这里设置大点，防止瞬间突发的请求，链接池获取释放的任务也是用这个队列
        //线程池和应用的机器绑定，如果应用只有1台，或者2台，只能用一个或者2个线程的任务队列。
        //就现在了单个应用在建链这里的瓶颈
        System.setProperty("io.netty.eventLoop.maxPendingTasks", "20000");
        System.setProperty("io.netty.eventexecutor.maxPendingTasks", "20000");
        //DISABLED -->SIMPLE -->ADVANCED -->PARANOID
        System.setProperty("io.netty.leakDetectionLevel", "DISABLED");

        try {
            if (SystemUtil.canUseNative() ) {
                defaultLoopGroupArray = new EpollEventLoopGroup[MASLA_DEFAULT_IO_GROUP];
                for(int index=1;index<=MASLA_DEFAULT_IO_GROUP;index++){
                    defaultLoopGroupArray[index-1] = new EpollEventLoopGroup(nettyConfig.getDefalutEventLoopThreadCount(),
                            new DefaultThreadFactory("EpollEventLoop" + "-" + "Group"+"-"+index));
                    ((EpollEventLoopGroup)defaultLoopGroupArray[index-1]).setIoRatio(MASLA_DEFAULT_IO_RATE);
                }
                slowLoopGroup = new EpollEventLoopGroup(nettyConfig.getSlowEventLoopThreadCount(),
                        new DefaultThreadFactory("SlowEpollEventLoop"));
                SocketChannel = EpollSocketChannel.class;
                ((EpollEventLoopGroup)slowLoopGroup).setIoRatio(MASLA_DEFAULT_IO_RATE);
            } else {
                defaultLoopGroupArray = new NioEventLoopGroup[MASLA_DEFAULT_IO_GROUP];
                for(int index=1;index<=MASLA_DEFAULT_IO_GROUP;index++){
                    defaultLoopGroupArray[index-1] = new NioEventLoopGroup(nettyConfig.getDefalutEventLoopThreadCount(),
                            new DefaultThreadFactory("NioEventLoop" + "-" + "Group"+"-"+index));
                    ((NioEventLoopGroup)defaultLoopGroupArray[index-1]).setIoRatio(MASLA_DEFAULT_IO_RATE);
                }

                slowLoopGroup = new NioEventLoopGroup(nettyConfig.getSlowEventLoopThreadCount(),
                        new DefaultThreadFactory("SlowNioEventLoop"));
                ((NioEventLoopGroup)slowLoopGroup).setIoRatio(MASLA_DEFAULT_IO_RATE);
                SocketChannel = NioSocketChannel.class;

            }

        } catch (AbstractMethodError e) {
            LOG.error("Masla init netty group error:{}",e);
        }
    }

    @Override
    public EventLoopGroup getClientEventLoopGroup(int eventLoopGroupType,String appName) {
        //访问这里肯定是单线程
        if(EventLoopGroupType.DEFAULT.getCode() == eventLoopGroupType){
            if(routeIndex >= 2){
                routeIndex = 0;
            }
            return defaultLoopGroupArray[routeIndex++];

        }else if(EventLoopGroupType.SLOW.getCode()== eventLoopGroupType){
            return slowLoopGroup;
        }else if(EventLoopGroupType.NEW.getCode() == eventLoopGroupType){
            return createEventLoopGroup(appName);
        }
        return defaultLoopGroupArray[0];
    }

    @Override
    public Map<String,EventLoopGroup> getAllExcludeEventLoopGroup() {
        return this.eventLoopGroupMap;
    }

    @Override
    public Class<? extends io.netty.channel.socket.SocketChannel> getSocketChannel() {
        return this.SocketChannel;
    }




    /**
     * 一个APP绑定一个独立的线程池，线程个数为默认个数 = cpu 核数*2
     * @param appName
     * @return
     */
    private EventLoopGroup createEventLoopGroup(String appName){

        EventLoopGroup newLoopGroup = this.eventLoopGroupMap.get(appName);
        if(newLoopGroup != null){
            return newLoopGroup;
        }
        synchronized (this.eventLoopGroupMap) {
            if((newLoopGroup = this.eventLoopGroupMap.get(appName)) != null){
                return newLoopGroup;
            }

            if (SystemUtil.canUseNative()) {
                newLoopGroup = new EpollEventLoopGroup(MASLA_DEFAULT_IO_THREAD,
                        new DefaultThreadFactory("EpollEventLoop" + "-" + appName));
                ((EpollEventLoopGroup) newLoopGroup).setIoRatio(MASLA_DEFAULT_IO_RATE);

            } else {
                newLoopGroup = new NioEventLoopGroup(MASLA_DEFAULT_IO_THREAD,
                        new DefaultThreadFactory("NioEventLoop" + "-" + appName));
                ((NioEventLoopGroup) newLoopGroup).setIoRatio(MASLA_DEFAULT_IO_RATE);
            }

            if (newLoopGroup != null) {
                this.eventLoopGroupMap.put(appName, newLoopGroup);
                return newLoopGroup;
            }
        }
        return this.slowLoopGroup;

    }

    @Override
    public PooledByteBufAllocator getBufAllocator(int eventLoopGroupType, String appName) {

        if(EventLoopGroupType.NEW.getCode() == eventLoopGroupType){
            PooledByteBufAllocator pooledByteBufAllocator = allocatorMap.get(appName);
            if(pooledByteBufAllocator != null){
                return pooledByteBufAllocator;
            }
            pooledByteBufAllocator = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());
            allocatorMap.put(appName,pooledByteBufAllocator);
            return pooledByteBufAllocator;
        }else{
            return PooledByteBufAllocator.DEFAULT;
        }
    }

    @Override
    public NettyConfig getNettyConfig() {
        return this.nettyConfig;
    }


    @Override
    public List<PooledByteBufAllocator> getUsedBufAllocatorList() {
        if(this.allocatorMap == null || this.allocatorMap.size() <=0){
            return null;
        }
        List<PooledByteBufAllocator> pooledByteBufAllocators = new ArrayList<PooledByteBufAllocator>(this.allocatorMap.size());
        for(Map.Entry<String,PooledByteBufAllocator> entry:this.allocatorMap.entrySet()){
            pooledByteBufAllocators.add(entry.getValue());
        }
        return pooledByteBufAllocators;
    }


    @Override
    public PooledByteBufAllocator getUsedBufAllocator(String appName) {
        return this.allocatorMap.get(appName);
    }

    @Override
    public void destroy() {

        for(int i=0;i<MASLA_DEFAULT_IO_GROUP;i++) {
            defaultLoopGroupArray[i].shutdownGracefully();
        }

        if (slowLoopGroup != null) {
            LOG.info("Masla start shutdown event loop group");
            slowLoopGroup.shutdownGracefully();
        }

        if(this.eventLoopGroupMap.size()>0){
            for(Map.Entry<String,EventLoopGroup> entry: this.eventLoopGroupMap.entrySet()){
                LOG.info("Masla start shutdown {} event loop group",entry.getKey());
                entry.getValue().shutdownGracefully();
            }
        }

        if(this.allocatorMap != null){
            this.allocatorMap.clear();
        }


    }

    public Map<String, PooledByteBufAllocator> getAllocatorMap() {
        return allocatorMap;
    }

    public void setAllocatorMap(Map<String, PooledByteBufAllocator> allocatorMap) {
        this.allocatorMap = allocatorMap;
    }
}
