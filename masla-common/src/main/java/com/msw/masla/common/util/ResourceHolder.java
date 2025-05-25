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
package com.msw.masla.common.util;

/**
 * 资源持有者接口, 为ConcurrentInitializer调用
 *
 * @param <K> 获取资源的Key类型，若资源只是单个对象，不是map结构，则现实方法可以不使用key
 * @param <V> 资源的类型
 * @param <E> 资源初始化时可能抛出的异常。若没有异常抛出，则设置E为RuntimeException
 */
public interface ResourceHolder<K, V, E extends Throwable> {

	/**
	 * 返回key对应的当前资源。如果不是map结构，子类实现可以不理会key
	 */
	V currentData(K key);

	/**
	 * 初始化key对应的资源，并返回。如果不是map结构，子类实现可以不理会key
	 */
	V initializeDate(K key) throws E;

}
