package com.msw.masla.protocol.http.netty.http.decode;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * Created by Gavin.peng on 2017/5/23.
 */
public interface MaslaDecode {

    public void receiveComplete(Channel channel, FullHttpResponse fullHttpResponse);

    public void receiveException(Channel channel,Throwable e);

    //空闲超时
    public void idleTimeout(Channel channel);

    //读结果超时
    public void readTimeout(Channel channel);


    public void forceClose(Channel channel);



}
