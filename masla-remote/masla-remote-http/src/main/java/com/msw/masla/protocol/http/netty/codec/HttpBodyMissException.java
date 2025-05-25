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
package com.msw.masla.protocol.http.netty.codec;

import io.netty.handler.codec.DecoderException;

/**
 * @Author: Gavin.peng
 * @Date: 2019/11/26 15:28
 */
public class HttpBodyMissException extends DecoderException {

    /**
     * Creates a new instance.
     */
    public HttpBodyMissException() {
    }

    /**
     * Creates a new instance.
     */
    public HttpBodyMissException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public HttpBodyMissException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public HttpBodyMissException(Throwable cause) {
        super(cause);
    }



}
