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
package org.redisson.api.search.index;

/**
 * SVS-VAMANA 向量索引的可选参数阶段接口。
 * <p>
 * 在设定向量类型、维度与距离度量后，可配置压缩算法、图构建窗口与搜索窗口等参数。
 *
 * @author seakider
 */
public interface SVSVamanaVectorOptionalArgs extends FieldIndex {

    /** 向量压缩算法枚举。 */
    enum CompressionAlgorithm {LVQ8, LVQ4, LVQ4x4, LVQ4x8, LeanVec4x8, LeanVec8x8}

    /**
     * 设置向量压缩算法。
     *
     * @param algorithm 压缩算法
     * @return 当前向量选项
     */
    SVSVamanaVectorOptionalArgs compression(CompressionAlgorithm algorithm);

    /**
     * 设置建图时的搜索窗口大小。
     *
     * @param value 搜索窗口大小
     * @return 当前向量选项
     */
    SVSVamanaVectorOptionalArgs constructionWindowSize(int value);

    /**
     * 设置每个节点的最大边数（图的最大度数）。
     *
     * @param value 最大边数
     * @return 当前向量选项
     */
    SVSVamanaVectorOptionalArgs graphMaxDegree(int value);

    /**
     * 设置查询时的搜索窗口大小。
     *
     * @param value 搜索窗口大小
     * @return 当前向量选项
     */
    SVSVamanaVectorOptionalArgs searchWindowSize(int value);

    /**
     * 设置范围查询的相对边界因子（epsilon）。
     *
     * @param value 相对边界因子
     * @return 当前向量选项
     */
    SVSVamanaVectorOptionalArgs epsilon(double value);

    /**
     * 设置学习压缩参数所需的向量数量阈值。
     *
     * @param value 向量数量阈值
     * @return 当前向量选项
     */
    SVSVamanaVectorOptionalArgs trainingThreshold(int value);

    /**
     * 设置 LeanVec4x8 或 LeanVec8x8 压缩时使用的向量维度。
     *
     * @param value 压缩向量维度
     * @return 当前向量选项
     */
    SVSVamanaVectorOptionalArgs leanVecDim(int value);

}
