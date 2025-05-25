/*
 * Copyright 2025 msw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.msw.masla.protocol.http.netty.event;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by Gavin.peng on 2017/6/5.
 */
public abstract class BaseEvent<T> implements IEvent<T> {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseEvent.class);

    protected long start;
    protected long startSendTime;
    protected long startEncodeTime;
    protected long startAcquireConnTime;
    protected long sendCompleteTime;
    protected long responseCompleteTime;
    protected long authStartTime;
    protected long authEndTime;
    protected T result;
    protected EventState state;
    protected Throwable remoteException;

    protected long MAX_REDO_THRESHOLD = 3;
    protected boolean retryable = true;
    protected boolean push = false;
    protected int execCount = 0;
    protected int maxRedoCount = 3;
    protected boolean invokeSso = false;
    protected boolean ssoCircuit = false;
    private int markTimeCount = 0;
    private long preMarkTime;
    private long submitTime;


    public BaseEvent(){
        this.start = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public EventState getState() {
        return state;
    }

    @Override
    public void setState(EventState state) {
        this.state = state;
    }


    public Throwable getErrorCause() {
        return remoteException;
    }

    @Override
    public void setRemoteException(Throwable remoteException) {
        this.remoteException = remoteException;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = TimeUnit.NANOSECONDS.toMillis(start);
    }

    @Override
    public boolean isRetryable() {
        return retryable && !isReachMaxRedoCount();
    }

    public int getMaxRedoCount(){
        return maxRedoCount;
    }

    protected boolean isReachMaxRedoCount() {
        return execCount >= maxRedoCount || execCount >= MAX_REDO_THRESHOLD;
    }

    @Override
    public void increaseExecCount() {
        execCount++;
    }

    @Override
    public int getExecCount() {
        return this.execCount;
    }


    public long getStartSendTime() {
        return startSendTime;
    }

    public void setStartSendTime(long startSendTime) {
        this.startSendTime = TimeUnit.NANOSECONDS.toMillis(startSendTime);
    }

    public long getStartAcquireConnTime() {
        return startAcquireConnTime;
    }

    public void setStartAcquireConnTime(long startAcquireConnTime) {
        this.startAcquireConnTime = TimeUnit.NANOSECONDS.toMillis(startAcquireConnTime);
    }

    public long getSendCompleteTime() {
        return sendCompleteTime;
    }

    public void setSendCompleteTime(long sendCompleteTime) {
        this.sendCompleteTime = TimeUnit.NANOSECONDS.toMillis(sendCompleteTime);
    }

    public void setSendCompleteTime(long sendCompleteTime, boolean nanos) {
        if(nanos) {
            this.sendCompleteTime = TimeUnit.NANOSECONDS.toMillis(sendCompleteTime);
        }else{
            this.sendCompleteTime = sendCompleteTime;
        }
    }

    public long getResponseCompleteTime() {
        return responseCompleteTime;
    }

    public void setResponseCompleteTime(long responseCompleteTime) {
        this.responseCompleteTime = TimeUnit.NANOSECONDS.toMillis(responseCompleteTime);
    }

    public long getStartEncodeTime() {
        return startEncodeTime;
    }

    public void setStartEncodeTime(long startEncodeTime) {
        this.startEncodeTime = TimeUnit.NANOSECONDS.toMillis(startEncodeTime);
    }

    public long getAuthStartTime() {
        return authStartTime;
    }

    public void setAuthStartTime(long authStartTime) {
        this.authStartTime = TimeUnit.NANOSECONDS.toMillis(authStartTime);
    }

    public long getAuthEndTime() {
        return authEndTime;
    }

    public void setAuthEndTime(long authEndTime) {
        this.authEndTime = TimeUnit.NANOSECONDS.toMillis(authEndTime);
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    public abstract void recycle();

    public abstract void reset();

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public boolean isInvokeSso() {
        return invokeSso;
    }

    public void setInvokeSso(boolean invokeSso) {
        this.invokeSso = invokeSso;
    }

    public boolean isSsoCircuit() {
        return ssoCircuit;
    }

    public void setSsoCircuit(boolean ssoCircuit) {
        this.ssoCircuit = ssoCircuit;
    }

    public long getPreMarkTime() {
        return preMarkTime;
    }

    public void setPreMarkTime(long preMarkTime) {
        this.preMarkTime = preMarkTime;
    }

    public int getMarkTimeCount() {
        return markTimeCount;
    }

    public void setMarkTimeCount(int markTimeCount) {
        this.markTimeCount = markTimeCount;
    }

    public long getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(long submitTime) {
        this.submitTime = submitTime;
    }
}
