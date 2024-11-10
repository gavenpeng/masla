package com.msw.masla.core.async.context;

import com.msw.masla.common.pojo.ParameterRewriteDO;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.common.enums.RequestDispatchMode;
import com.msw.masla.core.router.rule.RouteRule;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.protocol.http.netty.codec.MaslaChannelAttribute;
import com.msw.masla.core.utils.NettyCommonUtil;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by Gavin.peng on 2024/5/27.
 */
public class MaslaAsyncContext implements ChannelContext<IOSession ,HttpRequest , HttpResponse> {

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

    private String host;

    private String dc;//当前机房

    private String requestCopyHost;

    private ScheduledFuture<?> scheduledFuture;

    private long timeout;

    private BaseEvent event;

    private boolean doPush = true;

    private RequestDispatchMode requestDispatchMode = RequestDispatchMode.DEFAULT;

    private Map<String, String> headers;

    private Map<String, String> cookie;

    private Map<String, ParameterRewriteDO> rewriteRespParamMap;

    //当前上下文的请求参数
    private Map<String,List<String>> requstParamsMap;

    private int responseContentLength;

    private volatile boolean callbackFilter = false;


    private ServiceApp serviceApp;

    private boolean isStressRequest;

    private HttpResponse httpResponse;

    private RouteRule routeRule;

    private final static String SYMBOL_AND = "&";

    public MaslaAsyncContext() {}

    public MaslaAsyncContext(IOSession session, HttpRequest httpRequest, BaseEvent event){
        this.httpRequest = httpRequest;
        this.event = event;
        this.session = session;
        Attribute<Integer> channelAttributeLine = session.getChannel().attr(MaslaChannelAttribute.REQ_LINE_SIZE);
        if (channelAttributeLine != null && channelAttributeLine.get() != null) {
            this.requestLineLength = channelAttributeLine.get().intValue();
        }
        Attribute<Integer> channelAttributeHeader = session.getChannel().attr(MaslaChannelAttribute.REQ_HEADER_SIZE);
        if (channelAttributeHeader != null && channelAttributeHeader.get() != null) {
            this.requestHeaderLength = channelAttributeHeader.get().intValue();
        }
        this.requestBodyLength = ((FullHttpRequest)httpRequest).content().readableBytes();
    }


    public void initGrayContext(BaseEvent event){
        ((FullHttpRequest) httpRequest).content().resetReaderIndex();
        this.event = event;

        this.doPush = false;
        this.requestDispatchMode = RequestDispatchMode.REDIRECT;
        this.scheduledFuture = null;
        this.httpResponse = null;
        this.session = null;
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
            return (HttpResponse)event.getResult();
        }
        if(httpResponse == null){
            this.httpResponse = NettyCommonUtil.createResponse(HttpResponseStatus.OK,"");
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
        this.host = null;
        this.dc = null;
        this.requestCopyHost =null;
        this.requstParamsMap = null;
        this.serviceApp = null;

        this.rewritePath = null;
        this.requestPath = null;
        this.requestBodyLength = 0;
        this.requestLineLength = 0;
        this.requestHeaderLength = 0;
        this.cookie = null;
        this.headers = null;
        this.rewriteRespParamMap = null;
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
    public void setScheduledFuture(ScheduledFuture scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public BaseEvent getEvent() {
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

    public void reset(IOSession session, HttpRequest httpRequest, BaseEvent event){
        //this.channel = channel;
        this.session = session;
        this.httpRequest = httpRequest;
        this.event = event;
        Attribute<Integer> channelAttributeLine = session.getChannel().attr(MaslaChannelAttribute.REQ_LINE_SIZE);
        if (channelAttributeLine != null && channelAttributeLine.get() != null) {
            this.requestLineLength = channelAttributeLine.get().intValue();
        }
        Attribute<Integer> channelAttributeHeader = session.getChannel().attr(MaslaChannelAttribute.REQ_HEADER_SIZE);
        if (channelAttributeHeader != null && channelAttributeHeader.get() != null) {
            this.requestHeaderLength = channelAttributeHeader.get().intValue();
        }
        this.requestBodyLength = ((FullHttpRequest)httpRequest).content().readableBytes();
        //this.complete = false;
//        this.machineType = MachineType.NORMAL;
    }

    @Override
    public String getRequestHost() {
        return this.host;
    }

    @Override
    public void setRequestfHost(String host) {
        this.host = host;
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
     * @param httpRequest
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

    public void setEvent(BaseEvent event){
        this.event = event;
    }

    public void setAppDO(ServiceApp serviceApp) {
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
            Map<String, String> paramMap = new HashMap<String, String>(
                    requstParamsMap.size());
            for (String key : requstParamsMap.keySet()) {
                List<String> values = requstParamsMap.get(key);
                if (values != null && values.size() > 0) {
                    paramMap.put(key, values.get(0));
                }
            }
            return paramMap;
        }
        return null;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getCookie() {
        return cookie;
    }

    public void setCookie(Map<String, String> cookie) {
        this.cookie = cookie;
    }

    @Override
    public void setRequestCopyHost(String copyHost) {
        this.requestCopyHost = copyHost;
    }

    public int getResponseContentLength() {
        return responseContentLength;
    }

    public void setResponseContentLength(int responseContentLength) {
        this.responseContentLength = responseContentLength;
    }

    @Override
    public void fillCookies() {
        Map<String, String> cookies = NettyCommonUtil.getCookieMap(this.httpRequest);
        this.setCookie(cookies);
    }

    private static String decodeCookie(String cookieValue) {
        String result = null;
        try {
            if (cookieValue != null) {
                cookieValue = cookieValue.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                result = URLDecoder.decode(cookieValue, "UTF-8");
            }
        } catch (UnsupportedEncodingException neverHappen) {
            LOG.error("decode meet UnsupportedEncodingException, value={}", cookieValue);
        }
        return result;
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

    public RouteRule getRouteRule() {
        return routeRule;
    }

    public void setRouteRule(RouteRule routeRule) {
        this.routeRule = routeRule;
    }
}
