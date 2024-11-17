package com.msw.masla.core.router.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.msw.masla.protocol.http.netty.exception.MaslaException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Properties;

public class NacosRouteConfig {

    private final String DATA_ID = "route.properties";

    private static final String GROUP = "DEFAULT_GROUP";

    private String serverAddr;

    private String username;

    private String password;

    private String namespace;

    private Properties routeProperties;

    private ConfigService configService;

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
            throw new MaslaException("Masla gateway not found nacos server namespace params!!!");
        }

        Properties nacosProperties = new Properties();
        nacosProperties.put("serverAddr", serverAddr);
        nacosProperties.put("username", username);
        nacosProperties.put("password", password);
        nacosProperties.put("namespace", namespace);

        // 创建 ConfigService 实例并传入 nacosProperties
        configService = NacosFactory.createConfigService(nacosProperties);

        String config = configService.getConfig(DATA_ID, GROUP, 5000); // 从Nacos读取route.properties内容
        if (config != null) {
            routeProperties = new Properties();
            routeProperties.load(new ByteArrayInputStream(config.getBytes()));
        }
    }

    public Properties getRouteProperties() {
        return routeProperties;
    }
}
