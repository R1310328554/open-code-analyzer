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
 * 基于距离范围的向量相似度配置步骤。
 * <p>
 * 通过 epsilon 参数控制范围检索的精度。
 *
 * @author Nikita Koksharov
 */
public interface VectorSimilarityRange extends VectorSimilarity {

    /**
     * 设置范围搜索的 epsilon 精度控制参数。
     *
     * @param epsilon 精度控制参数
     * @return 向量相似度配置，可继续链式配置
     */
    VectorSimilarity epsilon(double epsilon);

}
