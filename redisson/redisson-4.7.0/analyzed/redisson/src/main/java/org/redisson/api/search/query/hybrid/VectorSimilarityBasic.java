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
 * 混合搜索向量相似度的基础配置接口。
 * <p>
 * 提供范围检索与 K 近邻（KNN）两种搜索模式的入口方法。
 *
 * @author Nikita Koksharov
 */
public interface VectorSimilarityBasic extends VectorSimilarity {

    /**
     * 创建基于距离范围的向量相似度搜索配置。
     *
     * @param radius 向量匹配的最大距离
     * @return 范围检索配置步骤
     */
    VectorSimilarityRange range(double radius);

    /**
     * 创建 K 近邻向量相似度搜索配置。
     *
     * @param k 要查找的最近邻数量
     * @return KNN 配置步骤
     */
    VectorSimilarityNearestNeighbors nearestNeighbors(int k);

}
