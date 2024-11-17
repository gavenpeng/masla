package com.msw.masla.server.ssl;

import com.msw.masla.protocol.http.netty.codec.MaslaHttpObjectAggregator;
import com.msw.masla.protocol.http.netty.config.ChannelConfig;
import com.msw.masla.protocol.http.netty.http.handler.MaslaServerHttpRequestDecode;
import com.msw.masla.protocol.http.netty.ssl.AbstractHttp2SslChannelInitializer;
import com.msw.masla.server.handler.MaslaServerChannelHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * Author: Gavin.peng
 * Date: 2024/4/14
 * Description:
 */
public class MaslaChannelInitialier extends AbstractHttp2SslChannelInitializer {


    public MaslaChannelInitialier(ChannelConfig channelConfig,
                                  ChannelGroup channels) {
        super(channelConfig, channels);
    }


    @Override
    public void addMaslaHttpHandlers(final ChannelPipeline pipeline)
    {
        final ChannelHandler maslaServerHandler = MaslaServerChannelHandler.getInstance();
        //支持先压缩，再encode为http2的frame,默认小于1024的不压缩
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024*1024));
        pipeline.addLast(MaslaServerChannelHandler.MASLA_NETTY_SERVER_HANDLE,maslaServerHandler);
    }

    @Override
    public void addHttp1Handlers(final ChannelPipeline pipeline)
    {
        final ChannelHandler maslaServerHandler = MaslaServerChannelHandler.getInstance();
        pipeline.addLast("httpDecode",new MaslaServerHttpRequestDecode(4096,65536,8192));
        pipeline.addLast("httpEncode",new HttpResponseEncoder());
        pipeline.addLast("aggregator", new MaslaHttpObjectAggregator(1024*1024));
        pipeline.addLast(MaslaServerChannelHandler.MASLA_NETTY_SERVER_HANDLE,maslaServerHandler);
    }
}
