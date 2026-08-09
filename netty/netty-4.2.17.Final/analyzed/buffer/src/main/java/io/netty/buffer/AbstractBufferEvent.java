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
 * 缓冲区生命周期 JFR 事件的抽象基类，承载容量、内存位置等通用字段。
 */
abstract class AbstractBufferEvent extends AbstractAllocatorEvent {
    @DataAmount
    /** 缓冲区当前配置的容量（{@link AbstractByteBuf#capacity()}） */
    @Description("Configured buffer capacity")
    public int size;
    @DataAmount
    /** 实际分配的快速可写上限（writerIndex + maxFastWritableBytes） */
    @Description("Actual allocated buffer capacity")
    public int maxFastCapacity;
    @DataAmount
    /** 缓冲区允许的最大容量 */
    @Description("Maximum buffer capacity")
    public int maxCapacity;
    /** 是否为堆外（Direct）内存 */
    @Description("Is this buffer referencing off-heap memory?")
    public boolean direct;
    /** 堆外内存起始地址（若可用） */
    @Description("The memory address of the off-heap memory, if available")
    @MemoryAddress
    public long address;

    /** 从 {@link AbstractByteBuf} 实例填充事件字段，供 JFR commit 前调用 */
    public void fill(AbstractByteBuf buf, Class<? extends AbstractByteBufAllocator> allocatorType) {
        this.allocatorType = allocatorType;
        size = buf.capacity();
        maxFastCapacity = buf.maxFastWritableBytes() + buf.writerIndex();
        maxCapacity = buf.maxCapacity();
        direct = buf._isDirect();
        address = buf._memoryAddress();
    }
}
