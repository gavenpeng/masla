package com.msw.masla.protocol.http.netty.session;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


/**
 * Created by Gavin.peng on 2017/10/20.
 * 对接入端的链接的抽象，一个链接一个session
 */
public interface IOSession {

    Logger LOG = LoggerFactory.getLogger(IOSession.class);


    Channel getChannel();

    /**
     * 写响应回客户端
     * @param response
     */
    void writeAndFlush(HttpResponse response);

    /**
     * 写响应回客户端,同时关闭链接
     * @param response
     */
    void writeAndClose(HttpResponse response);


    /**
     * 增加链接重用的次数
     */
    void addKeepaliveRequest();

    /**
     * 是否还可以keepalive，不可以的话，需要关闭链接
     * @return
     */
    boolean keepAlive();

    //是网关自己主动关闭session,会清空请求request，保证及时回收
    void close(SessionListener listener);

    //客户端主动关闭，网关被动关闭,不会清空request，因为有网关的线程会用到
    void error(SessionListener listener);

    boolean isActive();

    void setError();

    void writeError(HttpResponseStatus status, boolean close);

    void writeError(HttpResponseStatus status, String headerFlag, boolean close);

    long getActiveTime();

    long getCreateTime();

    HttpVersion getHttpVersion();

    public FullHttpRequest getHttpRequest();

    public String getContextRoot();

    public Long getLastBindAppId();

    public void bindAppId(long appId);

    public String getPath();

    public String getKey();

    public interface SessionListener{

        void close();
    }

    public Map<String,List<String>> getRequestParamsMap();


    public long getAppId();

    public void setAppId(long appId);


}
