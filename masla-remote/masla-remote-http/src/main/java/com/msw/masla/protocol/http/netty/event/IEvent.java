package com.msw.masla.protocol.http.netty.event;


/**
 * Created by Gavin.peng on 2017/6/5.
 */
public interface IEvent<T> {


    T getResult();

    void setResult(T result);

    EventState getState();

    void setState(EventState state);

    void setRemoteException(Throwable errorMessage);

    boolean isRetryable();

    int getExecCount();

    void increaseExecCount();




}
