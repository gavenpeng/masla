package com.msw.masla.common.pojo;

/**
 * 可以聚合的metric
 *
 * @author: acone.wu
 */
public interface Aggregatable<T> {

    /**
     * 聚合
     *
     * @param other 需要聚合的metric
     */
    void aggregate(T other);

    /**
     * 获得所在网关名称
     *
     * @return 网关分组名称
     */
    String getGroupName();

}
