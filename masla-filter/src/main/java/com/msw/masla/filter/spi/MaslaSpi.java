package com.msw.masla.filter.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: Gavin.peng
 * Date: 2024/4/14
 * Description:
 * Annotation for Provider class of SPI.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface MaslaSpi {

    /**
     * Alias name of Provider class
     */
    String value() default "";

    /**
     * Whether create singleton instance
     */
    boolean isSingleton() default true;

    /**
     * Whether is the default Provider
     */
    boolean isDefault() default false;

    /**
     * Order priority of Provider class
     */
    int order() default 0;

    int ORDER_HIGHEST = Integer.MIN_VALUE;

    int ORDER_LOWEST = Integer.MAX_VALUE;
}
