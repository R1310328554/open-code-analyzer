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
 * 混合搜索融合方法的组合配置。
 * <p>
 * 支持倒数排名融合（RRF）与线性加权两种策略。
 *
 * @author Nikita Koksharov
 */
public interface Combine {

    /**
     * 创建倒数排名融合（RRF）组合配置。
     * <p>
     * RRF 按公式 score = 1 / (constant + rank) 融合文本检索与向量相似度排名。
     *
     * @return RRF 配置步骤
     */
    static CombineReciprocalRankFusionStep reciprocalRankFusion() {
        return new CombineRrfParams();
    }

    /**
     * 创建线性加权组合配置。
     * <p>
     * 线性组合公式：score = alpha * text_score + beta * vector_score。
     *
     * @return 线性配置步骤
     */
    static CombineLinearStep linear() {
        return new CombineLinearParams();
    }

}