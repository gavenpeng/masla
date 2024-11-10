package com.msw.masla.protocol.http.netty.exception;

import java.io.IOException;

/**
 * Created by Gavin.peng on 2017/6/29.
 */
public class ServerClosedChannelException extends IOException {

    public ServerClosedChannelException(){
        super();
    }

    public ServerClosedChannelException(String msg){
        super(msg);
    }

    public ServerClosedChannelException(Throwable e){
        super(e);
    }
}
