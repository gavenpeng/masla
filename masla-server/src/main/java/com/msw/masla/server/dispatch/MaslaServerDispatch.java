package com.msw.masla.server.dispatch;


import com.msw.masla.common.config.MaslaServerConfig;
import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.util.CollectionUtil;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.common.util.StringBuilderHolder;
import com.msw.masla.core.async.context.MaslaRequestContextBuilder;
import com.msw.masla.core.utils.NettyCommonUtil;
import com.msw.masla.filter.exception.FilterException;
import com.msw.masla.filter.frame.MaslaDefaultFilterChain;
import com.msw.masla.filter.frame.MaslaFilter;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.server.AbstractEndpoint;
import com.msw.masla.filter.factory.MaslaFilterBeanFactory;
import com.msw.masla.filter.servlet.MaslaServlet;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Created by Gavin.peng on 2017/9/26.
 */
public class MaslaServerDispatch extends AbstractHttpDispatch {

    private MaslaFilterBeanFactory containerFactory;
    private AbstractEndpoint endpoint;
    private static volatile MaslaServerDispatch instance;


    private MaslaServerDispatch(AbstractEndpoint endpoint){
        this.endpoint = endpoint;
        this.containerFactory = MaslaFilterBeanFactory.getInstance();
    }


    public static MaslaServerDispatch getInstance(AbstractEndpoint endpoint){

        if(instance != null){
            return instance;
        }
        synchronized (MaslaServerDispatch.class){
            if(instance == null){
                instance = new MaslaServerDispatch(endpoint);
            }
        }
        return instance;
    }


    @Override
    protected void doDispatch(IOSession session, FullHttpRequest request) {

        String requestPath = StringBuilderHolder.getGlobal().append(session.getContextRoot()).append(session.getPath()).toString();
        BaseEvent event = MaslaRequestContextBuilder.buildNettyDispatchEvent();
        SessionContext<IOSession, HttpRequest, HttpResponse> reqContext = MaslaRequestContextBuilder.buildMaslaDispatchConext(session, request, event);


        MaslaServerConfig maslaServerConfig = (MaslaServerConfig) MaslaSpringContextUtil.getBean("maslaServerConfig");
        LOG.info("Server config port:{}", maslaServerConfig.getPort());

        //the app's health check
        if (session.getContextRoot().equals(Constants.MASLA_HEALTHCHECK_PATH_END)
                || session.getPath().equals(Constants.MASLA_HEALTHCHECK_PATH_END)) {
            HttpResponse response = NettyCommonUtil.createResponse(HttpResponseStatus.OK,"Malsa healthcheck status is ok!!!");
            session.writeAndFlush(response);
            reqContext.getEvent().recycle();
            reqContext.recycle();
            return;
        }


        //Select matcher filter
        List<MaslaFilter> matcherFilter = null;
        List<Pattern> filterList = this.containerFactory.getFilterMappingList();
        if(filterList != null && filterList.size() > 0){
            matcherFilter = new ArrayList<MaslaFilter>(10);
            for(Pattern filterMapping:filterList){
               if(filterMapping.matcher(requestPath).matches()){
                   try {
                       List<MaslaFilter> filters = containerFactory.getFilter(filterMapping.pattern());
                       if(!CollectionUtil.isEmpty(filters)){
                           matcherFilter.addAll(filters);
                       }
                   }catch (Throwable e){
                       LOG.error("Masla server load filter failed:",e);
                       break;
                   }
               }
            }
        }

        //Select matcher servelet
        MaslaServlet servlet = null;
        List<Pattern> servletList = this.containerFactory.getServletMappingList();
        if(servletList != null && !servletList.isEmpty()){
            for(Pattern servletMapping:servletList){
                if(servletMapping.matcher(requestPath).matches()){
                    try {
                        servlet = containerFactory.getServlet(servletMapping.pattern());
                        break;
                    }catch (Throwable e){
                        LOG.error("Masla server load servlet failed:",e);
                        break;
                    }
                }
            }
        }

        if(servlet == null){
            LOG.error("Masla can not mapping a servlet for request {}",request.uri());
            session.writeError(HttpResponseStatus.INTERNAL_SERVER_ERROR,true);
            reqContext.getEvent().recycle();
            reqContext.recycle();
            return;
        }

        MaslaDefaultFilterChain filterChain = new MaslaDefaultFilterChain(matcherFilter.toArray(new MaslaFilter[0]), servlet);
        reqContext.setRequestPath(requestPath);
        reqContext.getSession().addKeepaliveRequest();
        try {
            filterChain.doFilter(reqContext, reqContext.getEvent());
        }catch (FilterException e){
            reqContext.getEvent().recycle();
            reqContext.recycle();
            LOG.error("Masla server dispatch request {} do servlet failed:",reqContext.getRequestUrl(),e);
            //close channel
            session.writeError(HttpResponseStatus.INTERNAL_SERVER_ERROR,true);
        }catch (Throwable e){
            reqContext.getEvent().recycle();
            reqContext.recycle();
            LOG.error("Masla server dispatch request {} failed:",reqContext.getRequestUrl(),e);
            session.writeError(HttpResponseStatus.INTERNAL_SERVER_ERROR,true);
        }


    }




}
