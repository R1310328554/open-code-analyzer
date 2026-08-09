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

/**
 * {@link PoolChunkList} 的监控指标接口。
 * <p>
 * 描述该档位 Chunk 的使用率上下界，以及可迭代的 {@link PoolChunkMetric}。
 */
public interface PoolChunkListMetric extends Iterable<PoolChunkMetric> {

    /**
     * 本档位 Chunk 使用率下限；低于此值时 Chunk 会降级到上一档链表。
     */
    int minUsage();

    /**
     * 本档位 Chunk 使用率上限；高于此值时 Chunk 会升级到下一档链表。
     */
    int maxUsage();
}
