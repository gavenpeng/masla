package com.msw.masla.common.config;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableNacosConfig(globalProperties = @NacosProperties(serverAddr = "192.168.1.12:8858", username = "nacos", password = "nacos", namespace = "public"))
@NacosPropertySource(dataId = "masla.properties", autoRefreshed = true)
public class NacosConfiguration {

}