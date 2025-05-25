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
package com.msw.masla.filter.factory;

import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.filter.frame.MaslaFilter;
import com.msw.masla.filter.servlet.MaslaServlet;
import com.msw.masla.filter.spi.FilterSpiLoader;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("filterLoaderFactory")
public class MaslaFilterLoaderFactory implements InitializingBean {

	@Autowired
	private MaslaDefaultProxyInvokerFactory proxyInvokerFactory;

	public void init() throws Exception {

		//init masla filter and servlet by spi
		List<MaslaFilter> filterList = FilterSpiLoader.instance(MaslaFilter.class).loadInstanceListSorted();
		List<MaslaServlet> servletList = FilterSpiLoader.instance(MaslaServlet.class).loadInstanceListSorted();

		for (MaslaFilter filter : filterList) {
			filter.init();
			MaslaFilterBeanFactory.getInstance().registerFilter(filter.mappingPath(), filter);
		}

		for (MaslaServlet servlet : servletList) {
			servlet.init(proxyInvokerFactory);
			MaslaFilterBeanFactory.getInstance().registerServlet(servlet.mappingPath(), servlet);
		}

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}
}
