package com.msw.masla.core.router;

import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import org.springframework.stereotype.Component;

@Component
public class RouteRuleConfigListener {

    @NacosConfigListener(dataId = "route.properties", groupId = "DEFAULT_GROUP")
    public void onConfigChange(String newContent) {
        // 这里可以对新的配置进行处理
        System.out.println("配置变更：" + newContent);
    }
}
