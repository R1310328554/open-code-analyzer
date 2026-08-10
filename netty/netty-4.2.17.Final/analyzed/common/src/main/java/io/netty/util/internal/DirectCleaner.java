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
package io.netty.util.internal;

import java.nio.ByteBuffer;

/**
 * 基于 {@link PlatformDependent0} 直接分配/释放堆外内存的 {@link Cleaner} 实现。
 * <p>在 Unsafe 可用时使用 {@code allocateDirectNoCleaner} 与 {@code freeMemory}，支持高效 {@link #reallocate}。</p>
 */
final class DirectCleaner implements Cleaner {
    @Override
    public CleanableDirectBuffer allocate(int capacity) {
        return new CleanableDirectBufferImpl(capacity);
    }

    @Override
    public CleanableDirectBuffer reallocate(CleanableDirectBuffer old, int newCapacity) {
        int oldCapacity = old.buffer().capacity();
        int delta = newCapacity - oldCapacity;
        PlatformDependent.incrementMemoryCounter(delta);
        try {
            ByteBuffer newBuffer = PlatformDependent0.reallocateDirectNoCleaner(
                    old.buffer(), newCapacity);
            return new CleanableDirectBufferImpl(newBuffer);
        } catch (Throwable e) {
            PlatformDependent.decrementMemoryCounter(delta);
            throw e;
        }
    }

    @Override
    public void freeDirectBuffer(ByteBuffer buffer) {
        PlatformDependent0.freeMemory(PlatformDependent0.directBufferAddress(buffer));
    }

    @Override
    public boolean hasExpensiveClean() {
        return false;
    }

    /** {@link CleanableDirectBuffer} 包装，持有已分配或重分配后的堆外 {@link ByteBuffer}。 */
    private static final class CleanableDirectBufferImpl implements CleanableDirectBuffer {
        private final ByteBuffer buffer;

        // 常规分配：分配内存并递增全局计数器
        // Used for normal allocation — allocates memory and increments counter
        CleanableDirectBufferImpl(int capacity) {
            PlatformDependent.incrementMemoryCounter(capacity);
            try {
                this.buffer = PlatformDependent0.allocateDirectNoCleaner(capacity);
            } catch (Throwable e) {
                PlatformDependent.decrementMemoryCounter(capacity);
                throw e;
            }
        }

        // 重分配路径：内存已分配，计数器已在外部调整
        // Used for reallocation — memory already allocated, counter already adjusted
        CleanableDirectBufferImpl(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public ByteBuffer buffer() {
            return buffer;
        }

        @Override
        public void clean() {
            int capacity = buffer.capacity();
            PlatformDependent0.freeMemory(PlatformDependent0.directBufferAddress(buffer));
            PlatformDependent.decrementMemoryCounter(capacity);
        }
    }
}
