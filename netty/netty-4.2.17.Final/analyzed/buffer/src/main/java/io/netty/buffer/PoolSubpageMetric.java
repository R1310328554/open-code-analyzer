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
 * {@link PoolSubpage} 的监控指标接口。
 */
public interface PoolSubpageMetric {

    /**
     * Subpage 可切分的最大元素个数。
     */
    int maxNumElements();

    /**
     * 当前仍可分配的元素个数。
     */
    int numAvailable();

    /**
     * 每个元素的字节大小。
     */
    int elementSize();

    /**
     * 本 Subpage 所属物理页大小（字节）。
     */
    int pageSize();
}

