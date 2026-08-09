/*
 * Copyright 2012 The Netty Project
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

import io.netty.util.internal.CleanableDirectBuffer;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.LongAdder;

/**
 * 简单的非池化 {@link ByteBufAllocator}：每次分配新内存，不做复用。
 */
public final class UnpooledByteBufAllocator extends AbstractByteBufAllocator implements ByteBufAllocatorMetricProvider {

    /** 堆/直接内存用量指标。 */
    private final UnpooledByteBufAllocatorMetric metric = new UnpooledByteBufAllocatorMetric();
    /** 是否完全禁用直接缓冲泄漏检测。 */
    private final boolean disableLeakDetector;
    /** 是否尝试无 Cleaner 的直接内存分配。 */
    private final boolean noCleaner;

    /** 默认实例：对直接缓冲启用泄漏检测。 */
    public static final UnpooledByteBufAllocator DEFAULT =
            new UnpooledByteBufAllocator(PlatformDependent.directBufferPreferred());

    /**
     * 创建实例；直接缓冲默认启用泄漏检测。
     *
     * @param preferDirect {@code true} 时 {@link #buffer(int)} 优先分配直接缓冲
     */
    public UnpooledByteBufAllocator(boolean preferDirect) {
        this(preferDirect, false);
    }

    /**
     * 创建实例，可禁用泄漏检测（依赖 GC 回收未 release 的直接内存）。
     *
     * @param preferDirect {@code true} 优先直接缓冲
     * @param disableLeakDetector {@code true} 完全禁用泄漏检测
     */
    public UnpooledByteBufAllocator(boolean preferDirect, boolean disableLeakDetector) {
        this(preferDirect, disableLeakDetector, PlatformDependent.useDirectBufferNoCleaner());
    }

    /**
     * 完整构造：偏好直接缓冲、泄漏检测开关、是否尝试无 Cleaner 分配。
     *
     * @param preferDirect {@code true} 优先直接缓冲
     * @param disableLeakDetector {@code true} 禁用泄漏检测
     * @param tryNoCleaner {@code true} 尝试 {@link PlatformDependent#allocateDirect(int)}
     */
    public UnpooledByteBufAllocator(boolean preferDirect, boolean disableLeakDetector, boolean tryNoCleaner) {
        super(preferDirect);
        this.disableLeakDetector = disableLeakDetector;
        noCleaner = tryNoCleaner && PlatformDependent.hasUnsafe()
                && PlatformDependent.hasDirectBufferNoCleanerConstructor();
    }

    @Override
    protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
        return PlatformDependent.hasUnsafe() ?
                new InstrumentedUnpooledUnsafeHeapByteBuf(this, initialCapacity, maxCapacity) :
                new InstrumentedUnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
    }

    @Override
    protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
        final ByteBuf buf;
        if (PlatformDependent.hasUnsafe()) {
            buf = noCleaner ? new InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf(this, initialCapacity, maxCapacity) :
                    new InstrumentedUnpooledUnsafeDirectByteBuf(this, initialCapacity, maxCapacity);
        } else {
            buf = new InstrumentedUnpooledDirectByteBuf(this, initialCapacity, maxCapacity);
        }
        return disableLeakDetector ? buf : toLeakAwareBuffer(buf);
    }

    @Override
    public CompositeByteBuf compositeHeapBuffer(int maxNumComponents) {
        CompositeByteBuf buf = new CompositeByteBuf(this, false, maxNumComponents);
        return disableLeakDetector ? buf : toLeakAwareBuffer(buf);
    }

    @Override
    public CompositeByteBuf compositeDirectBuffer(int maxNumComponents) {
        CompositeByteBuf buf = new CompositeByteBuf(this, true, maxNumComponents);
        return disableLeakDetector ? buf : toLeakAwareBuffer(buf);
    }

    @Override
    public boolean isDirectBufferPooled() {
        return false;
    }

    @Override
    public ByteBufAllocatorMetric metric() {
        return metric;
    }

    /** 直接内存用量 +amount（Instrumented 包装调用）。 */
    void incrementDirect(int amount) {
        metric.directCounter.add(amount);
    }

    /** 直接内存用量 -amount。 */
    void decrementDirect(int amount) {
        metric.directCounter.add(-amount);
    }

    /** 堆内存用量 +amount。 */
    void incrementHeap(int amount) {
        metric.heapCounter.add(amount);
    }

    /** 堆内存用量 -amount。 */
    void decrementHeap(int amount) {
        metric.heapCounter.add(-amount);
    }

    /** 带堆内存指标统计的 Unsafe 堆缓冲。 */
    private static final class InstrumentedUnpooledUnsafeHeapByteBuf extends UnpooledUnsafeHeapByteBuf {
        InstrumentedUnpooledUnsafeHeapByteBuf(UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
            super(alloc, initialCapacity, maxCapacity);
        }

        @Override
        protected byte[] allocateArray(int initialCapacity) {
            byte[] bytes = super.allocateArray(initialCapacity);
            ((UnpooledByteBufAllocator) alloc()).incrementHeap(bytes.length);
            return bytes;
        }

        @Override
        protected void freeArray(byte[] array) {
            int length = array.length;
            super.freeArray(array);
            ((UnpooledByteBufAllocator) alloc()).decrementHeap(length);
        }
    }

    /** 带堆内存指标统计的普通堆缓冲。 */
    private static final class InstrumentedUnpooledHeapByteBuf extends UnpooledHeapByteBuf {
        InstrumentedUnpooledHeapByteBuf(UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
            super(alloc, initialCapacity, maxCapacity);
        }

        @Override
        protected byte[] allocateArray(int initialCapacity) {
            byte[] bytes = super.allocateArray(initialCapacity);
            ((UnpooledByteBufAllocator) alloc()).incrementHeap(bytes.length);
            return bytes;
        }

        @Override
        protected void freeArray(byte[] array) {
            int length = array.length;
            super.freeArray(array);
            ((UnpooledByteBufAllocator) alloc()).decrementHeap(length);
        }
    }

    /** 无 Cleaner 直接缓冲 + 直接内存指标。 */
    private static final class InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf
            extends UnpooledUnsafeNoCleanerDirectByteBuf {
        InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf(
                UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
            super(alloc, initialCapacity, maxCapacity);
        }

        @Override
        protected CleanableDirectBuffer allocateDirectBuffer(int capacity) {
            CleanableDirectBuffer buffer = super.allocateDirectBuffer(capacity);
            return new DecrementingCleanableDirectBuffer(alloc(), buffer);
        }

        @Override
        protected CleanableDirectBuffer allocateDirectBuffer(int capacity, boolean permitExpensiveClean) {
            CleanableDirectBuffer buffer = super.allocateDirectBuffer(capacity, permitExpensiveClean);
            return new DecrementingCleanableDirectBuffer(alloc(), buffer);
        }

        @Override
        CleanableDirectBuffer reallocateDirect(CleanableDirectBuffer oldBuffer, int newCapacity) {
            int oldCapacity = oldBuffer.buffer().capacity();
            CleanableDirectBuffer buffer = super.reallocateDirect(oldBuffer, newCapacity);
            return new DecrementingCleanableDirectBuffer(
                    alloc(), buffer, buffer.buffer().capacity() - oldCapacity);
        }
    }

    /** Unsafe 直接缓冲 + 直接内存指标。 */
    private static final class InstrumentedUnpooledUnsafeDirectByteBuf extends UnpooledUnsafeDirectByteBuf {
        InstrumentedUnpooledUnsafeDirectByteBuf(
                UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
            super(alloc, initialCapacity, maxCapacity);
        }

        @Override
        protected CleanableDirectBuffer allocateDirectBuffer(int capacity) {
            CleanableDirectBuffer buffer = super.allocateDirectBuffer(capacity);
            return new DecrementingCleanableDirectBuffer(alloc(), buffer);
        }

        @Override
        protected CleanableDirectBuffer allocateDirectBuffer(int capacity, boolean permitExpensiveClean) {
            CleanableDirectBuffer buffer = super.allocateDirectBuffer(capacity, permitExpensiveClean);
            return new DecrementingCleanableDirectBuffer(alloc(), buffer);
        }

        @Override
        protected ByteBuffer allocateDirect(int initialCapacity) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void freeDirect(ByteBuffer buffer) {
            throw new UnsupportedOperationException();
        }
    }

    /** 安全 NIO 直接缓冲 + 直接内存指标。 */
    private static final class InstrumentedUnpooledDirectByteBuf extends UnpooledDirectByteBuf {
        InstrumentedUnpooledDirectByteBuf(
                UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
            super(alloc, initialCapacity, maxCapacity);
        }

        @Override
        protected CleanableDirectBuffer allocateDirectBuffer(int initialCapacity) {
            CleanableDirectBuffer buffer = super.allocateDirectBuffer(initialCapacity);
            return new DecrementingCleanableDirectBuffer(alloc(), buffer);
        }

        @Override
        protected CleanableDirectBuffer allocateDirectBuffer(int initialCapacity, boolean permitExpensiveClean) {
            CleanableDirectBuffer buffer = super.allocateDirectBuffer(initialCapacity, permitExpensiveClean);
            return new DecrementingCleanableDirectBuffer(alloc(), buffer);
        }

        @Override
        protected ByteBuffer allocateDirect(int initialCapacity) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void freeDirect(ByteBuffer buffer) {
            throw new UnsupportedOperationException();
        }
    }

    /** 包装 CleanableDirectBuffer，clean 时递减分配器直接内存计数。 */
    private static final class DecrementingCleanableDirectBuffer implements CleanableDirectBuffer {
        private final UnpooledByteBufAllocator alloc;
        private final CleanableDirectBuffer delegate;

        private DecrementingCleanableDirectBuffer(
                ByteBufAllocator alloc, CleanableDirectBuffer delegate) {
            this(alloc, delegate, delegate.buffer().capacity());
        }

        private DecrementingCleanableDirectBuffer(
                ByteBufAllocator alloc, CleanableDirectBuffer delegate, int capacityConsumed) {
            this.alloc = (UnpooledByteBufAllocator) alloc;
            this.alloc.incrementDirect(capacityConsumed);
            this.delegate = delegate;
        }

        @Override
        public ByteBuffer buffer() {
            return delegate.buffer();
        }

        @Override
        public void clean() {
            int capacity = delegate.buffer().capacity();
            delegate.clean();
            alloc.decrementDirect(capacity);
        }

        @Override
        public boolean hasMemoryAddress() {
            return delegate.hasMemoryAddress();
        }

        @Override
        public long memoryAddress() {
            return delegate.memoryAddress();
        }
    }

    /** 非池化分配器的堆/直接内存用量指标。 */
    private static final class UnpooledByteBufAllocatorMetric implements ByteBufAllocatorMetric {
        /** 当前直接内存字节数。 */
        final LongAdder directCounter = new LongAdder();
        /** 当前堆内存字节数。 */
        final LongAdder heapCounter = new LongAdder();

        @Override
        public long usedHeapMemory() {
            return heapCounter.sum();
        }

        @Override
        public long usedDirectMemory() {
            return directCounter.sum();
        }

        @Override
        public String toString() {
            return StringUtil.simpleClassName(this) +
                    "(usedHeapMemory: " + usedHeapMemory() + "; usedDirectMemory: " + usedDirectMemory() + ')';
        }
    }
}
