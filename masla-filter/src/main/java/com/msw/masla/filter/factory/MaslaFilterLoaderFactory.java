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
