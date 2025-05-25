package com.msw.masla.core.async.context;

import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.enums.RequestDispatchMode;
import com.msw.masla.core.router.rule.RouteRule;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.protocol.http.netty.codec.MaslaChannelAttribute;
import com.msw.masla.core.utils.MaslaHttpUtil;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static com.msw.masla.common.constant.Constants.MASLA_REQUEST_TAG_STRESS_HEADER;

/**
 * Created by gavin.peng on 2024/5/27.
 */

@Data
public class MaslaAsyncContext implements SessionContext<IOSession ,HttpRequest , HttpResponse> {

    protected static final Logger LOG = LoggerFactory.getLogger(MaslaAsyncContext.class);

    private HttpRequest httpRequest;

    private IOSession session;

    private int requestLineLength;

    private int requestHeaderLength;

    private int requestBodyLength;

    private String requestPath;

    //转发到后端服务的url
    private String rewritePath;

    /**
     * 请求url 的service 标识
     */
    private String serviceIdentify;

    /**
     * routeTag 路由标签
     */
    private String routeTag;

    /**
     * 路由到的host
     */
    private String routeHost;


    private ScheduledFuture<?> scheduledFuture;


    private long timeout;

    private BaseEvent<HttpResponse> event;

    private boolean doPush = true;

    private RequestDispatchMode requestDispatchMode = RequestDispatchMode.DEFAULT;

    private Map<String, String> headers;

    private Map<String, String> cookie;

    //当前上下文的请求参数
    private Map<String,List<String>> requstParamsMap;

    private int responseContentLength;

    private ServiceApp serviceApp;

    private HttpResponse httpResponse;

    private RouteRule routeRule;

    public MaslaAsyncContext(IOSession session, HttpRequest httpRequest, BaseEvent<HttpResponse> event){
        this.httpRequest = httpRequest;
        this.event = event;
        this.session = session;
        Attribute<Integer> channelAttributeLine = session.getChannel().attr(MaslaChannelAttribute.REQ_LINE_SIZE);
        if (channelAttributeLine != null && channelAttributeLine.get() != null) {
            this.requestLineLength = channelAttributeLine.get();
        }
        Attribute<Integer> channelAttributeHeader = session.getChannel().attr(MaslaChannelAttribute.REQ_HEADER_SIZE);
        if (channelAttributeHeader != null && channelAttributeHeader.get() != null) {
            this.requestHeaderLength = channelAttributeHeader.get();
        }
        this.requestBodyLength = ((FullHttpRequest)httpRequest).content().readableBytes();
    }


    @Override
    public RequestDispatchMode getRequestDispatchMode() {
        return this.requestDispatchMode;
    }

    @Override
    public void setRequestDispatchMode(RequestDispatchMode requestDispatchMode) {
        this.requestDispatchMode = requestDispatchMode;
    }

    @Override
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    @Override
    public HttpResponse getHttpResponse() {
        if(event != null && event.getResult() != null){
            return event.getResult();
        }
        if(httpResponse == null){
            this.httpResponse = MaslaHttpUtil.createResponse(HttpResponseStatus.OK,"");
        }
        return httpResponse;
    }

    @Override
    public void recycle() {

        //防止异步flush 回调事件没有做
        if(this.httpRequest != null) {
            releaseHttpRequest(this.httpRequest);
            this.httpRequest = null;
        }

        if(this.httpResponse != null) {
            releaseHttpResponse(this.httpResponse);
            this.httpResponse = null;
        }

        this.session = null;
        this.scheduledFuture = null;
        this.doPush = true;
        this.requestDispatchMode = RequestDispatchMode.DEFAULT;
        this.routeTag = null;
        this.requstParamsMap = null;
        this.serviceApp = null;

        this.rewritePath = null;
        this.requestPath = null;
        this.requestBodyLength = 0;
        this.requestLineLength = 0;
        this.requestHeaderLength = 0;
        this.cookie = null;
        this.headers = null;
        this.timeout = 0;
        this.event = null;
        MaslaRequestContextBuilder.recycleContext(this);
    }

    @Override
    public ServiceApp getService() {
        return this.serviceApp;
    }

    @Override
    public String getRequestUrl() {
        if(this.requestPath != null){
            return this.requestPath;
        }
        if(this.httpRequest != null)
            return this.httpRequest.uri();
        return null;
    }

    @Override
    public ScheduledFuture<?> getScheduledFuture() {
        return this.scheduledFuture;
    }

    @Override
    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public BaseEvent<HttpResponse> getEvent() {
        return event;
    }

    @Override
    public int getRequestLineSize() {
        return requestLineLength;
    }

    @Override
    public int getRequestHeaderSize() {
        return requestHeaderLength;
    }

    @Override
    public int getRequestBodySize() {
        return requestBodyLength;
    }


    @Override
    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public void reset(IOSession session, HttpRequest httpRequest, BaseEvent<HttpResponse> event){
        //this.channel = channel;
        this.session = session;
        this.httpRequest = httpRequest;
        this.event = event;
        Attribute<Integer> channelAttributeLine = session.getChannel().attr(MaslaChannelAttribute.REQ_LINE_SIZE);
        if (channelAttributeLine != null && channelAttributeLine.get() != null) {
            this.requestLineLength = channelAttributeLine.get();
        }
        Attribute<Integer> channelAttributeHeader = session.getChannel().attr(MaslaChannelAttribute.REQ_HEADER_SIZE);
        if (channelAttributeHeader != null && channelAttributeHeader.get() != null) {
            this.requestHeaderLength = channelAttributeHeader.get();
        }
        this.requestBodyLength = ((FullHttpRequest)httpRequest).content().readableBytes();
    }


    @Override
    public String getRouteTag() {
        return this.routeTag;
    }

    @Override
    public void setRouteTag(String routeTag) {
        this.routeTag = routeTag;
    }




    @Override
    public boolean isStressRequest() {

        return this.httpRequest.headers().contains(MASLA_REQUEST_TAG_STRESS_HEADER);

    }

    @Override
    public String toString() {
        return "MaslaAsyncContext{" +
                "httpRequest=" + httpRequest +
                ", session=" + session +
                ", scheduledFuture=" + scheduledFuture +
                ", timeout=" + timeout +
                ", event=" + event +
                ", requestDispatchMode=" + requestDispatchMode +
                ", serviceApp=" + serviceApp +
                ", httpResponse=" + httpResponse +
                '}';
    }

    /**
     * 没有发送的情况需要主动release
     * @param httpRequest http request
     */
    private void releaseHttpRequest(HttpRequest httpRequest){
        if(httpRequest != null){
            int refCnt = ((FullHttpRequest)httpRequest).refCnt();
            if(refCnt == 4){
                if(LOG.isInfoEnabled()) {
                    LOG.info("Masla found error release request {} is no write", httpRequest.uri());
                }
            }
            if(refCnt > 0){
                ReferenceCountUtil.release(httpRequest,refCnt);
            }

        }
    }

    private void releaseHttpResponse(HttpResponse httpResponse){
        if(httpResponse != null){
            int refCnt = ((FullHttpResponse)httpResponse).refCnt();
            if(refCnt > 0){
                ReferenceCountUtil.release(httpResponse,refCnt);
            }

        }
    }

    public void setEvent(BaseEvent<HttpResponse> event){
        this.event = event;
    }

    public void setService(ServiceApp serviceApp) {
        this.serviceApp = serviceApp;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public IOSession getSession() {
        return this.session;
    }

    @Override
    public boolean needPush() {
        return doPush;
    }

    @Override
    public Map<String, String> getParams() {
        //
        if(requstParamsMap == null){
            QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
            this.requstParamsMap = decoder.parameters();
        }
        if(requstParamsMap != null && !requstParamsMap.isEmpty()){
            Map<String, String> paramMap = new HashMap<>(
                    requstParamsMap.size());
            for (String key : requstParamsMap.keySet()) {
                List<String> values = requstParamsMap.get(key);
                if (values != null && !values.isEmpty()) {
                    paramMap.put(key, values.get(0));
                }
            }
            return paramMap;
        }
        return null;
    }


    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }



    public void setCookie(Map<String, String> cookie) {
        this.cookie = cookie;
    }




    public void setResponseContentLength(int responseContentLength) {
        this.responseContentLength = responseContentLength;
    }

    @Override
    public void fillCookies() {
        Map<String, String> cookies = MaslaHttpUtil.getCookieMap(this.httpRequest);
        this.setCookie(cookies);
    }

    @Override
    public void setRewritePath(String rewritePath) {
        this.rewritePath = rewritePath;
    }

    @Override
    public String getRewritePath() {
        return this.rewritePath;
    }


    @Override
    public void setServiceIdentify(String serviceIdentify) {
        this.serviceIdentify = serviceIdentify;
    }

    @Override
    public String getServiceIdentify() {
        return this.serviceIdentify;
    }

    public void setRouteRule(RouteRule routeRule) {
        this.routeRule = routeRule;
    }
}
