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
 * 混合查询构建流程中的文本检索配置步骤。
 * <p>
 * 用于设置全文检索组件的打分算法、分数别名及向量相似度子查询。
 *
 * @author Nikita Koksharov
 */
public interface QueryStep {

    /**
     * 指定文本检索组件的打分算法。
     *
     * @param scorer 打分算法名称（例如 "BM25"）
     * @return 当前步骤，可继续链式配置
     */
    QueryStep scorer(String scorer);

    /**
     * 为检索分数指定别名，供后续融合或排序引用。
     *
     * @param alias 分数别名
     * @return 当前步骤，可继续链式配置
     */
    QueryStep scoreAlias(String alias);

    /**
     * 定义混合查询中的向量相似度组件。
     *
     * @param value 向量相似度配置
     * @return 参数配置步骤
     */
    ParamsStep vectorSimilarity(VectorSimilarity value);

}
