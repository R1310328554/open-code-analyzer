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
 * 有序集合读操作（交集/并集）的可选参数。
 *
 * @author seakider
 *
 */

import org.redisson.api.RScoredSortedSet.Aggregate;

public interface SetReadArgs<T> {

    /**
     * 为每个 ScoredSortedSet 定义权重乘数。
     *
     * @param weights 权重乘数
     * @return 参数对象
     */
    T weights(Double... weights);

    /**
     * 定义分数聚合方式。
     *
     * @param aggregate 分数聚合模式
     * @return 参数对象
     */
    T aggregate(Aggregate aggregate);

}
