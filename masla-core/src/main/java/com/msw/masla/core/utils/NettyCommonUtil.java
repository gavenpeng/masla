package com.msw.masla.core.utils;

import com.msw.masla.common.constant.MaslaError;
import com.msw.masla.common.util.CollectionUtil;
import com.msw.masla.common.util.StringBuilderHolder;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.common.constant.Constants;
import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.util.BufferUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Gavin.peng on 17/10/23.
 */

@Slf4j
public class NettyCommonUtil {

  private static final Logger LOG = LoggerFactory.getLogger(NettyCommonUtil.class);


  //链接达到限制的响应
  public static final HttpResponseStatus TOO_MANY_SESSIONS = new HttpResponseStatus(
          MaslaError.CIRCUIT_BREAKER_CODE, "Too many connection");

  //拒绝请求响应内容--502
  public static final HttpResponseStatus TOO_MANY_REQUESTS = new HttpResponseStatus(
      MaslaError.CIRCUIT_BREAKER_CODE, "Too many request");

  //熔断响应码--506
  public static final HttpResponseStatus CIRCUIT_REQUESTS = new HttpResponseStatus(
          506, "Circuit request");

  //流控响应码--507
  public static final HttpResponseStatus FLOW_CONTROL_REQUESTS = new HttpResponseStatus(
          507, "Flow control request");

  //熔断响应码--200
  public static final HttpResponseStatus CIRCUIT_REQUESTS_CODE_200 = new HttpResponseStatus(
          200, "Circuit request");

  //超时响应内容
  public static final HttpResponseStatus TIMEOUT_REQUESTS = new HttpResponseStatus(
          HttpResponseStatus.GATEWAY_TIMEOUT.code(), "Read timeout from server");

  //请求失败响应内容
  public static final HttpResponseStatus REQUEST_FAILED = new HttpResponseStatus(
          MaslaError.CIRCUIT_BREAKER_CODE, "Request Failed");


  public static String CLIENT_REAL_IP = "X-REAL-IP";
  private static String CLIENT_REAL_PORT = "X-REAL-PORT";

  private static int BODY_SAMPLE_LENGTH_LIMIT = 512 * 1024;

  //大body请求采样阀值3M，超过该值放弃
  private static int BIG_BODY_LENGTH_LIMIT = 3145728;

  /**
   * 获取IP，能够获得经过代理之后的真实IP
   */
  public static String getRemoteAddr(HttpRequest request, Channel channel) {
    String ip = request.headers().get("x-forwarded-for");
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.headers().get("Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.headers().get("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = channel.remoteAddress().toString();
    }

    return ip;
  }

  public static String getClientRealIp(HttpRequest request, Channel channel) {
    String ip = getRealIp(request.headers());

    if (!StringUtil.isEmptyString(ip)) {
      return ip;
    }

    ip = request.headers().get(CLIENT_REAL_IP);
    if (StringUtil.isEmptyString(ip) && channel != null) {
      ip = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
    }
    return ip;
  }

  private static String getRealIp(HttpHeaders headers) {
    String realIp = null;

    String xForwardedFor = headers.get(Constants.X_FORWARDED_FOR);
    realIp = doGetRealIp(xForwardedFor);

   return realIp;
  }

  public static String doGetRealIp(String ips) {
    if (StringUtil.isEmptyString(ips)) {
      return null;
    }
    return ips.split(",")[0];
  }

  public static String getClientRealPort(HttpRequest request) {
    return request.headers().get(CLIENT_REAL_PORT);
  }

  public static HttpResponse createResponse(HttpResponseStatus httpResponseStatus,
      String responseContentStr) {

    return createResponse(httpResponseStatus,responseContentStr,Constants.MASLA_RESPONSE_HEADER_KEY_VALUE);

  }


  public static HttpResponse createResponse(HttpResponseStatus httpResponseStatus,
                                            String responseContentStr,String headerFlag) {
    //ByteBuf buf = ByteBufAllocator.DEFAULT.
    byte[] content = responseContentStr.getBytes(CharsetUtil.UTF_8);
    ByteBuf buf = BufferUtils.SERVER_POOL_ALLOCATOR.ioBuffer(content.length);
    buf.writeBytes(content);
    //ByteBuf buf = Unpooled.copiedBuffer(responseContentStr, CharsetUtil.UTF_8);
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            httpResponseStatus, buf);

    response.headers().add("content-type","text/html;charset=utf-8");
    response.headers().add(Constants.MASLA_RESPONSE_HEADER_KEY,headerFlag);

    return response;
  }



  public static HttpResponse createResponse(HttpResponseStatus httpResponseStatus) {
    //ByteBuf buf = Unpooled.copiedBuffer(responseContentStr, CharsetUtil.UTF_8);
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            httpResponseStatus);
    response.headers().add(Constants.MASLA_RESPONSE_HEADER_KEY,Constants.MASLA_RESPONSE_HEADER_KEY_VALUE);
    return response;
  }

  public static HttpResponse createResponse(HttpResponseStatus httpResponseStatus, byte[] content) {
    ByteBuf buf = BufferUtils.SERVER_POOL_ALLOCATOR.ioBuffer(content.length);
    buf.writeBytes(content);
    //ByteBuf buf = Unpooled.copiedBuffer(responseContentStr, CharsetUtil.UTF_8);
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
        httpResponseStatus, buf);
    response.headers().add(Constants.MASLA_RESPONSE_HEADER_KEY,Constants.MASLA_RESPONSE_HEADER_KEY_VALUE);

    return response;
  }


  public static HttpResponse createResponse(HttpResponseStatus httpResponseStatus, ByteBuffer buffer) {
    ByteBuf buf = BufferUtils.SERVER_POOL_ALLOCATOR.ioBuffer(buffer.limit());
    buf.writeBytes(buffer);
    //ByteBuf buf = Unpooled.copiedBuffer(responseContentStr, CharsetUtil.UTF_8);
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            httpResponseStatus, buf);
    response.headers().add(Constants.MASLA_RESPONSE_HEADER_KEY,Constants.MASLA_RESPONSE_HEADER_KEY_VALUE);

    return response;
  }

  public static String getHttpQueryParamValue(QueryStringDecoder decoder, String param) {
    List<String> values = decoder.parameters().get(param);
    if (CollectionUtil.isEmpty(values)) {
      return null;
    }
    return values.get(0);
  }

  public static String getHttpPostQueryParamValue(String content, String param) {
    String[] params = content.split("&");
    for (String kvs : params) {
      String[] kv = kvs.split("=");
      if (kv[0].equals(param)) {
        return kv[1];
      }
    }
    return null;

  }

  public static String getRequestPath(String uri) {
    QueryStringDecoder decoder = new QueryStringDecoder(uri);
    return decoder.path();
  }

  public static Map<String, String> getCookieMap(HttpRequest request) {
    Map<String, String> cookiesMap = new HashMap();
    /**
     * @see HttpHeaders#get(String) 只会获取第一个同名的header
     */
//    String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
    List<String> cookieStrings = request.headers().getAll(HttpHeaderNames.COOKIE);
    if (cookieStrings != null && !cookieStrings.isEmpty()) {
      for (String cookieString : cookieStrings) {
        if (cookieString != null) {
          Set<io.netty.handler.codec.http.cookie.Cookie> cookies = ServerCookieDecoder.STRICT
              .decode(cookieString);
          if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (io.netty.handler.codec.http.cookie.Cookie cookie : cookies) {
              cookiesMap.put(cookie.name(), cookie.value());
            }
          }
        }
      }
    }
    return cookiesMap;
  }


  /**
   * 获取请求参数，目前支持GET和POST方法
   *
   * @param request 请求对象
   * @return 包含请求参数的Map
   */
  public static Map<String, String> getRequestParamMap(HttpRequest request) throws IOException {

    Map<String, String> paramMap = null;
    if (request == null) {
      return paramMap;
    }

    paramMap = new HashMap<String, String>();

    QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
    Set<Map.Entry<String, List<String>>> params = decoder.parameters().entrySet();
    for (Map.Entry<String, List<String>> param : params) {
      paramMap.put(param.getKey(), param.getValue().get(0));
    }

    return paramMap;

  }

  public static void addCookie(StringBuilder sb, String name, String val) {
    sb.append(';');
    sb.append(HttpConstants.SP_CHAR);
    sb.append(name);
    sb.append('=');
    sb.append(val);
  }

  public static void resolvePostParams(String content, Map<String, String> paramMap) {
    String[] params = content.split("&");
    for (String kvs : params) {
      String[] kv = kvs.split("=");
      if (kv.length > 1) {
        paramMap.put(kv[0], kv[1]);
      }
    }

  }

  /**
   * 获取请求HEADERS
   *
   * @param request 请求对象
   * @return 包含Http Header的key/value Map
   */
  public static Map<String, String> getHeaderMap(HttpRequest request) {
    Map<String, String> headerMap = null;

    if (request == null) {
      return headerMap;
    }

    headerMap = new HashMap<String, String>();
    HttpHeaders headers = request.headers();
    List<Map.Entry<String, String>> headerEntryList = headers.entries();

    for (Map.Entry<String, String> entry : headerEntryList) {
      headerMap.put(entry.getKey(), entry.getValue());
    }
    return headerMap;
  }



  /**
   * 获取表示服务的uri
   *
   * @param context 请求上下文
   * @return 服务uri
   */
  public static String getServiceId(ChannelContext context) {


      StringBuilder builder = StringBuilderHolder.getGlobal();
      //如果serviceId和contextRoot一样则不再重复包含contextRoot
      if (!context.getService().getContextRoot()
          .equalsIgnoreCase(context.getServiceIdentify())) {
        builder.append(context.getService().getContextRoot());
      }

      builder.append(context.getServiceIdentify());
      return builder.toString();




  }


  /**
   * 检查是否为网关路径
   *
   * @param requestContext
   * @return
   */
  public static boolean isMaslaMetaPath(ChannelContext requestContext) {
    //ADMIN的请求有直接到对应的servlet处理
    String requestPath = requestContext.getRequestUrl();
    if (requestPath.equals(Constants.MASLA_META_CONTEXT)
        || requestPath.equals(Constants.MASLA_API_PATH)) {

      return true;
    }
    return false;
  }


  /**
   * 根据请求url获取context root
   * @param requestPath
   * @return
   */
  public static String getContextRootFromUrl(String requestPath){
    String contextRoot = requestPath;
    if(!StringUtil.isEmptyString(requestPath) && requestPath.length()>1) {
      String tmp = requestPath.substring(1);
      if (!StringUtil.isEmptyString(tmp)) {
        //有可能是ruby的服务
        int firstOffset = tmp.indexOf(Constants.HTTP_SCHEMA);
        if (firstOffset < 0) {
          contextRoot = Constants.HTTP_SCHEMA + tmp;
        } else {
          contextRoot = Constants.HTTP_SCHEMA + tmp.substring(0, firstOffset);
        }
      }
    }
    return contextRoot;
  }

  /**
   * 获取request body
   * @return byte[]
   * */
  public static byte[] getRequestBody(FullHttpRequest fullHttpRequest) {
    if (fullHttpRequest == null || fullHttpRequest.content().readableBytes() <= 0) {
      return new byte[0];
    } else {
      byte[] body = new byte[fullHttpRequest.content().readableBytes()];
      // readBytes比getBytes性能更高，会增加index, 需要重置
      fullHttpRequest.content().markReaderIndex();
      fullHttpRequest.content().readBytes(body, 0, body.length);
      fullHttpRequest.content().resetReaderIndex();
      return body;
    }
  }


}
