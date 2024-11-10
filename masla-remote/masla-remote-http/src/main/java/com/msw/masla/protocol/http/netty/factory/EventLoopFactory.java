package com.msw.masla.protocol.http.netty.factory;

import com.msw.masla.protocol.http.netty.config.NettyConfig;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;

import java.util.List;
import java.util.Map;

/**
 * Created by Gavin.peng on 2017/7/14.
 * EventLoop 类型的获取，有Default，fast，slow 三种类型的LoopGroup
 */
public interface EventLoopFactory {

    EventLoopGroup getClientEventLoopGroup(int eventLoopGroupType,String appName);

    PooledByteBufAllocator getBufAllocator(int eventLoopGroupType, String appName);

    List<PooledByteBufAllocator> getUsedBufAllocatorList();

    PooledByteBufAllocator getUsedBufAllocator(String appName);



    Class<? extends SocketChannel> getSocketChannel();

    Map<String,EventLoopGroup> getAllExcludeEventLoopGroup();

    NettyConfig getNettyConfig();

    void destroy();

}
