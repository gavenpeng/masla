package com.msw.masla.common.util;

import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.config.MaslaServerConfig;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by Gavin.peng on 2017/6/6.
 */
@Component
@Data
public class MaslaSpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 实现ApplicationContextAware接口的回调方法，设置上下文环境
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        MaslaSpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取对象
     * <a href="http://my.oschina.net/u/556800" class="referer" target="_blank">@return</a>  Object 一个以所给名字注册的bean的实例 (service注解方式，自动生成以首字母小写的类名为bean name)
     */
    public static Object getBean(String name) throws BeansException {
        if(applicationContext != null) {
            return applicationContext.getBean(name);
        }
        return null;
    }

    public static MaslaConfConfig getMaslaConfConfigBean() {
        if(applicationContext != null) {
            return (MaslaConfConfig)applicationContext.getBean("maslaConfConfig");
        }
        return null;
    }

    public static MaslaServerConfig getMaslaServerConfigBean() {
        if(applicationContext != null) {
            return (MaslaServerConfig)applicationContext.getBean("maslaServerConfig");
        }
        return null;
    }
}
