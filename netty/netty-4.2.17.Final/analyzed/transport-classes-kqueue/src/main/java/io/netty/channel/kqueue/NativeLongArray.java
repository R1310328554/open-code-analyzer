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
package io.netty.channel.kqueue;

import io.netty.channel.unix.Buffer;
import io.netty.util.internal.CleanableDirectBuffer;
import io.netty.util.internal.PlatformDependent;

import java.nio.ByteBuffer;

import static io.netty.channel.unix.Limits.SIZEOF_JLONG;
import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * 堆外 native-order {@code long[]}，供 JNI 批量传递 64 位值。
 * <p>容量不足时按策略扩容；支持 Unsafe 与 ByteBuffer 两种写入路径。</p>
 */
final class NativeLongArray {
    private CleanableDirectBuffer memoryCleanable;
    private ByteBuffer memory;
    private long memoryAddress;
    /** 当前可容纳的 long 个数 */
    private int capacity;
    /** 已写入元素个数 */
    private int size;

    NativeLongArray(int capacity) {
        this.capacity = checkPositive(capacity, "capacity");
        memoryCleanable = Buffer.allocateDirectBufferWithNativeOrder(calculateBufferCapacity(capacity));
        memory = memoryCleanable.buffer();
        memoryAddress = Buffer.memoryAddress(memory);
    }

    private static int idx(int index) {
        return index * SIZEOF_JLONG;
    }

    private static int calculateBufferCapacity(int capacity) {
        return capacity * SIZEOF_JLONG;
    }

    void add(long value) {
        reallocIfNeeded();
        if (PlatformDependent.hasUnsafe()) {
            PlatformDependent.putLong(memoryOffset(size), value);
        } else {
            memory.putLong(idx(size), value);
        }
        ++size;
    }

    void clear() {
        size = 0;
    }

    boolean isEmpty() {
        return size == 0;
    }

    int size() {
        return size;
    }

    /** 释放堆外内存；之后不可再访问 */
    void free() {
        memoryCleanable.clean();
        memoryAddress = 0;
    }

    /** 堆外数组起始地址，供 JNI 读取 */
    long memoryAddress() {
        return memoryAddress;
    }

    long memoryAddressEnd() {
        return memoryOffset(size);
    }

    private long memoryOffset(int index) {
        return memoryAddress + idx(index);
    }

    private void reallocIfNeeded() {
        if (size == capacity) {
            // 较小时容量翻倍，较大时增量 50%
            int newLength = capacity <= 65536 ? capacity << 1 : capacity + (capacity >> 1);
            int newCapacity = calculateBufferCapacity(newLength);
            CleanableDirectBuffer buffer = Buffer.allocateDirectBufferWithNativeOrder(newCapacity);
            // 拷贝旧内容；始终按 position=0 使用缓冲
            memory.position(0).limit(size);
            buffer.buffer().put(memory);
            buffer.buffer().position(0);

            memoryCleanable.clean();
            memoryCleanable = buffer;
            memory = buffer.buffer();
            memoryAddress = Buffer.memoryAddress(memory);
            capacity = newLength;
        }
    }

    @Override
    public String toString() {
        return "memoryAddress: " + memoryAddress + " capacity: " + capacity + " size: " + size;
    }
}
