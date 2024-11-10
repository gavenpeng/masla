package com.msw.masla.protocol.http.netty.compress;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.compression.ZlibWrapper;

/**
 * @Author: Gavin.peng
 * @Date: 2021/6/20 21:34
 */
public interface ZlibEncoder {

    public ByteBuf startEncode(ByteBuf content)throws Exception;

    public ByteBuf finishEncode(ByteBuf content);

    public ZlibWrapper getZlibWrapper();
}
