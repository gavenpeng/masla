package com.msw.masla.common.config;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableNacosConfig(globalProperties = @NacosProperties(serverAddr = "${nacos.server-addr}", username = "${nacos.username}", password = "${nacos.password:nacos}", namespace = "${nacos.namespace:public}"))
@NacosPropertySource(dataId = "masla.properties", autoRefreshed = true)
@Slf4j
public class NacosConfiguration {

    public NacosConfiguration() {
        log.info("Masla init masla.properties from nacos remote config");
    }

}