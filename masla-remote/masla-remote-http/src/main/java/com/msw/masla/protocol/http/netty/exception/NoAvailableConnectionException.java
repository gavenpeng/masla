package com.msw.masla.protocol.http.netty.exception;

/**
 * Created by Gavin.peng on 2017/5/22.
 */
public class NoAvailableConnectionException extends Exception {

    public NoAvailableConnectionException(String message){
        super(message);
    }

    public NoAvailableConnectionException(Throwable cause){
        super(cause);
    }

}
