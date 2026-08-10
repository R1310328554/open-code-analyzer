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
import io.netty.util.internal.AdaptiveCalculator;

/**
 * {@link IoUringBufferRingAllocator} implementation which uses an adaptive strategy to allocate buffers, which
 * will decrease / increase the buffer size depending on if the allocated buffers were completely used or not before.
 * <p>自适应 buffer ring 分配器：根据读满与否动态增减下次 buffer 大小。</p>
 * <p>读满时通过 {@link AdaptiveCalculator} 快速放大，减少回 selector 等待的延迟。</p>
 */
public final class IoUringAdaptiveBufferRingAllocator extends AbstractIoUringBufferRingAllocator {

    /** 默认最小 buffer 大小（字节） */
    public static final int DEFAULT_MINIMUM = 1024;
    /** 默认初始 buffer 大小（字节） */
    public static final int DEFAULT_INITIAL = 4096;
    /** 默认最大 buffer 大小（字节） */
    public static final int DEFAULT_MAXIMUM = 65536;

    private final AdaptiveCalculator calculator;

    public IoUringAdaptiveBufferRingAllocator() {
        this(ByteBufAllocator.DEFAULT);
    }

    /**
     * Creates new instance.
     * <p>创建新实例。</p>
     *
     * @param allocator the {@link ByteBufAllocator} to use.
     */
    public IoUringAdaptiveBufferRingAllocator(ByteBufAllocator allocator) {
        this(allocator, DEFAULT_MINIMUM, DEFAULT_INITIAL, DEFAULT_MAXIMUM);
    }

    /**
     * Creates new instance.
     * <p>创建新实例。</p>
     *
     * @param allocator the {@link ByteBufAllocator} to use for the allocations
     * @param minimum   the inclusive lower bound of the expected buffer size
     * @param initial   the initial buffer size when no feed back was received
     * @param maximum   the inclusive upper bound of the expected buffer size
     */
    public IoUringAdaptiveBufferRingAllocator(ByteBufAllocator allocator, int minimum, int initial, int maximum) {
        this(allocator, minimum, initial, maximum, false);
    }

    /**
     * Creates new instance.
     * <p>创建新实例。</p>
     *
     * @param allocator         the {@link ByteBufAllocator} to use for the allocations
     * @param minimum           the inclusive lower bound of the expected buffer size
     * @param initial           the initial buffer size when no feed back was received
     * @param maximum           the inclusive upper bound of the expected buffer size
     * @param largeAllocation   {@code true} if we should do a large allocation for the whole buffer ring
     *                          and then slice out the buffers or {@code false} if we should do one allocation
     *                          per buffer.
     */
    public IoUringAdaptiveBufferRingAllocator(
            ByteBufAllocator allocator, int minimum, int initial, int maximum, boolean largeAllocation) {
        super(allocator, largeAllocation);
        this.calculator = new AdaptiveCalculator(minimum, initial, maximum);
    }

    @Override
    protected int nextBufferSize() {
        return calculator.nextSize();
    }

    @Override
    public void lastBytesRead(int attempted, int actual) {
        // 读满时放大下次猜测；大数据传输时避免频繁回 selector 检查
        if (attempted == actual) {
            calculator.record(actual);
        }
    }
}
