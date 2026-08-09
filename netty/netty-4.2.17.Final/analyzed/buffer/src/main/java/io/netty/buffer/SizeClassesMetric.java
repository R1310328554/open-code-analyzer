/*
 * Copyright 2020 The Netty Project
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
 * 暴露 {@link SizeClasses} 的尺寸分类查询与归一化能力。
 */
public interface SizeClassesMetric {

    /**
     * 按 sizeIdx 从查表结果返回字节大小。
     *
     * @return size
     */
    int sizeIdx2size(int sizeIdx);

    /**
     * 按 sizeIdx 公式计算字节大小（不查表）。
     *
     * @return size
     */
    int sizeIdx2sizeCompute(int sizeIdx);

    /**
     * 按 pageIdx 从查表返回页大小（pageSize 的整数倍）。
     *
     * @return size which is multiples of pageSize.
     */
    long pageIdx2size(int pageIdx);

    /**
     * 按 pageIdx 公式计算页大小（不查表）。
     *
     * @return size which is multiples of pageSize
     */
    long pageIdx2sizeCompute(int pageIdx);

    /**
     * 将请求大小向上归一化到最近 size class，返回 sizeIdx。
     *
     * @param size request size
     *
     * @return sizeIdx of the size class
     */
    int size2SizeIdx(int size);

    /**
     * 将页数向上归一化到最近 pageSize class，返回 pageIdx。
     *
     * @param pages multiples of pageSizes
     *
     * @return pageIdx of the pageSize class
     */
    int pages2pageIdx(int pages);

    /**
     * 将页数向下归一化到最近 pageSize class，返回 pageIdx。
     *
     * @param pages multiples of pageSizes
     *
     * @return pageIdx of the pageSize class
     */
    int pages2pageIdxFloor(int pages);

    /**
     * 考虑对齐后，将请求大小归一化为实际可分配的字节大小。
     *
     * @param size request size
     *
     * @return normalized size
     */
    int normalizeSize(int size);
}
