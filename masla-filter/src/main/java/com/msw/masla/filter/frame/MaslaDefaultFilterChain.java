package com.msw.masla.filter.frame;

import com.msw.masla.filter.exception.FilterException;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.filter.servlet.MaslaServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Gavin.peng on 2017/9/26.
 */
public class MaslaDefaultFilterChain implements MaslaFilterChain {

    public static final Logger LOG = LoggerFactory.getLogger(MaslaDefaultFilterChain.class);


    private MaslaFilter[] filters;

    /**
     * The int which is used to maintain the current position
     * in the filter chain.
     */
    private int pos = 0;


    /**
     * The int which gives the current number of filters in the chain.
     */
    private int filterCnt = 0;

    /**
     * The servlet instance to be executed by this chain.
     */
    private MaslaServlet servlet = null;



    public MaslaDefaultFilterChain(MaslaFilter[] filters, MaslaServlet servlet){
        this.filters = filters;
        this.servlet = servlet;
        this.filterCnt = filters.length;
    }



    @Override
    public void doFilter(ChannelContext requestContext, BaseEvent event) throws FilterException {
        //filter chain 执行完以后，执行servlet，如果其中一个filter执行失败，不影响当前请求,继续执行下一个
        if (pos < filterCnt) {
            MaslaFilter filter = filters[pos++];
            try {
                filter.doFilter(requestContext, event, this);
            }catch (Throwable e){
                LOG.error("Masla  found request {} do filter {} failed and no catch exception:",requestContext.getRequestUrl(),filter.mappingPath(),e);
                doService(requestContext,event);
            }
        } else {
            doService(requestContext,event);
        }
    }

    private void doService(ChannelContext requestContext, BaseEvent event) throws FilterException{
        try {
            servlet.service(requestContext, event);
        }catch (IOException e){
            throw new FilterException(e);
        }
        catch (Throwable e){
            LOG.error("Masla  exec servlet failed:",e);
            throw new FilterException("Masla  netty servlet error",e);
        }
    }
}
