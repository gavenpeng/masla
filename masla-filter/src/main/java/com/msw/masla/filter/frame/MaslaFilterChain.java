package com.msw.masla.filter.frame;

import com.msw.masla.filter.exception.FilterException;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;

/**
 * Author: Gavin.peng
 * Date: 2024/3/31
 * Description:
 */
public interface MaslaFilterChain {

    void doFilter(ChannelContext maslaContext, BaseEvent event) throws FilterException;
}
