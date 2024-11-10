package com.msw.masla.filter.frame;

import com.msw.masla.core.utils.NettyCommonUtil;
import com.msw.masla.filter.exception.FilterException;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Author: Gavin.peng
 * Date: 2024/3/31
 * Description:
 */
public abstract class AbstractMaslaFilter implements MaslaFilter{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMaslaFilter.class);


    @Override
    public void doFilter(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent event,
                         MaslaFilterChain filterChain) throws FilterException {

        if (NettyCommonUtil.isMaslaMetaPath(requestContext)) {
            filterChain.doFilter(requestContext, event);
            return;
        }

        try {
            if (!apply(requestContext, event)) {
                return;
            }
        }catch (Throwable e){
            LOG.error("Masla found request {} do filter {} failed:",requestContext.getRequestUrl(),this.getName(),e);
        }
        filterChain.doFilter(requestContext,event);

    }

    public abstract boolean apply(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent event) throws FilterException;

    public abstract String getName();

    @Override
    public void init() {

    }

    @Override
    public String mappingPath() {
        return "/*";
    }
}
