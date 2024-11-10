package com.msw.masla.core.utils;

import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.*;

import java.util.*;

public class MaslaBackupResponseUitls {


    /**
     * 判断是否存在自定义响应
     * @param requestContext request context
     * @param serviceId request url path
     * @return http response
     */
    public static HttpResponse fillBackupResponse(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, String serviceId){

        return null;
    }


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

}
