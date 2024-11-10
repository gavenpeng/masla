package com.msw.masla.core.push.processor.impl;

import com.msw.masla.protocol.http.netty.context.ChannelContext;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.core.utils.HttpContentCompressUtils;
import com.msw.masla.protocol.http.netty.compress.ZlibCodecFactory;
import com.msw.masla.protocol.http.netty.compress.ZlibEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT_ENCODING;

/**
 * @Author: Gavin.peng
 * @Date: 2021/6/20 20:47
 */
public class HttpContentCompressService {

    private static final Logger LOG = LoggerFactory.getLogger(HttpContentCompressService.class);

    static final String IDENTITY = HttpHeaderValues.IDENTITY.toString();
    private static final CharSequence ZERO_LENGTH_HEAD = "HEAD";
    private static final CharSequence ZERO_LENGTH_CONNECT = "CONNECT";
    private static final int CONTINUE_CODE = HttpResponseStatus.CONTINUE.code();
    private final int compressionLevel;
    private final int windowBits;
    private final int memLevel;
    private final int contentSizeThreshold;


    public HttpContentCompressService(int compressionLevel, int windowBits, int memLevel, int contentSizeThreshold){
        if (compressionLevel < 0 || compressionLevel > 9) {
            throw new IllegalArgumentException(
                    "compressionLevel: " + compressionLevel +
                            " (expected: 0-9)");
        }
        if (windowBits < 9 || windowBits > 15) {
            throw new IllegalArgumentException(
                    "windowBits: " + windowBits + " (expected: 9-15)");
        }
        if (memLevel < 1 || memLevel > 9) {
            throw new IllegalArgumentException(
                    "memLevel: " + memLevel + " (expected: 1-9)");
        }
        if (contentSizeThreshold < 0) {
            throw new IllegalArgumentException(
                    "contentSizeThreshold: " + contentSizeThreshold + " (expected: non negative number)");
        }
        this.compressionLevel = compressionLevel;
        this.windowBits = windowBits;
        this.memLevel = memLevel;
        this.contentSizeThreshold = contentSizeThreshold;
    }


    public FullHttpResponse compressContent(ChannelContext<IOSession, HttpRequest, HttpResponse> requestContext, FullHttpResponse httpResponse) throws Throwable {

        ByteBuf content = httpResponse.content();
        long contentSize = content.readableBytes();
        //检查长度是否大于压缩的阀值。
        if (this.contentSizeThreshold > 0) {
            if (contentSize < contentSizeThreshold) {
                return httpResponse;
            }
        }

        //检查acceptEncoding
        HttpRequest httpRequest = requestContext.getHttpRequest();
        CharSequence acceptEncoding = getAcceptEncoding(httpRequest);

//        FullHttpResponse httpResponse = event.getResult();
        String contentEncoding = httpResponse.headers().get(HttpHeaderNames.CONTENT_ENCODING);
        if (contentEncoding != null) {
            // Content-Encoding was set, either as something specific or as the IDENTITY encoding
            // Therefore, we should NOT encode here
            return httpResponse;
        }

        //
        if (isPassthru(httpResponse.protocolVersion(), httpResponse.status().code(), acceptEncoding)) {
            return httpResponse;
        }

        ZlibWrapper wrapper = HttpContentCompressUtils.determineWrapper(acceptEncoding.toString());
        if (wrapper == null) {
            return httpResponse;
        }

        String targetContentEncoding = null;
        switch (wrapper) {
            case GZIP:
                targetContentEncoding = "gzip";
                break;
            case ZLIB:
                targetContentEncoding = "deflate";
                break;
            default:
                break;
        }

        if(null == targetContentEncoding){
            return httpResponse;
        }

        httpResponse.headers().set(HttpHeaderNames.CONTENT_ENCODING, targetContentEncoding);


        //开始压缩header
        ZlibEncoder zlibEncoder = ZlibCodecFactory.newZlibEncoder(
                wrapper, compressionLevel, windowBits, memLevel);

        ByteBuf presssContent = zlibEncoder.startEncode(content);
        //压缩的尾部
        zlibEncoder.finishEncode(presssContent);
        //替换content，这里是新创建了一个response。
        FullHttpResponse newRes = httpResponse.replace(presssContent);
        //重新设置响应头content-length
        if (HttpUtil.isContentLengthSet(newRes)) {
            // adjust the content-length header
            HttpUtil.setContentLength(newRes, newRes.content().readableBytes());
        } else {
            newRes.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        }

        //增加引起次数，push后还可以引用
        requestContext.getEvent().setResult(newRes);
        //release content byte buffer
        ReferenceCountUtil.release(content);

        return newRes;

    }


    private CharSequence getAcceptEncoding(HttpRequest msg){
        CharSequence acceptEncoding;
        List<String> acceptEncodingHeaders = msg.headers().getAll(ACCEPT_ENCODING);
        switch (acceptEncodingHeaders.size()) {
            case 0:
                acceptEncoding = IDENTITY;
                break;
            case 1:
                acceptEncoding = acceptEncodingHeaders.get(0);
                break;
            default:
                // Multiple message-header fields https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
                acceptEncoding = StringUtil.join(",", acceptEncodingHeaders);
                break;
        }

        HttpMethod method = msg.method();
        if (HttpMethod.HEAD.equals(method)) {
            acceptEncoding = ZERO_LENGTH_HEAD;
        } else if (HttpMethod.CONNECT.equals(method)) {
            acceptEncoding = ZERO_LENGTH_CONNECT;
        }
        return acceptEncoding;
    }

    private boolean isPassthru(HttpVersion version, int code, CharSequence httpMethod) {
        return code < 200 || code == 204 || code == 304 ||
                (httpMethod == ZERO_LENGTH_HEAD || (httpMethod == ZERO_LENGTH_CONNECT && code == 200)) ||
                version == HttpVersion.HTTP_1_0;
    }


}
