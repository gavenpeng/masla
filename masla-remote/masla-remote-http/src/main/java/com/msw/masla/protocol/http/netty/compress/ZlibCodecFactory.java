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
package com.msw.masla.protocol.http.netty.compress;


import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * @Author: Gavin.peng
 * @Date: 2021/6/20 21:47
 */
public class ZlibCodecFactory {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ZlibCodecFactory.class);

    private static final int DEFAULT_JDK_WINDOW_SIZE = 15;
    private static final int DEFAULT_JDK_MEM_LEVEL = 8;

    private static final boolean noJdkZlibDecoder;
    private static final boolean noJdkZlibEncoder;
    private static final boolean supportsWindowSizeAndMemLevel;

    static {
        noJdkZlibDecoder = SystemPropertyUtil.getBoolean("io.netty.noJdkZlibDecoder",
                PlatformDependent.javaVersion() < 7);
        logger.debug("-Dio.netty.noJdkZlibDecoder: {}", noJdkZlibDecoder);

        noJdkZlibEncoder = SystemPropertyUtil.getBoolean("io.netty.noJdkZlibEncoder", false);
        logger.debug("-Dio.netty.noJdkZlibEncoder: {}", noJdkZlibEncoder);

        supportsWindowSizeAndMemLevel = noJdkZlibDecoder || PlatformDependent.javaVersion() >= 7;
    }

    /**
     * Returns {@code true} if specify a custom window size and mem level is supported.
     */
    public static boolean isSupportingWindowSizeAndMemLevel() {
        return supportsWindowSizeAndMemLevel;
    }


    public static ZlibEncoder newZlibEncoder(ZlibWrapper wrapper, int compressionLevel, int windowBits, int memLevel) {
        if (PlatformDependent.javaVersion() < 7 || noJdkZlibEncoder ||
                windowBits != DEFAULT_JDK_WINDOW_SIZE || memLevel != DEFAULT_JDK_MEM_LEVEL) {
//            return new JZlibEncoder(wrapper, compressionLevel, windowBits, memLevel);
            return new JdkZlibEncoder(wrapper, compressionLevel);
        } else {
            return new JdkZlibEncoder(wrapper, compressionLevel);
        }
    }



    private ZlibCodecFactory() {
        // Unused
    }
}
