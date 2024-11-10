package com.msw.masla.protocol.http.netty.codec;

import io.netty.util.AttributeKey;

/**
 * 记录上行和下行http 请求行，请求头的大小
 */
public class MaslaChannelAttribute {

    public static final AttributeKey<Integer> REQ_LINE_SIZE = AttributeKey.newInstance("reqLineSize");
    public static final AttributeKey<Integer> REQ_HEADER_SIZE = AttributeKey.newInstance("reqHeaderSize");

    public static final AttributeKey<Integer> RESP_LINE_SIZE = AttributeKey.newInstance("respLineSize");
    public static final AttributeKey<Integer> RESP_HEADER_SIZE = AttributeKey.newInstance("respHeaderSize");

}
