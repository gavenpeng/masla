package com.msw.masla.core.discovery.nacos;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.msw.masla.common.util.StringUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT_PORT;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMESPACE;
import static com.alibaba.nacos.api.PropertyKeyConst.PASSWORD;
import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;
import static com.alibaba.nacos.api.PropertyKeyConst.USERNAME;

/**
 * Author: Gavin.peng
 * Date: 2024/7/20
 * Description:
 */
@Data
@Component
public class MaslaNacosDiscoveryProperties implements InitializingBean {

    private static final Logger log = LoggerFactory
            .getLogger(MaslaNacosDiscoveryProperties.class);

    /**
     * Prefix of {@link MaslaNacosDiscoveryProperties}.
     */
    public static final String PREFIX = "masla.nacos.discovery";


    private static final Pattern PATTERN = Pattern.compile("-(\\w)");


    @NacosValue(value = "${masla.nacos.discovery.serverAddr:}", autoRefreshed = true )
    private String serverAddr;

    @NacosValue(value = "${masla.nacos.discovery.username:}", autoRefreshed = true )
    private String username;

    @NacosValue(value = "${masla.nacos.discovery.password:}", autoRefreshed = true )
    private String password;

    @NacosValue(value = "${masla.nacos.discovery.namespace:}", autoRefreshed = true )
    private String namespace;


    private String endpoint;


    private long watchDelay = 30000;


    private String clusterName = "DEFAULT";


    private String group = "DEFAULT_GROUP";


    public Properties getNacosProperties() {
        Properties properties = new Properties();
        properties.put(SERVER_ADDR, serverAddr);
        properties.put(USERNAME, Objects.toString(username, ""));
        properties.put(PASSWORD, Objects.toString(password, ""));
        properties.put(NAMESPACE, namespace);

        if (endpoint != null && endpoint.contains(":")) {
            int index = endpoint.indexOf(":");
            properties.put(ENDPOINT, endpoint.substring(0, index));
            properties.put(ENDPOINT_PORT, endpoint.substring(index + 1));
        }
        else if (endpoint != null) {
            properties.put(ENDPOINT, endpoint);
        }

        return properties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (StringUtil.isEmptyString(this.serverAddr)) {
            this.serverAddr = System.getProperty("nacos.server-addr");
        }

        if (StringUtil.isEmptyString(this.namespace)) {
            this.namespace = System.getProperty("nacos.namespace");
        }

        if (StringUtil.isEmptyString(this.password)) {
            this.password = System.getProperty("nacos.password");
        }

        if (StringUtil.isEmptyString(this.username)) {
            this.username = System.getProperty("nacos.username");
        }
    }
}
