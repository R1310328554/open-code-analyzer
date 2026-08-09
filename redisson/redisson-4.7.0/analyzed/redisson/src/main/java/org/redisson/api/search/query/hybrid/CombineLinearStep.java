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
 * 线性组合配置步骤接口。
 *
 * @author Nikita Koksharov
 */
public interface CombineLinearStep extends Combine {

    /**
     * 设置文本检索得分的 alpha 权重。
     *
     * @param alpha alpha 权重值
     * @return 当前配置步骤
     */
    CombineLinearStep alpha(double alpha);

    /**
     * 设置向量相似度得分的 beta 权重。
     *
     * @param beta beta 权重值
     * @return 当前配置步骤
     */
    CombineLinearStep beta(double beta);

    /**
     * 设置线性组合的窗口大小。
     *
     * @param window 窗口大小
     * @return 当前配置步骤
     */
    CombineLinearStep window(int window);

    /**
     * Assigns an alias to the combined fusion score.
     *
     * @param alias score alias name
     * @return this step for further configuration
     */
//    CombineLinearStep scoreAlias(String alias);

}