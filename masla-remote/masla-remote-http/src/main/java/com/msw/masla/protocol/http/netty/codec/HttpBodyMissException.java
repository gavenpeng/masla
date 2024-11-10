package com.msw.masla.protocol.http.netty.codec;

import io.netty.handler.codec.DecoderException;

/**
 * @Author: Gavin.peng
 * @Date: 2019/11/26 15:28
 */
public class HttpBodyMissException extends DecoderException {

    /**
     * Creates a new instance.
     */
    public HttpBodyMissException() {
    }

    /**
     * Creates a new instance.
     */
    public HttpBodyMissException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public HttpBodyMissException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public HttpBodyMissException(Throwable cause) {
        super(cause);
    }



}
