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
