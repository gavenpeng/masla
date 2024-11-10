package com.msw.masla.protocol.http.netty.http.connection;

/**
 * Created by Gavin.peng on 2017/5/27.
 */
public enum ChannelState {
    EXCLUSIVE(0), UPGRADE(1),MULTIPLEX(2);
    private int code;

    ChannelState(int code) {
        this.setCode(code);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
