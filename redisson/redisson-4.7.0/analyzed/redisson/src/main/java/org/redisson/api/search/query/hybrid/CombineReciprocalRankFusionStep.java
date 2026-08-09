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
 * 倒数排名融合（RRF）配置步骤接口。
 *
 * @author Nikita Koksharov
 */
public interface CombineReciprocalRankFusionStep {

    /**
     * 设置 RRF 窗口大小，默认 20。
     *
     * @param window 窗口大小
     * @return 当前配置步骤
     */
    CombineReciprocalRankFusionFinalStep window(int window);

    /**
     * 设置 RRF 常数项，默认 60。
     *
     * @param constant 常数值
     * @return 当前配置步骤
     */
    CombineReciprocalRankFusionFinalStep constant(double constant);

}