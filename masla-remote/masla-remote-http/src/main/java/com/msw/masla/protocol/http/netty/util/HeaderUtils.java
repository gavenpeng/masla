package com.msw.masla.protocol.http.netty.util;

import com.msw.masla.common.util.StringUtil;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * authors: gavin.peng
 */
public class HeaderUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HeaderUtils.class);

    public static void setHeader(final HttpRequest request, String k, String v) {
        if (request == null || request.headers() == null || StringUtil.isEmptyString(k) || StringUtil.isEmptyString(v)) {
            return;
        }
        if (request.headers().contains(k) && LOG.isInfoEnabled()) {
            LOG.info("Masla found request {} header name {} exist", request.uri(), k);
        }
        request.headers().set(k, v);
    }



    /**
     * 处理请求path多个//开头的问题，去掉多余的，只留一个/
     * @param path
     * @return
     */
    public static String resloveRequestPath(String path){
        String newUrl = path;
        if(path.startsWith("//")){
//            newUrl = FIRST_REGEX.matcher(path).replaceFirst("");
            newUrl = path.substring(1);
            if(newUrl.startsWith("//")){
                return resloveRequestPath(newUrl);
            }
        }
        return newUrl;
    }

}
