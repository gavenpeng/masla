package com.msw.masla.protocol.http.netty.factory;

/**
 * Created by Gavin.peng on 2017/7/14.
 */
public enum EventLoopGroupType {
    DEFAULT(0),FAST(1),SLOW(2),NEW(3);

    private int code;
    EventLoopGroupType(int code){
        this.code = code;
    }

    public int getCode(){
        return this.code;
    }

}
