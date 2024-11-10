package com.msw.masla.protocol.http.netty.exception;

/**
 * Created by Gavin.peng on 2017/5/22.
 */
public class NoAvailableHostException extends Exception {

    public NoAvailableHostException(String message){
        super(message);
    }

    public NoAvailableHostException(Throwable cause){
        super(cause);
    }

}
