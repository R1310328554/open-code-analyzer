/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.buffer;

import io.netty.util.internal.PlatformDependent;

import java.nio.ByteBuffer;

/**
 * 包装已有绝对地址的直接内存为 {@link UnpooledUnsafeDirectByteBuf}，
 * 释放时调用 {@link PlatformDependent#freeMemory(long)} 而非释放 {@link ByteBuffer}。
 */
final class WrappedUnpooledUnsafeDirectByteBuf extends UnpooledUnsafeDirectByteBuf {

    /** 用 {@link PlatformDependent#directBuffer(long, int)} 构造 NIO 视图并绑定分配器。 */
    /** 用 {@link PlatformDependent#directBuffer(long, int)} 构造 NIO 视图并绑定分配器。 */
    WrappedUnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, long memoryAddress, int size, boolean doFree) {
        super(alloc, PlatformDependent.directBuffer(memoryAddress, size), size, doFree);
    }

    @Override
    /** 按内存地址释放，忽略传入的 buffer 对象。 */
    /** 按内存地址释放，忽略传入的 buffer 对象。 */
    protected void freeDirect(ByteBuffer buffer) {
        PlatformDependent.freeMemory(memoryAddress);
    }
}
