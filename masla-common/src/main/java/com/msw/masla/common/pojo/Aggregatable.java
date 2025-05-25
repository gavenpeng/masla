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
