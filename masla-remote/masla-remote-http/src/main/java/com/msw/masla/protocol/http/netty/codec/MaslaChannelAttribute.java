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

import io.netty.util.AttributeKey;

/**
 * 记录上行和下行http 请求行，请求头的大小
 */
public class MaslaChannelAttribute {

    public static final AttributeKey<Integer> REQ_LINE_SIZE = AttributeKey.newInstance("reqLineSize");
    public static final AttributeKey<Integer> REQ_HEADER_SIZE = AttributeKey.newInstance("reqHeaderSize");

    public static final AttributeKey<Integer> RESP_LINE_SIZE = AttributeKey.newInstance("respLineSize");
    public static final AttributeKey<Integer> RESP_HEADER_SIZE = AttributeKey.newInstance("respHeaderSize");

}
