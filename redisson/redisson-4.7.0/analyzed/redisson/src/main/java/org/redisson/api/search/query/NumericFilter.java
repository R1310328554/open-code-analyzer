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
package org.redisson.api.search.query;

/**
 * 用于 {@link QueryOptions#filters(QueryFilter...)} 的数值范围过滤器。
 *
 * @author Nikita Koksharov
 *
 */
public interface NumericFilter {

    /**
     * 设置数值范围下限（含边界）。
     *
     * @param value 下限值
     * @return 上限配置阶段
     */
    NumericFilterMax min(double value);

    /**
     * 设置数值范围下限（不含边界）。
     *
     * @param value 下限值
     * @return 上限配置阶段
     */
    NumericFilterMax minExclusive(double value);

}
