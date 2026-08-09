/*
 * Copyright 2017 The Netty Project
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

import io.netty.util.internal.StringUtil;

import java.util.List;

/**
 * {@link PooledByteBufAllocator} 对外暴露的运行指标。
 */
@SuppressWarnings("deprecation")
public final class PooledByteBufAllocatorMetric implements ByteBufAllocatorMetric {

    private final PooledByteBufAllocator allocator;

    PooledByteBufAllocatorMetric(PooledByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    /**
     * 返回堆 arena 数量。
     */
    public int numHeapArenas() {
        return allocator.numHeapArenas();
    }

    /**
     * 返回直接内存 arena 数量。
     */
    public int numDirectArenas() {
        return allocator.numDirectArenas();
    }

    /**
     * 返回此池提供的全部堆 {@link PoolArenaMetric} 列表。
     */
    public List<PoolArenaMetric> heapArenas() {
        return allocator.heapArenas();
    }

    /**
     * 返回此池提供的全部直接 {@link PoolArenaMetric} 列表。
     */
    public List<PoolArenaMetric> directArenas() {
        return allocator.directArenas();
    }

    /**
     * 返回 {@link PooledByteBufAllocator} 使用的线程本地缓存数量。
     */
    public int numThreadLocalCaches() {
        return allocator.numThreadLocalCaches();
    }

    /**
     * 返回 tiny 缓存大小。
     *
     * @deprecated Tiny 缓存已并入 small 缓存。
     */
    @Deprecated
    public int tinyCacheSize() {
        return allocator.tinyCacheSize();
    }

    /**
     * 返回 small 缓存大小。
     */
    public int smallCacheSize() {
        return allocator.smallCacheSize();
    }

    /**
     * 返回 normal 缓存大小。
     */
    public int normalCacheSize() {
        return allocator.normalCacheSize();
    }

    /**
     * 返回每个 arena 的 chunk 字节大小。
     */
    public int chunkSize() {
        return allocator.chunkSize();
    }

    @Override
    public long usedHeapMemory() {
        return allocator.usedHeapMemory();
    }

    @Override
    public long usedDirectMemory() {
        return allocator.usedDirectMemory();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append(StringUtil.simpleClassName(this))
                .append("(usedHeapMemory: ").append(usedHeapMemory())
                .append("; usedDirectMemory: ").append(usedDirectMemory())
                .append("; numHeapArenas: ").append(numHeapArenas())
                .append("; numDirectArenas: ").append(numDirectArenas())
                .append("; smallCacheSize: ").append(smallCacheSize())
                .append("; normalCacheSize: ").append(normalCacheSize())
                .append("; numThreadLocalCaches: ").append(numThreadLocalCaches())
                .append("; chunkSize: ").append(chunkSize()).append(')');
        return sb.toString();
    }
}
