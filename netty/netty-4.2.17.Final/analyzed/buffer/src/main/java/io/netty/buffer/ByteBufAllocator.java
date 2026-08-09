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

/**
 * 缓冲区分配器接口：实现类负责分配 {@link ByteBuf}，且必须是线程安全的。
 */
public interface ByteBufAllocator {

    ByteBufAllocator DEFAULT = ByteBufUtil.DEFAULT_ALLOCATOR;

    /**
     * 分配 {@link ByteBuf}；堆或直接内存由实现决定。
     */
    ByteBuf buffer();

    /**
     * 按给定初始容量分配 {@link ByteBuf}。
     */
    ByteBuf buffer(int initialCapacity);

    /**
     * 按初始容量与最大容量分配 {@link ByteBuf}。
     */
    ByteBuf buffer(int initialCapacity, int maxCapacity);

    /**
     * 分配 {@link ByteBuf}，优先直接内存以适配 I/O。
     */
    ByteBuf ioBuffer();

    /**
      * 分配 {@link ByteBuf}，优先使用适合 I/O 的直接缓冲区。
     */
    ByteBuf ioBuffer(int initialCapacity);

    /**
      * 分配 {@link ByteBuf}，优先使用适合 I/O 的直接缓冲区。
     */
    ByteBuf ioBuffer(int initialCapacity, int maxCapacity);

    /**
     * 分配堆 {@link ByteBuf}。
     */
    ByteBuf heapBuffer();

    /**
     * 按初始容量分配堆 {@link ByteBuf}。
     */
    ByteBuf heapBuffer(int initialCapacity);

    /**
     * 按初始与最大容量分配堆 {@link ByteBuf}。
     */
    ByteBuf heapBuffer(int initialCapacity, int maxCapacity);

    /**
     * 分配直接 {@link ByteBuf}。
     */
    ByteBuf directBuffer();

    /**
     * 按初始容量分配直接 {@link ByteBuf}。
     */
    ByteBuf directBuffer(int initialCapacity);

    /**
     * 按初始与最大容量分配直接 {@link ByteBuf}。
     */
    ByteBuf directBuffer(int initialCapacity, int maxCapacity);

    /**
     * 分配 {@link CompositeByteBuf}。
     */
    CompositeByteBuf compositeBuffer();

    /**
     * 分配指定最大组件数的 {@link CompositeByteBuf}。
     */
    CompositeByteBuf compositeBuffer(int maxNumComponents);

    /**
     * 分配堆 {@link CompositeByteBuf}。
     */
    CompositeByteBuf compositeHeapBuffer();

    /**
     * 分配指定最大组件数的堆 {@link CompositeByteBuf}。
     */
    CompositeByteBuf compositeHeapBuffer(int maxNumComponents);

    /**
     * 分配直接 {@link CompositeByteBuf}。
     */
    CompositeByteBuf compositeDirectBuffer();

    /**
     * 分配指定最大组件数的直接 {@link CompositeByteBuf}。
     */
    CompositeByteBuf compositeDirectBuffer(int maxNumComponents);

    /**
     * 若直接 {@link ByteBuf} 使用池化则返回 {@code true}。
     */
    boolean isDirectBufferPooled();

    /**
     * 计算扩容时的新容量：至少 {@code minNewCapacity}，且不超过 {@code maxCapacity}。
     */
    int calculateNewCapacity(int minNewCapacity, int maxCapacity);
 }
