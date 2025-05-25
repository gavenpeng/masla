package com.msw.masla.core.utils;

import com.msw.masla.common.constant.Constants;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.protocol.http.netty.util.BufferUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.*;

public class MaslaBackupResponseUitls {


    /**
     * 检查请求参数是否匹配设置参数
     *
     * @param customizedParamMap 自定义响应参数
     * @param requestParamMap 请求参数
     * @return 只要有不匹配的返回false，否则返回true
     */
    private static boolean isParamsMatched(Map<String, String> customizedParamMap,
                                           Map<String, String> requestParamMap) {

        //请求参数中包含所有设置参数
        //检查每个参数的值是否匹配
        for (String paramName : customizedParamMap.keySet()) {
            String requestParamValue = requestParamMap.get(paramName);
            //如果参数不存在，则不匹配
            if(requestParamValue == null){
                return false;
            }

            //参数存在，再比较值是否相等
            String[] customizedResponseParamValues = customizedParamMap
                    .get(paramName).split(",");
            if (!Arrays.asList(customizedResponseParamValues).contains(requestParamValue)) {
                return false;
            }
        }
        return true;
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

}
