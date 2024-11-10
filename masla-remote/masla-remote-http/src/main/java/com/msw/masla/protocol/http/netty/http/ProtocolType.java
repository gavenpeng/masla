package com.msw.masla.protocol.http.netty.http;

/**
 * Created by Gavin.peng on 2017/5/22.
 */
public enum ProtocolType {

    HTTP("HTTP", 1),  VARNISH("VARNISH", 3),RUBY("RUBY", 4);

    ProtocolType(String code, int value) {
        this.code = code;
        this.value = value;
    }

    private String code;
    private int value;

    public String getCode() {
        return code;
    }

    public int getValue() {
        return value;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
