package com.msw.masla.protocol.http.netty.exception;

/**
 * Created by Gavin.peng on 2018/07/31
 * DummyException 重写了Throwable的fillInStackTrace方法，直接返回this
 *
 * 不需要异常堆栈的情况下可以使用以提高性能,fillInStackTrace 方法回有native调用。
 *
 */
public class DummyException extends Exception {

    public DummyException(String msg){
        super(msg);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
