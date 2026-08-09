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
package org.redisson.api.search.aggregate;

import java.util.Arrays;

/**
 * 聚合分组（GROUP BY）配置入口。
 * <p>
 * 指定分组字段并挂载归约器，供 {@link AggregationBaseOptions#groupBy(GroupBy...)} 使用。
 *
 * @author Nikita Koksharov
 *
 */
public interface GroupBy {

    /**
     * 指定用于分组的字段名。
     *
     * @param names 分组字段名
     * @return 分组参数对象
     */
    static GroupBy fieldNames(String... names) {
        return new GroupParams(Arrays.asList(names));
    }

    /**
     * 为当前分组追加归约器。
     *
     * @param reducers 归约器实例
     * @return 当前分组配置
     */
    GroupBy reducers(Reducer... reducers);

}
