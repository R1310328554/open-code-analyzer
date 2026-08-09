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
package org.redisson.api.search;

import java.util.Arrays;

/**
 * RediSearch 聚合查询的分组（GROUPBY）配置接口。
 * <p>
 * 指定分组字段及应用于各组的归约器（reducer）。
 *
 * @author Nikita Koksharov
 *
 */
public interface GroupBy {

    /**
     * 指定用于分组的字段名。
     *
     * @param names 字段名列表
     * @return 分组配置对象
     */
    static GroupBy fieldNames(String... names) {
        return new GroupParams(Arrays.asList(names));
    }

    /**
     * 指定应用于各分组的归约器。
     *
     * @param reducers 归约器对象
     * @return 分组配置对象
     */
    GroupBy reducers(Reducer... reducers);

}
