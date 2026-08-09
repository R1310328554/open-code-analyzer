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

import org.redisson.api.SortOrder;

/**
 * 聚合结果排序字段描述。
 * <p>
 * 供 {@link AggregationBaseOptions#sortBy(SortedField...)} 指定 SORTBY 子句的字段与顺序。
 *
 * @author Nikita Koksharov
 *
 */
public final class SortedField {

    private final String name;
    private final SortOrder order;

    /** 按字段名升序排序。 */
    public SortedField(String name) {
        this(name, SortOrder.ASC);
    }

    /**
     * 指定排序字段与顺序。
     *
     * @param name 字段名
     * @param order 排序方向
     */
    public SortedField(String name, SortOrder order) {
        this.name = name;
        this.order = order;
    }

    /** 返回排序字段名。 */
    public String getName() {
        return name;
    }

    /** 返回排序方向。 */
    public SortOrder getOrder() {
        return order;
    }
}
