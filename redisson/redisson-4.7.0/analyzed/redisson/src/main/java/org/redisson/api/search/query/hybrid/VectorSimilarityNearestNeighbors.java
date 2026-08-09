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
package org.redisson.api.search.query.hybrid;

/**
 * K 近邻（KNN）向量相似度配置步骤。
 * <p>
 * 用于调优 HNSW 索引搜索参数及分布式 KNN 的分片候选比例。
 *
 * @author Nikita Koksharov
 */
public interface VectorSimilarityNearestNeighbors extends VectorSimilarity {

    /**
     * 设置 HNSW 索引搜索的 EF_RUNTIME 参数。
     * 用于在搜索精度与速度之间权衡。
     *
     * @param efRuntime EF_RUNTIME 取值
     * @return 向量相似度配置，可继续链式配置
     */
    VectorSimilarity efRuntime(int efRuntime);

    /**
     * 设置结果集中距离字段的别名。
     *
     * @param field 距离字段别名
     * @return 向量相似度配置，可继续链式配置
     */
    VectorSimilarity yieldDistanceAs(String field);

    /**
     * 设置分布式 KNN 搜索的分片 K 比例参数。
     * 决定每个分片额外获取的候选数量。
     *
     * @param ratio 分片 K 比例值
     * @return 向量相似度配置，可继续链式配置
     */
    VectorSimilarity shardKRatio(double ratio);

}
