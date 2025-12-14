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
package com.msw.masla.core.router.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.config.listener.Listener;
import com.msw.masla.core.router.DefaultRouteRuleFactory;
import com.msw.masla.core.router.rule.RouteRuleCache;
import com.msw.masla.protocol.http.netty.exception.MaslaException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import java.util.concurrent.Executor;

@Slf4j
@Data
public class NacosRouteConfig {

    private final String DATA_ID = "route.properties";

    private static final String GROUP = "DEFAULT_GROUP";

    private String serverAddr;

    private String username;

    private String password;

    private String namespace;

    private Properties routeProperties;

    private ConfigService configService;

    private final DefaultRouteRuleFactory defaultRouteRuleFactory;


    public NacosRouteConfig(DefaultRouteRuleFactory defaultRouteRuleFactory) {
        this.defaultRouteRuleFactory = defaultRouteRuleFactory;
    }

    public void init() throws Exception {

        this.serverAddr = System.getProperty("nacos.server-addr", "localhost:8848");
        this.username = System.getProperty("nacos.username", "nacos");
        this.password = System.getProperty("nacos.password", "nacos");
        this.namespace = System.getProperty("nacos.namespace", "public");
        if (serverAddr == null || serverAddr.isEmpty()) {
            throw new MaslaException("Masla gateway not found nacos server addr params!!!");
        }
        if (username == null || username.isEmpty()) {
            throw new MaslaException("Masla gateway not found nacos server username params!!!");
        }
        if (password == null || password.isEmpty()) {
            throw new MaslaException("Masla gateway not found nacos server password params!!!");
        }

        if (namespace == null || namespace.isEmpty()) {
           log.info("namespace is empty, so use the default namespace public");
        }

        Properties nacosProperties = new Properties();
        nacosProperties.put("serverAddr", serverAddr);
        nacosProperties.put("username", username);
        nacosProperties.put("password", password);
        nacosProperties.put("namespace", namespace);

        // 创建 ConfigService 实例并传入 nacosProperties
        configService = NacosFactory.createConfigService(nacosProperties);
        String config = configService.getConfig(DATA_ID, GROUP, 5000);
        if (config != null) {
            routeProperties = new Properties();
            routeProperties.load(new ByteArrayInputStream(config.getBytes()));
        }


        configService.addListener(DATA_ID, GROUP, new Listener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                try {
                    log.info("Masla receive data id {} group {} config file update", DATA_ID, GROUP);
                    routeProperties.load(new ByteArrayInputStream(configInfo.getBytes()));
                    defaultRouteRuleFactory.loadRouteRule();
                } catch (Throwable e) {
                    log.error("Masla flush {} nacos config failed:", DATA_ID, e);
                }

            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        });
    }


}
