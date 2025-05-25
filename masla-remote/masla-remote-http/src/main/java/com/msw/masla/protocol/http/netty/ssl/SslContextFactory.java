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
package com.msw.masla.protocol.http.netty.ssl;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by Gavin.peng on 2017/9/25.
 */
public interface SslContextFactory
{
    SslContextBuilder createBuilderForServer();
    String[] getProtocols();
    List<String> getCiphers() throws NoSuchAlgorithmException;
    void enableSessionTickets(SslContext sslContext);
    void configureOpenSslStatsMetrics(SslContext sslContext, String sslContextId);
}
