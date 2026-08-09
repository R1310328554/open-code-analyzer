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
package io.netty.buffer;

import jdk.jfr.DataAmount;
import jdk.jfr.Description;
import jdk.jfr.MemoryAddress;

@SuppressWarnings("Since15")
/**
 * 内存块（Chunk）分配/释放 JFR 事件的抽象基类。
 */
abstract class AbstractChunkEvent extends AbstractAllocatorEvent {
    @DataAmount
    /** Chunk 的字节容量 */
    @Description("Size of the chunk")
    public int capacity;
    /** Chunk 是否位于堆外内存 */
    @Description("Is this chunk referencing off-heap memory?")
    public boolean direct;
    /** 堆外 Chunk 的起始地址（若可用） */
    @Description("The memory address of the off-heap memory, if available")
    @MemoryAddress
    public long address;

    /** 从 {@link ChunkInfo} 填充事件字段 */
    public void fill(ChunkInfo chunk, Class<? extends AbstractByteBufAllocator> allocatorType) {
        this.allocatorType = allocatorType;
        capacity = chunk.capacity();
        direct = chunk.isDirect();
        address = chunk.memoryAddress();
    }
}
