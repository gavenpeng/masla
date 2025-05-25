package com.msw.masla.protocol.http.netty.session;

import com.msw.masla.common.constant.Constants;
import com.msw.masla.common.util.StringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.util.ReferenceCountUtil;

import java.util.List;
import java.util.Map;


/**
 * Created by Gavin.peng on 2024/08/20.
 */
public class MaslaSession implements IOSession {

    private String key;
    private Channel channel;
    private FullHttpRequest httpRequest;
    private int keepaliveRequest = 0;
    private volatile boolean isError;
//    private volatile boolean dispatch;
    private long activeTime;//开始处理请求的时间
    private long createTime;//建链的时间
    private String contextRoot;

    private long appId;

    private Long lastBindAppId;

    private String path;

    private Map<String,List<String>> requstParamsMap;


    public MaslaSession(String sessionKey, Channel channel){
        this.key = sessionKey;
        this.channel = channel;
        this.createTime = System.currentTimeMillis();
    }


    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public void writeAndFlush(final HttpResponse response) {
        boolean isClose = false;
        if(this.channel instanceof Http2StreamChannel){
            isClose = true;
        }else{
            if(httpRequest != null) {
                String connection = this.httpRequest.headers().get(HttpHeaderNames.CONNECTION);
                if (!StringUtil.isEmptyString(connection) && connection.equalsIgnoreCase(HttpHeaderValues.CLOSE.toString())) {
                    isClose = true;
                }
            }else{
                LOG.warn("Masla found session {}{} channel {} http request is null",this.contextRoot,this.path,this.channel.remoteAddress());
                isClose = true;
            }
        }
        flush(response, isClose);
    }


    @Override
    public void writeAndClose(HttpResponse response) {
        flush(response,true);
    }


    public void flush(final HttpResponse response,final boolean isClose) {
        //Push thread do release so need retain one
        if(isClose) {
            this.isError = true;
        }

//        active = false;
        this.prepareResponse(response);
        //记录该session上次请求的app
        if(this.channel.isActive()) {
            this.httpRequest = null;
            //需要把requstParamsMap重制，因为session会处理多个请求，是重用的
            this.channel.writeAndFlush(response).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Masla write response to client {} success for request {}", channel.remoteAddress(),contextRoot + path);
                        }
                        if (isClose) {
                            future.channel().close();
                        }
                    } else {
                        LOG.error("Masla write request {} response to client {} failed:", contextRoot + path,channel.remoteAddress(), future.cause());
                        //write failed,shout close session
                        close(null);

                    }
                }
            });
        }else{
            ReferenceCountUtil.release(response);

           if(LOG.isInfoEnabled()) {
               LOG.info("Masla write request {} response but channel {} is closed", httpRequest == null ? this.contextRoot + this.path : httpRequest.uri(), channel.remoteAddress());
           }
            if(!channel.closeFuture().isDone()){
                LOG.warn("Masla found channel {} client is closed but masla is not closed",channel.remoteAddress());
                channel.close();
            }
            httpRequest = null;

        }



    }


    @Override
    public void addKeepaliveRequest() {
       this.keepaliveRequest++;
    }

    @Override
    public boolean keepAlive() {
        if(!isError){
//        if(!isError && this.keepaliveRequest < this.endpoint.getMaxKeepAliveRequests()){
            return true;
        }
        return false;
    }

    @Override
    public void close(final SessionListener listener) {
        closeChannel(listener);
        this.httpRequest = null;
    }

    @Override
    public void error(SessionListener listener) {
        this.isError = true;
        closeChannel(listener);
    }


    private void closeChannel(final SessionListener listener){
        if(this.channel != null && !this.channel.closeFuture().isDone()) {
            this.channel.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isDone()) {
                        LOG.info("Masla success close channel {}", channel.remoteAddress());
                    }
                    if (listener != null) {
                        listener.close();
                    }
                    //channel = null;
                }
            });
        }else{
            if(LOG.isInfoEnabled()) {
                LOG.info("Masla close channel {} but channel is already closed", channel.remoteAddress());
            }
        }
    }

    @Override
    public void setError() {
        this.isError = true;
    }

    @Override
    public HttpVersion getHttpVersion() {
        return this.httpRequest.protocolVersion();
    }

    public void setHttpRequest(FullHttpRequest request){
        this.httpRequest = request;
//        this.activeTime = System.currentTimeMillis();
    }

    private void prepareResponse(HttpResponse response){

        if(!response.headers().contains(HttpHeaderNames.CONTENT_TYPE)) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE,Constants.JSON_CONTENT_TYPE_VALUE);
        }

        if(!response.headers().contains(HttpHeaderNames.CONTENT_LENGTH)
            && !response.headers().contains(HttpHeaderNames.TRANSFER_ENCODING)){
            if(response instanceof FullHttpResponse) {
                int statusCode = response.status().code();
                if (statusCode < 200 || statusCode == 204 || statusCode == 205 ||
                        statusCode == 304) {
                    //以上情况 No entity body,客户端会忽略content-length。
                    if (statusCode == 205) {
                        // RFC 7231 requires the server to explicitly signal an empty
                        // response in this case
                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
                    }
                }else{
                    //其它响应码都需要content-length。
                    FullHttpResponse fullHttpResponse = (FullHttpResponse) response;
                    int contentLength = fullHttpResponse.content().readableBytes();
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
                }
            }
        }
        //connection header
        if(!this.keepAlive()){
            //set，避免出现多个
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }else {
            //http1.0 keep live
            if (this.httpRequest != null && this.httpRequest.protocolVersion() != null
                    && this.httpRequest.protocolVersion().compareTo(HttpVersion.HTTP_1_1) < 0) {
                response.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }else{
                //1.1 keep live
                response.headers().remove(HttpHeaderNames.CONNECTION);
            }
        }


        //add http dns and xdcs collector switch
        addXttendHeader(response);

        // Add server header
//        if (this.endpoint.getServer() != null) {
//            // Masla server always overrides anything the app might set
//            response.headers().add(HttpHeaderNames.SERVER,this.endpoint.getServer());
//        }
        //TODO
        //response.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);


    }


    private void addXttendHeader(HttpResponse response){



        setGlobalHeader(response);
        // 全局清理响应头
        deleteGlobalResponseHeader(response);

    }

    //全局响应头
    private void setGlobalHeader(HttpResponse response) {
        if (response == null || response.headers() == null) {
            return;
        }

    }

    //全局删除响应头
    private void deleteGlobalResponseHeader(HttpResponse response) {
        if (response == null || response.headers() == null) {
            return;
        }
    }


    @Override
    public void writeError(HttpResponseStatus httpResponseStatus,String headerFlag, final boolean close) {
        //this.prepareResponse();
        if (close) {
            this.isError = true;
        }
        DefaultHttpResponse errorResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1,httpResponseStatus);
        errorResponse.headers().set(Constants.MASLA_RESPONSE_HEADER_KEY,headerFlag);
        flush(errorResponse,true);
    }

    @Override
    public void writeError(HttpResponseStatus httpResponseStatus, final boolean close) {
        this.writeError(httpResponseStatus,Constants.MASLA_RESPONSE_HEADER_KEY_VALUE,close);
    }

    @Override
    public long getActiveTime() {
        return this.activeTime;
    }

    @Override
    public long getCreateTime() {
        return this.createTime;
    }

    public long getKeepAlive() {
        return this.keepaliveRequest;
    }


    @Override
    public boolean isActive() {
        return !isError;
    }

    @Override
    public FullHttpRequest getHttpRequest() {
        return this.httpRequest;
    }

    @Override
    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Long getLastBindAppId() {
        return this.lastBindAppId;
    }

    @Override
    public void bindAppId(long appId) {
        this.lastBindAppId = appId;
    }

    @Override
    public Map<String, List<String>> getRequestParamsMap() {
        if(requstParamsMap != null)
            return requstParamsMap;
        //参数为null，说明没有解析，需要解析
        QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
        this.requstParamsMap = decoder.parameters();
        return requstParamsMap;
    }

    public void setRequstParamsMap(Map<String, List<String>> requstParamsMap){
        this.requstParamsMap = requstParamsMap;
    }

    @Override
    public long getAppId() {
        return appId;
    }

    @Override
    public void setAppId(long appId) {
        this.appId = appId;
    }

}
