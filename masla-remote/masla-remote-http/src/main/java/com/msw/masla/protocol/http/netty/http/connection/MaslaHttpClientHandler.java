package com.msw.masla.protocol.http.netty.http.connection;

import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.event.EventState;
import com.msw.masla.protocol.http.netty.exception.ServerClosedChannelException;
import com.msw.masla.protocol.http.netty.factory.MaslaEventLoopGroupFactory;
import com.msw.masla.protocol.http.netty.http.decode.MaslaDecode;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * Created by Gavin.peng on 2017/5/16.
 */
@ChannelHandler.Sharable
public class MaslaHttpClientHandler extends ChannelInboundHandlerAdapter {

    protected static final Logger LOG = LoggerFactory.getLogger(MaslaHttpClientHandler.class);

    public static final String MASLA_NETTY_CLIENT_HANDLE = "maslaHttpHandler";


    private MaslaDecode maslaDecode;


    public MaslaHttpClientHandler(){
        //this.clientChannelPoolHandler = clientChannelPoolHandler;
    }

    private static class MaslaHttpClientHandlerHolder {

        static MaslaHttpClientHandler instance = new MaslaHttpClientHandler();
    }

    public static MaslaHttpClientHandler getInstance(){
        return MaslaHttpClientHandlerHolder.instance;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse fullHttpResponse = (FullHttpResponse) msg;
        maslaDecode.receiveComplete(ctx.channel(), fullHttpResponse);
        //this.maslaDecode = null;//help gc not wait channel close
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //LOG.info("Found channel {} is active",ctx.channel().remoteAddress());
        //ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(!ctx.channel().closeFuture().isDone()) {
            disalbeMultiplexAndClose(ctx.channel());
        }
        //LOG.info("Found channel local {} is receive FIN request",ctx.channel().remoteAddress());
        ChannelContext maslaContext = ctx.channel().attr(ChannelContext.CONTEXT_KEY).get();
        if(maslaContext != null){
            LOG.warn("Masla found channel {} is closed in exclusive state for request {}",ctx.channel().remoteAddress(), maslaContext.getRequestUrl());
            maslaDecode.receiveException(ctx.channel(), new ServerClosedChannelException("connection is closed by server:" + ctx.channel().remoteAddress()));
        }

    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            //如果发生读空闲，而且连接在用，说明超时,需要关闭连接
            if(((IdleStateEvent) evt).state() == IdleState.READER_IDLE){
                if(!ctx.channel().closeFuture().isDone()) {
                    disalbeMultiplexAndClose(ctx.channel());
                }else{
                    LOG.info("Masla found channel {} is idle but already close",ctx.channel().remoteAddress());
                }
                ChannelContext<IOSession, HttpRequest, HttpResponse> maslaContext = ctx.channel().attr(ChannelContext.CONTEXT_KEY).get();
                if(maslaContext != null){
                    BaseEvent event = maslaContext.getEvent();
                    long now = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
                    int idleTimeout = MaslaEventLoopGroupFactory.getInstance().getNettyConfig().getSoReadTimeout();
                    if(event.getState() == EventState.REQUESTING
                            && now - event.getStartSendTime() < idleTimeout){
                        LOG.warn("Masla found channel {} send time is {} really idle timeout less than {} for request {},so discard this idle timeout",ctx.channel().remoteAddress(),event.getStartSendTime(),idleTimeout, maslaContext.getHttpRequest().uri());
                        return;
                    }
                    LOG.warn("Masla found channel {} is bussiness timeout for request {} event {}",ctx.channel().remoteAddress(), maslaContext.getHttpRequest().uri(),event.getState());
                    maslaDecode.idleTimeout(ctx.channel());
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LOG.error("Receive exception event from channel {} exception:",ctx.channel().remoteAddress(),cause);
        disalbeMultiplexAndClose(ctx.channel());
        maslaDecode.receiveException(ctx.channel(), cause);

    }


    private void disalbeMultiplexAndClose(Channel channel){
        LOG.info("Masla found channel {} is idle timeout,available disable multiplex!!!",channel.remoteAddress());
        try {
            channel.close();

        }catch (Exception e){
            LOG.error("Masla disable channel {} multiplex status and close failed:{}",channel.remoteAddress(),e.getMessage());
        }
    }




    public MaslaDecode getMaslaDecode() {
        return maslaDecode;
    }

    public void setMaslaDecode(MaslaDecode maslaDecode) {
        this.maslaDecode = maslaDecode;
    }
}
