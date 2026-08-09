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
 * {@code TOPK.INFO} 命令返回的 Top-K 草图信息。
 *
 * @author Nikita Koksharov
 *
 */
public class TopKInfo {

    private final long topK;
    private final long width;
    private final long depth;
    private final double decay;

    public TopKInfo(long topK, long width, long depth, double decay) {
        this.topK = topK;
        this.width = width;
        this.depth = depth;
        this.decay = decay;
    }

    /**
     * 返回 Top-K 跟踪的最高频元素数量。
     *
     * @return 跟踪的最高频元素数（k）
     */
    public long getTopK() {
        return topK;
    }

    /**
     * 返回每个计数器数组的宽度（width）。
     *
     * @return 计数器数组宽度
     */
    public long getWidth() {
        return width;
    }

    /**
     * 返回计数器数组层数（depth）。
     *
     * @return 计数器数组层数
     */
    public long getDepth() {
        return depth;
    }

    /**
     * 返回计数器冲突时被衰减的概率。
     *
     * @return 计数器衰减概率
     */
    public double getDecay() {
        return decay;
    }
}
