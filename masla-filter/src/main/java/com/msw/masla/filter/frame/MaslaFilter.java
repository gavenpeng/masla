package com.msw.masla.filter.frame;

import com.msw.masla.filter.exception.FilterException;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * Author: Gavin.peng
 * Date: 2024/3/31
 * Description:
 *  Masla filter common interface
 */
public interface MaslaFilter {

    String mappingPath();

    void doFilter(ChannelContext<IOSession, HttpRequest, HttpResponse> context, BaseEvent event, MaslaFilterChain filterChain) throws FilterException;

    void init();

    void order();


}
