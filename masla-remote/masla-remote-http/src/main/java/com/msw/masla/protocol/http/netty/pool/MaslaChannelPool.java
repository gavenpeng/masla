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
package com.msw.masla.protocol.http.netty.pool;

import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.protocol.http.netty.http.connection.ChannelState;
import com.msw.masla.protocol.http.netty.http.connection.MaslaClientChannelPoolHandler;
import com.msw.masla.protocol.http.netty.factory.MaslaEventLoopGroupFactory;
import com.msw.masla.protocol.http.netty.http.HostInstance;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;

/**
 * Created by Gavin.peng on 2017/5/23.
 */
public class MaslaChannelPool extends FixedChannelPool {


    //连接池对应的host
    private String host;

    private int port;

    private HostInstance HostInstance;

    private ChannelState channelState = ChannelState.EXCLUSIVE;

    private int multiplexLimit = -10;

    private static final int MAX_MULTIPLEX_LIMIT = 1000;


//    private Map<Integer,ChannelReq>

    public MaslaChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler, int maxConnections, String host, int port) {

        super(bootstrap, handler, maxConnections, Integer.MAX_VALUE);
        this.host = host;
        this.port = port;
    }


    public MaslaChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler, int maxConnections, int maxPendingAcquires, String host, int port) {
        super(bootstrap, handler, ChannelHealthChecker.ACTIVE, null, -1, maxConnections, maxPendingAcquires);
        this.host = host;
        this.port = port;
    }


    public MaslaChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler,
                            ChannelHealthChecker healthCheck, AcquireTimeoutAction action,
                            final long acquireTimeoutMillis,
                            int maxConnections, int maxPendingAcquires, String host, int port) {
        super(bootstrap, handler, healthCheck, action, acquireTimeoutMillis, maxConnections, maxPendingAcquires, true);
        this.host = host;
        this.port = port;
    }


    public MaslaChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler,
                            ChannelHealthChecker healthCheck, AcquireTimeoutAction action,
                            final long acquireTimeoutMillis,
                            int maxConnections, int maxPendingAcquires, final boolean releaseHealthCheck, String host, int port) {
        super(bootstrap, handler, healthCheck, action, acquireTimeoutMillis, maxConnections, maxPendingAcquires, releaseHealthCheck);
        ((MaslaClientChannelPoolHandler)handler).setChannelPool(this);
        this.host = host;
        this.port = port;
    }

    public MaslaChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler,
                            ChannelHealthChecker healthCheck, AcquireTimeoutAction action,
                            final long acquireTimeoutMillis,
                            int maxConnections, int maxPendingAcquires, final boolean releaseHealthCheck, HostInstance HostInstance) {
        super(bootstrap, handler, healthCheck, action, acquireTimeoutMillis, maxConnections, maxPendingAcquires, releaseHealthCheck);
        ((MaslaClientChannelPoolHandler)handler).setChannelPool(this);
       this.HostInstance = HostInstance;
    }

    public MaslaChannelPool(EventLoopGroup group, Bootstrap bootstrap,
                            ChannelPoolHandler handler,
                            ChannelHealthChecker healthCheck, AcquireTimeoutAction action,
                            final long acquireTimeoutMillis,
                            int maxConnections, int maxPendingAcquires, final boolean releaseHealthCheck, HostInstance HostInstance) {
        super(group,bootstrap, handler, healthCheck, action, acquireTimeoutMillis, maxConnections, maxPendingAcquires, releaseHealthCheck);
        ((MaslaClientChannelPoolHandler)handler).setChannelPool(this);
        this.HostInstance = HostInstance;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public HostInstance getHostInstance() {
        return HostInstance;
    }

    public void setHostInstance(HostInstance HostInstance) {
        this.HostInstance = HostInstance;
    }

    public ChannelState getChannelState(){
        return this.channelState;
    }


    public void setChannelState(ChannelState channelState) {
        if(this.channelState != channelState) {
            this.channelState = channelState;
        }
    }


    public boolean isUpgrade(){
        return this.channelState == ChannelState.UPGRADE;
    }

    public int getMultiplexLimit() {
        return multiplexLimit;
    }


}
