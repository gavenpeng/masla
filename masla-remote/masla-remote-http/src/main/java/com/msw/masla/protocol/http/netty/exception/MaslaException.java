package com.msw.masla.protocol.http.netty.exception;

/**
 * Created by Gavin.peng on 2017/5/23.
 */
public class MaslaException extends RuntimeException {

    public MaslaException(String message){
        super(message);
    }

    public MaslaException(String message, Throwable cause){
        super(message,cause);
    }

    public MaslaException(Throwable cause){
        super(cause);
    }

}
