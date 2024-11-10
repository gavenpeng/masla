package com.msw.masla.core.async.repsone;

import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.monitor.metrics.BandwidthCount;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.event.EventState;
import com.msw.masla.protocol.http.netty.event.IEvent;
import com.msw.masla.core.push.engine.AsyncPushEngine;
import com.msw.masla.core.push.engine.PushEngine;
import com.msw.masla.core.push.engine.SyncPushEngine;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.protocol.http.netty.codec.MaslaChannelAttribute;
import com.msw.masla.protocol.http.netty.http.decode.MaslaDecode;
import com.msw.masla.protocol.http.netty.pool.SimpleChannelPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Gavin.peng on 2024/5/23.
 */
public class MaslaHttpDecode implements MaslaDecode {

    private static final Logger LOG = LoggerFactory.getLogger(MaslaHttpDecode.class);

    private PushEngine asynPushEngine;

    private PushEngine syncPushEngine;

    private static class MaslaHttpDecodeHolder{
        static final MaslaHttpDecode instance = new MaslaHttpDecode();
    }

    public static MaslaHttpDecode getInstance(){
        return MaslaHttpDecodeHolder.instance;
    }

    public void initPushEngine(MaslaDefaultProxyInvokerFactory factory){
        this.asynPushEngine =  AsyncPushEngine.getPushEngine(factory);
        this.syncPushEngine = SyncPushEngine.getPushEngine(factory);
    }



    private MaslaHttpDecode(){}

    @Override
    public void receiveComplete(final Channel channel,FullHttpResponse fullHttpResponse) {
        decodeResponse(channel,fullHttpResponse);
    }




    public void decodeResponse(final Channel channel,FullHttpResponse fullHttpResponse) {
        final ChannelContext<IOSession, HttpRequest, HttpResponse> curContext = channel.attr(ChannelContext.CONTEXT_KEY).getAndSet(null);
        if(curContext == null){
            ReferenceCountUtil.release(fullHttpResponse);
            return;
        }


        if (fullHttpResponse.status().code() >= HttpResponseStatus.BAD_REQUEST.code()) {
            String requestHost = curContext.getHttpRequest() == null?"no host":curContext.getHttpRequest().headers().get(HttpHeaderNames.HOST);
            String requestUrl = curContext.getHttpRequest() == null?"no path":curContext.getHttpRequest().uri();
            if(LOG.isInfoEnabled()) {
                LOG.info("Masla request {} host {} receive error response status code {} from remote {}", requestUrl, requestHost, fullHttpResponse.status().code(), channel.remoteAddress());
            }
        }else {
            if(LOG.isDebugEnabled()){
                String requestHost = curContext.getHttpRequest() == null?"no host":curContext.getHttpRequest().headers().get(HttpHeaderNames.HOST);
                String requestUrl = curContext.getHttpRequest() == null?"no path":curContext.getHttpRequest().uri();
                LOG.debug("Masla request {} host {} receive error response status code {} from remote {}", requestUrl,requestHost,fullHttpResponse.status().code(),channel.remoteAddress());
            }
        }

        int respLineLength = channel.attr(MaslaChannelAttribute.RESP_LINE_SIZE).getAndSet(0);
        int respHeaderLength = channel.attr(MaslaChannelAttribute.RESP_HEADER_SIZE).getAndSet(0);
        int respContentLength = fullHttpResponse.content().readableBytes();
        int totalLength = respLineLength+respHeaderLength+respContentLength;

        ServiceApp appDO = curContext.getService();

        if(fullHttpResponse.status().code() < HttpResponseStatus.BAD_REQUEST.code()){
          String serviceId = curContext.getServiceIdentify();
          if(serviceId != null){
            BandwidthCount bandwidthCount = appDO.getAppBandwidthCount(serviceId);
            if(bandwidthCount != null){
              bandwidthCount.getOutLineBWCount().addAndGet(respLineLength);
              bandwidthCount.getOutHeaderBWCount().addAndGet(respHeaderLength);
              bandwidthCount.getOutBodyBWCount().addAndGet(respContentLength);
            }
          }
        }
        curContext.setResponseContentLength(totalLength);
        final BaseEvent event = curContext.getEvent();

        if(null != curContext.getScheduledFuture()){
            if(!curContext.getScheduledFuture().cancel(false)){
                LOG.warn("Masla found request {} task submit time {} time out task is cannel failed",curContext.getRequestUrl(),event.getSubmitTime());
            }
        }

        event.setState(EventState.RESPONSE_COMPLETE);
        event.setResult(fullHttpResponse);
        event.setResponseCompleteTime(System.nanoTime());
        if (HttpHeaderValues.CLOSE.toString().equals(fullHttpResponse.headers().get(HttpHeaderNames.CONNECTION))) {
            channel.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    //LOG.info("Channel {} is closed start to push request {} response ", channel.remoteAddress(), curContext.getRequestUrl());
                    releaseAndPush(curContext,event,channel);
                }
            });
        } else {
            releaseAndPush(curContext,event,channel);
        }

    }

    private void releaseAndPush(final ChannelContext maslaContext, final IEvent event, final Channel channel){

        releaseChannel(channel);

        if("nio".equals(MaslaSpringContextUtil.getMaslaConfConfigBean().getPushThreadMode())){
            syncPushEngine.push(maslaContext,event);
        }else{
            asynPushEngine.push(maslaContext, event);
        }


    }

    private void releaseChannel(Channel channel){
        final SimpleChannelPool channelPool = channel.attr(SimpleChannelPool.POOL_KEY).get();
        if(channelPool != null) {
            try {
                channelPool.release(channel, channel.voidPromise());
            }catch (Throwable e){
                LOG.error("Masla found channle {} is already release to pool",channel.remoteAddress());
            }
        }
    }


    private void closeAndRelease(final Channel channel){
        channel.close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                releaseChannel(channel);
            }
        });
    }

    @Override
    public void receiveException(final Channel channel,final Throwable e) {
        final ChannelContext<IOSession, HttpRequest, HttpResponse> curContext = channel.attr(ChannelContext.CONTEXT_KEY).getAndSet(null);
        //添加了timeout task后，防止重复push
        if(curContext != null) {
            //final BaseEvent event = channel.attr(BaseEvent.EVENT_KEY).getAndSet(null);
            final BaseEvent event = curContext.getEvent();
            /*
             * 这里如果是服务端链接关闭，导致重试，netty会有两个回调的地方，
             * 1 一个是channel上的inactive,这个是先回调，这里回调时会关闭释放链接，并push，释放请求资源。
             * 2 一个是写channel上的future，这里是会检查是否可以重试，如果是第一次发起被关闭，则会重试
             *
             * 所以这里需要判断，如果是写的过程中出现服务端关闭链接，则这里不需要push，因为会触发写失败回调，
             * 去做重试，如果不能重试也会触发push，如果这里push了，回调去重试，会出现资源已经释放的问题。
             *
             */
            if (event.getState() == EventState.REQUESTING) {
                if(LOG.isInfoEnabled()){
                    LOG.info("Masla found request {} channel {} is error but not send complete,so not push close retry,err msg:{}", curContext.getHttpRequest().uri(), channel.remoteAddress(),e.getMessage());
                }
                if (curContext.getScheduledFuture() != null) {
                    curContext.getScheduledFuture().cancel(false);
                }
                //如果是发送中出现异常，肯定是服务端关闭了链接。
                forceClose(channel);
            }else {
                event.setState(EventState.RESPONSE_RECEIVE_EXCEPTION);
                event.setRemoteException(e);
                event.setResponseCompleteTime(System.nanoTime());
                //取消超时任务
                if (null != curContext.getScheduledFuture()) {
                    if(LOG.isInfoEnabled()){
                        LOG.info("Masla found channel {} exception but request {} is flush err msg:", channel.remoteAddress(), curContext.getHttpRequest().uri(),e.getMessage());
                    }
                    curContext.getScheduledFuture().cancel(false);
                }
                closeAndPush(curContext, event, channel);
            }
        }else{
            forceClose(channel);
            //channelPool.release(channel, channel.voidPromise());
        }

    }

    private void closeAndPush(final ChannelContext maslaContext, final BaseEvent event, final Channel channel){

        if (channel != null) {
            if(!channel.closeFuture().isDone()) {
                channel.close().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        //在用的连接，需要释放回去
                        releaseAndPush(maslaContext, event, channel);
                        //releaseAndPush(channel);
                    }
                });
            }else{
                releaseAndPush(maslaContext, event, channel);
            }
        }

    }

    @Override
    public void idleTimeout(final Channel channel) {

        final ChannelContext curContext = channel.attr(ChannelContext.CONTEXT_KEY).getAndSet(null);
        if(curContext !=null) {
            final BaseEvent event = curContext.getEvent();
            //没有发送完成，有可能只发一部分，还是可以重试，所以这里只关闭连接,让发送future 重试
            if (event.getState() == EventState.REQUESTING) {
                LOG.warn("Masla found request {} channel {} is idle timeout but not send complete,close channel,may be is internet is problem!!", curContext.getRequestUrl(), channel.remoteAddress());
                if (curContext.getScheduledFuture() != null) {
                    curContext.getScheduledFuture().cancel(false);
                }
                //发送超时，不重试，重试只会问题更严重，发不出去的时候基本出现问题了
                //还有就是发送出去了，业务很久没有返回，io线程又block了，超时任务不能执行，这种情况基本不存在。
//              forceClose(channel);
            }
            //
            event.setResponseCompleteTime(System.nanoTime());
            event.setState(EventState.RESPONSE_RECEIVE_EXCEPTION);
            event.setRemoteException(new TimeoutException("Read response timeout:" + channel.remoteAddress()));
            showCostTime(channel, curContext, event);
            closeAndPush(curContext,event,channel);
                //this.receiveException(channel, new TimeoutException("Read response timeout:" + channel.remoteAddress()));

        }else{
            forceClose(channel);
            //releaseChannel(channel);
        }

    }




    @Override
    public void forceClose(final Channel channel) {
        if(channel != null) {
            //先关闭，再释放
            if(!channel.closeFuture().isDone()) {
                channel.close().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                          releaseChannel(channel);
                    }
                });
            }else{
                releaseChannel(channel);
            }
        }
    }


    @Override
    public void readTimeout(final Channel channel) {

        final ChannelContext bindContext = channel.attr(ChannelContext.CONTEXT_KEY).getAndSet(null);
        //bindContext 为空，说明1 请求要么响应，2出现异常，比如服务端关闭了连接，
        if(bindContext != null) {
            final BaseEvent event = bindContext.getEvent();
            long costTime = showCostTime(channel, bindContext, event);
            //BaseEvent event1 = (BaseEvent)this.event;
            //需要判断是否已经flush，如果没有，则说明写慢，则close 连接让其重试
            if (event.getState() == EventState.REQUESTING) {
                LOG.warn("Masla found request {} channel {} is write timeout but not send complete,so not push close retry", bindContext.getRequestUrl(), channel.remoteAddress());
//                if (bindContext.getScheduledFuture() != null) {
//                    bindContext.getScheduledFuture().cancel(false);
//                }
                //出现超时，但是请求还没有发送，代表是io线程被block了，不能再重试。
//                forceClose(channel);
                //记录发送超时
                event.setRemoteException(new TimeoutException("Send timeout to :" + channel.remoteAddress() + " send request cost:" + costTime));
            }else{
                //已经发送，读超时异常
                event.setRemoteException(new TimeoutException("Read response timeout from :" + channel.remoteAddress() + " server side cost:" + costTime));
            }
            event.setResponseCompleteTime(System.nanoTime());
            event.setState(EventState.RESPONSE_FAILED);
            closeAndPush(bindContext, event, channel);

        }

    }

    private long showCostTime(Channel channel, ChannelContext<IOSession, HttpRequest, HttpResponse> maslaContext, BaseEvent event){
        long acquireTime = event.getStartSendTime() - event.getStartAcquireConnTime();
        long now = System.nanoTime();

        if(event.getSendCompleteTime()<=0){
            event.setSendCompleteTime(now);
        }


        if(event.getStartEncodeTime()<=0){
            event.setStartEncodeTime(now);
        }

        long ioQueueTime = event.getStartEncodeTime() - event.getStartSendTime();
        long sendTime = event.getSendCompleteTime() - event.getStartEncodeTime();

        if(event.getResponseCompleteTime()<=0){
            event.setResponseCompleteTime(now);
        }

        long servTime = event.getResponseCompleteTime() - event.getSendCompleteTime();

        if(LOG.isInfoEnabled()) {
            LOG.info("Request {} acquire time {} io queue time {} send time {} server cost time {} channel {}", maslaContext.getHttpRequest().uri(),
                    acquireTime, ioQueueTime, sendTime, servTime,channel.remoteAddress());
        }
        return servTime>0?servTime:sendTime;
    }

}
