/*
 * Copyright 2025 The Netty Project
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
package io.netty.channel.uring;

import io.netty.buffer.ByteBufAllocator;
import io.netty.util.internal.ObjectUtil;


/**
 * {@link IoUringBufferRingAllocator} implementation which uses a fixed size for the buffers that are returned by
 * {@link #allocate()}.
 * <p>固定 buffer 大小的 {@link IoUringBufferRingAllocator}；每次 {@link #allocate()} 返回相同容量。</p>
 */
public final class IoUringFixedBufferRingAllocator extends AbstractIoUringBufferRingAllocator {
    /** 环中每个 buffer 的固定字节容量 */
    private final int bufferSize;

    /**
     * Create a new instance
     *
     * @param allocator         the {@link ByteBufAllocator} to use.
     * @param largeAllocation   {@code true} if we should do a large allocation for the whole buffer ring
     *                          and then slice out the buffers or {@code false} if we should do one allocation
     *                          per buffer.
     * @param bufferSize        the size of the buffers that are allocated.
     * <p>指定分配器、大块/逐块分配策略与固定 buffer 大小。</p>
     */
    public IoUringFixedBufferRingAllocator(ByteBufAllocator allocator, boolean largeAllocation, int bufferSize) {
        super(allocator, largeAllocation);
        this.bufferSize = ObjectUtil.checkPositive(bufferSize, "bufferSize");
    }

    /**
     * Create a new instance
     *
     * @param allocator     the {@link ByteBufAllocator} to use.
     * @param bufferSize    the size of the buffers that are allocated.
     * <p>使用默认逐 buffer 分配策略。</p>
     */
    public IoUringFixedBufferRingAllocator(ByteBufAllocator allocator, int bufferSize) {
        this(allocator, false, bufferSize);
    }

    /**
     * Create a new instance
     *
     * @param bufferSize    the size of the buffers that are allocated.
     * <p>使用 {@link ByteBufAllocator#DEFAULT} 与逐 buffer 分配。</p>
     */
    public IoUringFixedBufferRingAllocator(int bufferSize) {
        this(ByteBufAllocator.DEFAULT, bufferSize);
    }

    @Override
    /** 固定大小实现：始终返回 {@link #bufferSize} */
    protected int nextBufferSize() {
        return bufferSize;
    }
}
