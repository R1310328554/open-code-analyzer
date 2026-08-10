/*
 * Copyright 2015 The Netty Project
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
package io.netty.channel.epoll;

import io.netty.channel.unix.Buffer;
import io.netty.util.internal.CleanableDirectBuffer;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.UnstableApi;

import java.nio.ByteBuffer;

/**
 * This is an internal datastructure which can be directly passed to epoll_wait to reduce the overhead.
 * <p>可直接传给 {@code epoll_wait} 的内部结构，减少 JNI 拷贝；{@code epoll_data} 的 {@code fd} 字段存
 * {@link AbstractEpollChannel} 文件描述符以便后续映射。</p>
 *
 * typedef union epoll_data {
 *     void        *ptr;
 *     int          fd;
 *     uint32_t     u32;
 *     uint64_t     u64;
 * } epoll_data_t;
 *
 * struct epoll_event {
 *     uint32_t     events;      // Epoll events
 *     epoll_data_t data;        // User data variable
 * };
 *
 * We use {@code fd} if the {@code epoll_data union} to store the actual file descriptor of an
 * {@link AbstractEpollChannel} and so be able to map it later.
 */
@UnstableApi
public final class EpollEventArray {
    // epoll_event 结构体字节大小
    private static final int EPOLL_EVENT_SIZE = Native.sizeofEpollEvent();
    // epoll_event 中 data 联合体的偏移量
    private static final int EPOLL_DATA_OFFSET = Native.offsetofEpollData();

    private CleanableDirectBuffer cleanable;
    private ByteBuffer memory;
    private long memoryAddress;
    private int length;

    EpollEventArray(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("length must be >= 1 but was " + length);
        }
        this.length = length;
        cleanable = Buffer.allocateDirectBufferWithNativeOrder(calculateBufferCapacity(length));
        memory = cleanable.buffer();
        memoryAddress = Buffer.memoryAddress(memory);
    }

    /**
     * Return the {@code memoryAddress} which points to the start of this {@link EpollEventArray}.
     * <p>返回指向本数组起始地址的原生内存指针，供 {@code epoll_wait} 使用。</p>
     */
    long memoryAddress() {
        return memoryAddress;
    }

    /**
     * Return the length of the {@link EpollEventArray} which represent the maximum number of {@code epoll_events}
     * that can be stored in it.
     * <p>数组容量，即单次 {@code epoll_wait} 最多可接收的事件数。</p>
     */
    int length() {
        return length;
    }

    /**
     * Increase the storage of this {@link EpollEventArray}.
     * <p>容量翻倍扩容；旧内容无需保留。</p>
     */
    void increase() {
        // 容量翻倍
        length <<= 1;
        // 无需保留旧内存内容
        CleanableDirectBuffer buffer = Buffer.allocateDirectBufferWithNativeOrder(calculateBufferCapacity(length));
        cleanable.clean();
        cleanable = buffer;
        memory = buffer.buffer();
        memoryAddress = Buffer.memoryAddress(buffer.buffer());
    }

    /**
     * Free this {@link EpollEventArray}. Any usage after calling this method may segfault the JVM!
     * <p>释放直接内存；调用后继续使用可能导致 JVM 段错误。</p>
     */
    void free() {
        cleanable.clean();
        memoryAddress = 0;
    }

    /**
     * Return the events for the {@code epoll_event} on this index.
     * <p>读取指定下标 {@code epoll_event} 的 events 掩码。</p>
     */
    int events(int index) {
        return getInt(index, 0);
    }

    /**
     * Return the file descriptor for the {@code epoll_event} on this index.
     * <p>读取指定下标事件关联的文件描述符。</p>
     */
    int fd(int index) {
        return getInt(index, EPOLL_DATA_OFFSET);
    }

    private int getInt(int index, int offset) {
        if (PlatformDependent.hasUnsafe()) {
            long n = (long) index * EPOLL_EVENT_SIZE;
            return PlatformDependent.getInt(memoryAddress + n + offset);
        }
        return memory.getInt(index * EPOLL_EVENT_SIZE + offset);
    }

    private static int calculateBufferCapacity(int capacity) {
        return capacity * EPOLL_EVENT_SIZE;
    }
}
