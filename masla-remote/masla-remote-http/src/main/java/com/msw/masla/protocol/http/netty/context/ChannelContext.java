package com.msw.masla.protocol.http.netty.context;

import com.msw.masla.common.pojo.ParameterRewriteDO;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.enums.RequestDispatchMode;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import io.netty.util.AttributeKey;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by Gavin.peng on 2017/6/15.
 */
public interface ChannelContext<S , T , R> {

    AttributeKey<ChannelContext> CONTEXT_KEY = AttributeKey.newInstance("channelContext");

    T getHttpRequest();

     R getHttpResponse();

     void recycle();

     S getSession();

     int getRequestLineSize();

     int getRequestHeaderSize();

     int getRequestBodySize();

     ServiceApp getService();

     void setRequestPath(String requestPath);

     void setRewritePath(String rewritePath);

    void setServiceIdentify(String serviceIdentify);

    String getServiceIdentify();

    BaseEvent getEvent();

    //请求path
     String getRequestUrl();

     String getRewritePath();

    String getRequestHost();

    void setRequestfHost(String host);

    void setRequestCopyHost(String copyHost);

    void fillCookies();

     long getTimeout();

     //请求相关的任务，有Timeout task
     void setScheduledFuture(ScheduledFuture scheduledFuture);

     //请求相关的任务，有Timeout task
     ScheduledFuture<?> getScheduledFuture();

    RequestDispatchMode getRequestDispatchMode();

    void setRequestDispatchMode(RequestDispatchMode requestDispatchMode);


    boolean needPush();

    Map<String,String> getParams();

    Map<String, String> getHeaders();

    Map<String, String> getCookie();

  public int getResponseContentLength();

  public void setResponseContentLength(int responseContentLength);

}
