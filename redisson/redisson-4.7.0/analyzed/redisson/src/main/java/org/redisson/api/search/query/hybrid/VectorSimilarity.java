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
 * 混合搜索中的向量相似度配置。
 * <p>
 * 支持基于距离范围的检索与 K 近邻（KNN）两种向量搜索模式。
 *
 * @author Nikita Koksharov
 */
public interface VectorSimilarity {

    static VectorSimilarityBasic of(String field, String param) {
        return new VectorSimilarityParams(field, param);
    }

    /**
     * 为向量相似度分数指定别名。
     *
     * @param value 分数别名
     * @return 当前实例，可继续链式配置
     */
    VectorSimilarity scoreAlias(String value);

    /**
     * 对向量搜索结果应用预过滤表达式。
     *
     * @param value 过滤表达式
     * @return 当前实例，可继续链式配置
     */
    VectorSimilarity filter(String value);

}
