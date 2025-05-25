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
package com.msw.masla.protocol.http.netty.config;


import com.msw.masla.protocol.http.netty.ssl.ServerSslConfig;
import com.msw.masla.protocol.http.netty.ssl.SslContextFactory;

/**
 * User: michaels@netflix.com
 * Date: 2/8/17
 * Time: 6:21 PM
 */
public class CommonChannelConfigKeys
{
    public static final ChannelConfigKey<Boolean> withProxyProtocol = new ChannelConfigKey<Boolean>("withProxyProtocol", false);
    public static final ChannelConfigKey<Boolean> preferProxyProtocolForClientIp = new ChannelConfigKey<Boolean>("preferProxyProtocolForClientIp", true);

    public static final ChannelConfigKey<Integer> idleTimeout = new ChannelConfigKey<Integer>("idleTimeout");
    public static final ChannelConfigKey<Integer> httpRequestReadTimeout = new ChannelConfigKey<Integer>("httpRequestReadTimeout");
    public static final ChannelConfigKey<Integer> maxConnections = new ChannelConfigKey<Integer>("maxConnections");
    public static final ChannelConfigKey<Integer> maxRequestsPerConnection = new ChannelConfigKey<Integer>("maxRequestsPerConnection", 4000);
    public static final ChannelConfigKey<Integer> maxRequestsPerConnectionInBrownout = new ChannelConfigKey<Integer>("maxRequestsPerConnectionInBrownout", 100);
    public static final ChannelConfigKey<Integer> connectionExpiry = new ChannelConfigKey<Integer>("connectionExpiry", 20 * 60 * 1000);

    // SSL:
    public static final ChannelConfigKey<Boolean> isSSlFromIntermediary = new ChannelConfigKey<Boolean>("isSSlFromIntermediary", false);
    public static final ChannelConfigKey<ServerSslConfig> serverSslConfig = new ChannelConfigKey<ServerSslConfig>("serverSslConfig");
    public static final ChannelConfigKey<SslContextFactory> sslContextFactory = new ChannelConfigKey<SslContextFactory>("sslContextFactory");

    // HTTP/2 specific:
    public static final ChannelConfigKey<Integer> maxConcurrentStreams = new ChannelConfigKey<Integer>("maxConcurrentStreams", 100);
    public static final ChannelConfigKey<Integer> initialWindowSize = new ChannelConfigKey<Integer>("initialWindowSize", 5242880);  // 5MB
    public static final ChannelConfigKey<Integer> connCloseDelay = new ChannelConfigKey<Integer>("connCloseDelay");
    public static final ChannelConfigKey<Integer> maxHttp2HeaderTableSize = new ChannelConfigKey<Integer>("maxHttp2HeaderTableSize", 4096);
    public static final ChannelConfigKey<Integer> maxHttp2HeaderListSize = new ChannelConfigKey<Integer>("maxHttp2HeaderListSize");
}
