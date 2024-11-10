package com.msw.masla.core.push.engine;

import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.event.EventState;
import com.msw.masla.core.async.handle.EventHandler;
import com.msw.masla.core.async.handle.factory.HandlerFactory;
import com.msw.masla.core.async.handle.factory.MaslaHandlerFactory;
import com.msw.masla.core.log.RequestSampler;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.protocol.http.netty.exception.MaslaException;
import com.msw.masla.protocol.http.netty.exception.ServerClosedChannelException;
import com.msw.masla.core.utils.NettyCommonUtil;

import com.msw.masla.common.enums.ErrorType;
import com.msw.masla.common.enums.RequestDispatchMode;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;


/**
 * Created by Gavin.peng on 2017/6/12.
 */
public abstract class BasePushEngine implements PushEngine<ChannelContext, BaseEvent<Object>, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(BasePushEngine.class);


    private ConcurrentHashMap<Long, RequestSampler> timeoutSamplerMap = new ConcurrentHashMap<Long, RequestSampler>();
    private ConcurrentHashMap<Long, RequestSampler> retrySamplerMap = new ConcurrentHashMap<Long, RequestSampler>();
    private ConcurrentHashMap<Long, RequestSampler> connClosedSamplerMap = new ConcurrentHashMap<Long, RequestSampler>();

    protected HandlerFactory handlerFactory;

    private MaslaDefaultProxyInvokerFactory maslaDefaultProxyInvokerFactory;

    public BasePushEngine(MaslaDefaultProxyInvokerFactory factory){
        this.handlerFactory = MaslaHandlerFactory.getInstance();
        this.maslaDefaultProxyInvokerFactory = factory;
        //this.initShutdownHook();
    }

    protected void initPushContext(ChannelContext context){


    }

    protected void doPush(ChannelContext<IOSession, HttpRequest, HttpResponse> context, BaseEvent event){
        try {

            if(context.needPush()){
                initPushContext(context);
                event.setState(EventState.PUSHING);
                EventHandler handler = handlerFactory.create(event);
                handler.handle(context, event);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Masla push request {}  response complete status {}", context.getHttpRequest().uri(), event.getState());
                }
                if(event.getResult() == null
                            && event.getErrorCause() != null){
                        logError(context,event);
                }
            }else{
                try {
                    if (null != event.getErrorCause()) {
                        if(LOG.isInfoEnabled()) {
                            LOG.info("send gray request {} error:{}", context.getRequestUrl(), event.getErrorCause());
                        }
                    } else if (null == event.getResult()) {
                        if(LOG.isInfoEnabled()) {
                            LOG.info("send gray request {} error,has no response.", context.getRequestUrl());
                        }
                    } else {
                        FullHttpResponse httpResponse = (FullHttpResponse) event.getResult();
                        if (httpResponse.status().code() >= HttpResponseStatus.BAD_REQUEST.code()) {
                            if(LOG.isInfoEnabled()) {
                                LOG.info("send gray request {} error,the response code is {}", context.getRequestUrl(), httpResponse.status().code());
                            }
                        }
                    }
                }finally {
                    //copy的流量不需要push，需要网关层自己release byte buffer
                    FullHttpResponse httpResponse = (FullHttpResponse) event.getResult();
                    ReferenceCountUtil.release(httpResponse);
                }
            }

        } catch (Throwable e) {
            LOG.error("Masla push request {} response status {} failed", context.getHttpRequest().uri(), event.getState(), e);
        } finally {
            try {

            } finally {
                if ((context.getRequestDispatchMode() == RequestDispatchMode.COPY
                        ||context.getRequestDispatchMode() == RequestDispatchMode.MQ)
                        && context.getHttpRequest() != null
                        && ((FullHttpRequest)context.getHttpRequest()).refCnt() > 0) {   // 宝山机房的请求不拷贝，不发给mq
                    if(LOG.isDebugEnabled()){
                        LOG.debug("Masla found request {} need replay for {}",context.getRequestUrl(),context.getRequestDispatchMode().name());
                    }

                    context.recycle();

                }else if(context.getRequestDispatchMode() == RequestDispatchMode.VARNISH_DISPATCH) {
                    //重新发起的请求，需要沿用本次的上下文，context不recycle,
                }else {
                    context.recycle();
                }

                // 转发mq的时候，因为要获取到response，先不recycle(),发送完mq再recycle()
                if (context.getRequestDispatchMode() != RequestDispatchMode.MQ||event.getResult() == null) {
                    event.recycle();
                }

            }
        }
    }

    private void logError(ChannelContext context, BaseEvent event) {
        ServiceApp appDO = context.getService();

        if (event.getErrorCause() instanceof TimeoutException) {

        } else if (event.getErrorCause() instanceof MaslaException) {

        } else if (event.getErrorCause() instanceof ServerClosedChannelException) {

        } else {
        }
    }






}
