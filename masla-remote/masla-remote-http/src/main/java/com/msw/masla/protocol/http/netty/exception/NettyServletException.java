package com.msw.masla.protocol.http.netty.exception;

/**
 * Created by gaoyue on 2017/10/17.
 */
public class NettyServletException extends Exception {

    public NettyServletException(String message){
        super(message);
    }

    public NettyServletException(Throwable cause){
        super(cause);
    }

    public NettyServletException(String message, Throwable cause){
        super(message, cause);
    }

}
