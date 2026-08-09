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

package io.netty.buffer;

import java.util.List;

/**
 * {@link PoolArena} 的监控指标接口。
 * <p>
 * 暴露线程缓存数、Subpage/Chunk 列表、各尺寸档分配/释放计数及当前活跃字节数。
 */
public interface PoolArenaMetric extends SizeClassesMetric {

    /**
     * 返回绑定到本 Arena 的 {@link PoolThreadCache} 数量。
     */
    int numThreadCaches();

    /**
     * 返回 Arena 中 tiny 级 Subpage 数量。
     *
     * @deprecated Tiny 已与 Small 合并。
     */
    @Deprecated
    int numTinySubpages();

    /**
     * 返回 Arena 中 small 级 Subpage 池数量。
     */
    int numSmallSubpages();

    /**
     * 返回 Arena 中 {@link PoolChunkList} 链表档位数。
     */
    int numChunkLists();

    /**
     * 返回 tiny Subpage 的不可变 {@link PoolSubpageMetric} 列表。
     *
     * @deprecated Tiny 已与 Small 合并。
     */
    @Deprecated
    List<PoolSubpageMetric> tinySubpages();

    /**
     * 返回 small Subpage 的不可变 {@link PoolSubpageMetric} 列表。
     */
    List<PoolSubpageMetric> smallSubpages();

    /**
     * 返回各使用率档位 {@link PoolChunkListMetric} 的不可变列表。
     */
    List<PoolChunkListMetric> chunkLists();

    /**
     * 经本 Arena 完成的分配总次数（含所有尺寸档）。
     */
    long numAllocations();

    /**
     * Tiny 档分配次数。
     *
     * @deprecated Tiny 已与 Small 合并。
     */
    @Deprecated
    long numTinyAllocations();

    /**
     * Small 档（Subpage）分配次数。
     */
    long numSmallAllocations();

    /**
     * Normal 档（整 run）分配次数。
     */
    long numNormalAllocations();

    /**
     * Huge 档（超出 Chunk 规格）分配次数。
     */
    long numHugeAllocations();

    /**
     * 池化 Chunk 新建次数，未定义时返回 -1。
     */
    default long numChunkAllocations() {
        return -1;
    }

    /**
     * 经本 Arena 完成的释放总次数（含所有尺寸档）。
     */
    long numDeallocations();

    /**
     * Tiny 档释放次数。
     *
     * @deprecated Tiny 已与 Small 合并。
     */
    @Deprecated
    long numTinyDeallocations();

    /**
     * Small 档释放次数。
     */
    long numSmallDeallocations();

    /**
     * Normal 档释放次数。
     */
    long numNormalDeallocations();

    /**
     * Huge 档释放次数。
     */
    long numHugeDeallocations();

    /**
     * 池化 Chunk 销毁次数，未定义时返回 -1。
     */
    default long numChunkDeallocations() {
        return -1;
    }

    /**
     * 当前仍存活的分配总数（分配减释放）。
     */
    long numActiveAllocations();

    /**
     * 当前存活的 Tiny 分配数。
     *
     * @deprecated Tiny 已与 Small 合并。
     */
    @Deprecated
    long numActiveTinyAllocations();

    /**
     * 当前存活的 Small 分配数。
     */
    long numActiveSmallAllocations();

    /**
     * 当前存活的 Normal 分配数。
     */
    long numActiveNormalAllocations();

    /**
     * 当前存活的 Huge 分配数。
     */
    long numActiveHugeAllocations();

    /**
     * 当前存活的池化 Chunk 数，未定义时返回 -1。
     */
    default long numActiveChunks() {
        return -1;
    }

    /**
     * Arena 当前占用的活跃字节总数。
     */
    long numActiveBytes();
}
