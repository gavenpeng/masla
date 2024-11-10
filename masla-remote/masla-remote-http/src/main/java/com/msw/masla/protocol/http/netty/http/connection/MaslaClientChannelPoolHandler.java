package com.msw.masla.protocol.http.netty.http.connection;

import com.google.common.collect.Sets;
import com.msw.masla.protocol.http.netty.codec.MaslaHttpRequestEncoder;
import com.msw.masla.protocol.http.netty.http.handler.MaslaReadIdleStateHandler;
import com.msw.masla.protocol.http.netty.http.decode.MaslaClientHttpResponseDecoder;
import com.msw.masla.protocol.http.netty.pool.MaslaChannelPool;
import io.netty.channel.*;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gavin.peng on 2017/5/16.
 */
public class MaslaClientChannelPoolHandler implements ChannelPoolHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MaslaClientChannelPoolHandler.class);
    private Set<Channel> channels = Sets.newConcurrentHashSet();


    //private Timer timer = new Timer("netty-transport-channelpool-heartbeater-timer");
    //private List<ChannelHandlerFactory> channelHandlerFactories;
    private MaslaChannelPool channelPool;
    private static final int MAX_CONTENT_LENGTH = 1024*1024*20;//20M
    private static final int INNER_MAX_CONTENT_LENGTH = Integer.MAX_VALUE;
    private int readIdleSec;
    private int writeIdleSec;

    public MaslaClientChannelPoolHandler(Integer idleCheckIntervalSec,
                                         List channelHandlerFactories) {
        //this.channelHandlerFactories = channelHandlerFactories;
        this.readIdleSec = idleCheckIntervalSec;
        this.writeIdleSec = 0;
        //timer.schedule(new ShowChannleStateTask(),1000,10000);
    }

    @Override
    public void channelReleased(Channel channel) throws Exception {
        if(LOG.isDebugEnabled()) {
            LOG.debug("channel in pool released: {}", channel);
        }
        if(!channel.isOpen() || !channel.isActive()){
            channels.remove(channel);
        }
        //channel.attr(ChannelContext.CONTEXT_KEY).set(null);

    }

    @Override
    public void channelAcquired(Channel channel) throws Exception {
        if(LOG.isDebugEnabled()) {
            LOG.debug("channel in pool acquired: {}", channel);
        }

    }

    @Override
    public void channelCreated(final Channel channel) throws Exception {
        ChannelPipeline channelPipeline = channel.pipeline();
        //记录链接池里链接的个数
        channelPool.incrementChannelCount();
        channelPipeline.addLast("idleStateHandler", new MaslaReadIdleStateHandler(readIdleSec, 0, 0, TimeUnit.MILLISECONDS));
        channelPipeline.addLast("httpEncode",new MaslaHttpRequestEncoder());
        channelPipeline.addLast("httpDecode",new MaslaClientHttpResponseDecoder());
        channelPipeline.addLast("aggregator", new HttpObjectAggregator(MAX_CONTENT_LENGTH));
        channelPipeline.addLast(MaslaHttpClientHandler.MASLA_NETTY_CLIENT_HANDLE, MaslaHttpClientHandler.getInstance());
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                channels.remove(channel);
                //空闲关闭时
                channelPool.decrementChannelCount();
                //在masla 主动关闭时，channel 是不在pool 里的，这是大部分情况，
                //只有空闲时，在pool 里，然后对端发起了关闭，但是在获取channel时，会判断
                //如果是close的连接，会丢弃，去重新获取,这个相对主动关闭很少
            }
        });
        channels.add(channel);
    }

    public MaslaChannelPool getChannelPool() {
        return channelPool;
    }

    public void setChannelPool(MaslaChannelPool channelPool) {
        this.channelPool = channelPool;
    }

    public void close() {
        //timer.cancel();
    }
}
