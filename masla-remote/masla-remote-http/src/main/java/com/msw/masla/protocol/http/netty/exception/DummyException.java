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
