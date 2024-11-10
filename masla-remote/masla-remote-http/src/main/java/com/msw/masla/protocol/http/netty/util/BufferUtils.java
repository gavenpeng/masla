package com.msw.masla.protocol.http.netty.util;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.internal.PlatformDependent;

/**
 * Author: Gavin.peng
 * Date: 2024/4/14
 * Description:
 */
public class BufferUtils {

    public static final PooledByteBufAllocator SERVER_POOL_ALLOCATOR = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());

}
