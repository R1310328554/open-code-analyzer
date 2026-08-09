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

import io.netty.util.internal.UnstableApi;

/**
 * 分配器内存块（chunk）的元信息。
 */
@UnstableApi
interface ChunkInfo {
    /**
      * 块容量，单位为字节。
     */
    int capacity();

    /**
      * 若块包含本地（堆外）内存则为 {@code true}，否则为 {@code false}。
     */
    boolean isDirect();

    /**
      * 块的本地内存地址；若无则为 0。
     */
    long memoryAddress();
}
