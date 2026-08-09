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
 * 单个 {@link PoolChunk} 的监控指标接口。
 */
public interface PoolChunkMetric {

    /**
     * 返回 Chunk 当前使用率百分比（0–100）。
     */
    int usage();

    /**
     * 返回 Chunk 总字节容量。
     */
    int chunkSize();

    /**
     * 返回 Chunk 中尚未分配的空闲字节数。
     */
    int freeBytes();
}
