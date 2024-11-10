package com.msw.masla.core.router.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Properties;

@Component
public class NacosRouteConfig implements InitializingBean {

    @NacosValue("${nacos.server-addr:localhost:8848}")
    private final String SERVER_ADDR = "localhost:8848"; // Nacos服务器地址
    private final String DATA_ID = "route.properties";   // 路由规则文件
    private static final String GROUP = "DEFAULT_GROUP";         // 配置分组

    private Properties routeProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public void init() throws Exception {
        ConfigService configService = NacosFactory.createConfigService(SERVER_ADDR);
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
