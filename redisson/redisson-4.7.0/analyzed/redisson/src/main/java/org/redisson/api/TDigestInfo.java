/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api;

/**
 * {@code TDIGEST.INFO} 命令返回的 t-digest 信息。
 *
 * @author Nikita Koksharov
 *
 */
public class TDigestInfo {

    private final long compression;
    private final long capacity;
    private final long mergedNodes;
    private final long unmergedNodes;
    private final double mergedWeight;
    private final double unmergedWeight;
    private final long observations;
    private final long totalCompressions;
    private final long memoryUsage;

    public TDigestInfo(long compression, long capacity,
                       long mergedNodes, long unmergedNodes,
                       double mergedWeight, double unmergedWeight,
                       long observations, long totalCompressions,
                       long memoryUsage) {
        this.compression = compression;
        this.capacity = capacity;
        this.mergedNodes = mergedNodes;
        this.unmergedNodes = unmergedNodes;
        this.mergedWeight = mergedWeight;
        this.unmergedWeight = unmergedWeight;
        this.observations = observations;
        this.totalCompressions = totalCompressions;
        this.memoryUsage = memoryUsage;
    }

    /**
     * 返回草图的压缩因子。
     *
     * @return 压缩因子
     */
    public long getCompression() {
        return compression;
    }

    /**
     * 返回草图可容纳的质心数量。
     *
     * @return 容量
     */
    public long getCapacity() {
        return capacity;
    }

    /**
     * 返回已合并的质心数量。
     *
     * @return 已合并质心数
     */
    public long getMergedNodes() {
        return mergedNodes;
    }

    /**
     * 返回尚未合并的缓冲质心数量。
     *
     * @return 未合并质心数
     */
    public long getUnmergedNodes() {
        return unmergedNodes;
    }

    /**
     * 返回已合并质心的总权重。
     *
     * @return 已合并权重
     */
    public double getMergedWeight() {
        return mergedWeight;
    }

    /**
     * 返回尚未合并缓冲质心的总权重。
     *
     * @return 未合并权重
     */
    public double getUnmergedWeight() {
        return unmergedWeight;
    }

    /**
     * 返回已添加到草图的观测值数量。
     *
     * @return 观测值数量
     */
    public long getObservations() {
        return observations;
    }

    /**
     * 返回草图执行的压缩（compaction）次数。
     *
     * @return 压缩次数
     */
    public long getTotalCompressions() {
        return totalCompressions;
    }

    /**
     * 返回草图占用的字节数。
     *
     * @return 内存占用（字节）
     */
    public long getMemoryUsage() {
        return memoryUsage;
    }
}