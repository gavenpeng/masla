package com.msw.masla.protocol.http.netty.http.handler;

import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

import java.io.IOException;

/**
 * Created by Gavin.peng on 2017/5/23.
 */
public class ContentHeaderHandler implements RequestHeaderHandler {

    private static final String LOG_IDENTITY = "handler.http.netty.com.msw.masla.ContentHeaderHandler.doHandle";

    @Override
    public void doHandle(HttpRequest request) throws IOException {
        if (request instanceof DefaultFullHttpRequest) {
            //request.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
            request.headers().remove(HttpHeaderNames.CONTENT_LENGTH);

            /*
              process transfer-encoding header,if not contain transfer-encoding
              then must set content-length;
             */

            ByteBuf content = ((DefaultFullHttpRequest) request).content();
            if(!request.headers().contains(HttpHeaderNames.TRANSFER_ENCODING)){
                int contentLength = content.readableBytes();
                if (content == null || contentLength <= 0) {
                    request.headers().add(HttpHeaderNames.CONTENT_LENGTH, "0");
                    return;
                }
                request.headers().add(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(contentLength));
            }

            //判断请求的大小是否超过了限制，超过了需要打日志报警
            int contentLength = content.readableBytes();
            if (contentLength > MaslaSpringContextUtil.getMaslaConfConfigBean().getRequestSizeLimit()) {
                String logDetail = "the request of the api:" + request.uri() + " exceed the size limit.the size is:" + contentLength;
//                ErrorlogSpanUtil.append(ServiceApp.getAppDO().getName(), request.uri(), logDetail, LOG_IDENTITY, logDetail);
                LOG.error("the request of the api:{} exceed the size limit.the size is:{}.", request.uri(), contentLength);
            }

        }
    }
}
