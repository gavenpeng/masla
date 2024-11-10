package com.msw.masla.core.async.handle;

import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.event.EventState;
import com.msw.masla.core.push.processor.impl.CircuitBreakNettyResponseProcessor;
import com.msw.masla.core.push.processor.impl.HttpContentCompressService;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.core.utils.MaslaBackupResponseUitls;
import com.msw.masla.protocol.http.netty.exception.MaslaException;
import com.msw.masla.protocol.http.netty.exception.NoAvailableHostException;
import com.msw.masla.protocol.http.netty.exception.ServerClosedChannelException;
import com.msw.masla.core.utils.NettyCommonUtil;
import com.msw.masla.common.enums.RequestDispatchMode;
import com.msw.masla.core.push.processor.impl.ApiMetricNettyResponseProcessor;
import com.msw.masla.core.push.processor.ResponseParameterProcessor;
import com.msw.masla.core.push.processor.ResponseProcessor;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import java.util.*;
import java.util.concurrent.TimeoutException;



/**
 * Created by Gavin.peng on 2017/6/5.
 */
public class MaslaCommonResponseHandler extends AbstractHandler<FullHttpResponse> {

    private List<ResponseProcessor> prevProcessesList;
    private List<ResponseProcessor> postProcessesList;
    protected HttpContentCompressService contentCompressService;


    private MaslaCommonResponseHandler(){
        prevProcessesList = new ArrayList<ResponseProcessor>(2);
        postProcessesList = new ArrayList<ResponseProcessor>(5);
        prevProcessesList.add(new ResponseParameterProcessor());
        postProcessesList.add(new ApiMetricNettyResponseProcessor());
        postProcessesList.add(new CircuitBreakNettyResponseProcessor());
    }

    static class MaslaResponseHandlerHolder{
       static MaslaCommonResponseHandler instance = new MaslaCommonResponseHandler();
    }

    public static MaslaCommonResponseHandler getInstance(){
        return MaslaResponseHandlerHolder.instance;
    }


    @Override
    protected void doHandle(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<FullHttpResponse> event) throws Throwable {
        prevPush(requestContext, event);
        try{
            if(LOG.isDebugEnabled()) {
                LOG.debug("Masla do push for request {} status {}", requestContext.getRequestUrl(), event.getState());
            }
            push(requestContext, event);
        }catch (Throwable e){
            throw e;
        }finally {
            if(LOG.isDebugEnabled()) {
                LOG.debug("Masla do post push for request {} status {}", requestContext.getRequestUrl(), event.getState());
            }
            try {
                postPush(requestContext, event);
            }finally {
                FullHttpResponse fullHttpResponse = event.getResult();
                if(fullHttpResponse != null) {
                    ReferenceCountUtil.release(fullHttpResponse);
                }
            }

        }

    }


    private void push(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<FullHttpResponse> event)throws Throwable{
        try {

            //如果是网关发起的varnish请求，需要修改下请求类型，保证原始请求的context释放
            if(requestContext.getRequestDispatchMode() == RequestDispatchMode.VARNISH_DISPATCH) {
                requestContext.setRequestDispatchMode(RequestDispatchMode.DEFAULT);
            }

            if(event.getResult() != null){
                FullHttpResponse httpResponse = event.getResult();
                pushResponse(requestContext,httpResponse);
            }else{
                pushNoResponse(requestContext,event);
            }

            if(null == event.getErrorCause()){
                event.setState(EventState.PUSH_SUCCESS_REQUEST_COMPLETE);
            }else{
                event.setState(EventState.PUSH_SUCCESS_REQUEST_FAILED);
            }
        }catch (Throwable e){
            LOG.error("Masla do push request {} response error {}",requestContext.getRequestUrl(),e.getMessage());
            event.setState(EventState.PUSH_FAILED);
            requestContext.getSession().writeAndFlush(NettyCommonUtil.createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,"intranet request url execute failed   -API Gateway"));
            throw e;
        }finally {

        }
    }




    /**
     * 执行响应客户端之前的Processor
     * @param requestContext
     * @param event
     * @throws Throwable
     */
    private void prevPush(ChannelContext requestContext, BaseEvent<FullHttpResponse> event) throws Throwable{

        if (prevProcessesList != null && prevProcessesList.size() > 0) {
            for (ResponseProcessor processor : prevProcessesList) {
                try {
                    processor.process(requestContext, event,null);
                }catch (Throwable e){
                    LOG.error("Masla prev processor {} process request {} response failed",processor.getProcessorName(),requestContext.getRequestUrl(),e);
                }
            }
        }
    }


    /**
     * 执行响应客户端之后的Processor
     * @param requestContext
     * @param event
     * @throws Throwable
     */
    private void postPush(final ChannelContext requestContext, final BaseEvent<FullHttpResponse> event) throws Throwable{
        if (postProcessesList != null && postProcessesList.size() > 0) {
            for (ResponseProcessor processor : postProcessesList) {
                try {
                    processor.process(requestContext, event,null);
                }catch (Throwable e){
                    LOG.error("Masla processor {} process request {} response failed",processor.getProcessorName(),requestContext.getRequestUrl(),e);
                }
            }
        }
    }


    /**
     * 添加刷新流到客户端之前的Processor
     * @param processor
     */
    public void addPrevPushProcessor(ResponseProcessor processor){
        this.prevProcessesList.add(processor);
    }

    /**
     * 添加刷新流到客户端的之后Processor
     * @param processor
     */
    public void addPostPushProcessor(ResponseProcessor processor){
        this.postProcessesList.add(processor);
    }


    private void pushNoResponse(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<FullHttpResponse> event) throws Throwable{
        if(LOG.isInfoEnabled()) {
            LOG.info("intranet request no response, request url {},error msg {}",
                    requestContext.getHttpRequest().uri(), event.getErrorCause());
        }
      HttpResponse httpResponse = null;
        String headerFlag = Constants.MASLA_RESPONSE_HEADER_KEY_VALUE;

        try {

            String serviceId = NettyCommonUtil.getServiceId(requestContext);
            httpResponse = MaslaBackupResponseUitls.fillBackupResponse(requestContext, serviceId);

            Throwable cause = event.getErrorCause();
            if (Constants.WAIT_TIMEOUT_EXCEPTION
                    .equalsIgnoreCase(cause.getMessage())) {
                headerFlag = Constants.MASLA_RESPONSE_HEADER_WATI_CONN_TIMEOUT;
            } else if (cause instanceof TimeoutException) {
                headerFlag = Constants.MASLA_RESPONSE_HEADER_SERVER_TIMEOUT;
            } else if (cause instanceof ServerClosedChannelException) {
                headerFlag = Constants.MASLA_RESPONSE_HEADER_SERVER_CONN_CLOSED;
            } else if (cause instanceof NoAvailableHostException) {
                headerFlag = Constants.MASLA_RESPONSE_HEADER_ROUTER_FAILED;
            }else if (cause instanceof MaslaException) {
                headerFlag = Constants.MASLA_RESPONSE_HEADER_FORWARD_FAILED;
            } else {
                if(!StringUtil.isEmptyString(cause.getMessage())){
                    if (cause.getMessage().contains(Constants.CONN_REFUSED) || cause.getMessage().contains(Constants.CONN_REFUSED_CHINESE)) {
                        headerFlag = Constants.MASLA_RESPONSE_HEADER_CONN_REFUSED;
                    } else if (cause.getMessage().contains(Constants.CONN_RESET)) {
                        headerFlag = Constants.MASLA_RESPONSE_HEADER_SERVER_CONN_RESET;
                    } else if (cause.getMessage().contains(Constants.TIMED_OUT_EXCEPTION)) {
                        headerFlag = Constants.MASLA_RESPONSE_HEADER_CONN_TIMEOUT;
                    } else if (Constants.ACQUIRE_CONN_QUEUE_FULL_EXCEPTION.equalsIgnoreCase(cause.getMessage())) {
                        headerFlag = Constants.MASLA_RESPONSE_HEADER_WATI_QUEUE_FULL;
                    }else{
                        headerFlag = cause.getMessage();
                    }
                }
            }

            if (httpResponse == null) {
                //TODO 响应异常到前端
                String errorContent = headerFlag;
                requestContext.getSession().setError();
                if (event.getErrorCause() instanceof TimeoutException) {
                    httpResponse = NettyCommonUtil.createResponse(NettyCommonUtil.TIMEOUT_REQUESTS, errorContent, headerFlag);
                } else {
                    httpResponse = NettyCommonUtil.createResponse(NettyCommonUtil.REQUEST_FAILED, errorContent, headerFlag);
                }
            } else {
                //增加网关返回头
                httpResponse.headers().add(Constants.MASLA_RESPONSE_HEADER_KEY, headerFlag);
            }

        }catch (Throwable e){
            LOG.error("Masla set failed request {} header error:",requestContext.getRequestUrl(),e);
        }

       //网关返回的标示,方便排查问题
       requestContext.getSession().writeAndFlush(httpResponse);

    }


    private void pushResponse(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, FullHttpResponse httpResponse) throws Throwable {
        //服务正常响应的情况下，也支持自定义响应
        httpResponse.retain();
        requestContext.getSession().writeAndFlush(httpResponse);
    }

    @Override
    protected void initHeader(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent<FullHttpResponse> event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Masla start init response header for push request {} status {}", requestContext.getRequestUrl(), event.getState());
        }
    }



    @Override
    public void close() {

    }
}
