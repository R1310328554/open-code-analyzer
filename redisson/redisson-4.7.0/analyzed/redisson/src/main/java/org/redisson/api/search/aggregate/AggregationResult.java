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

import java.util.List;
import java.util.Map;

/**
 * RediSearch 聚合查询结果。
 * <p>
 * 包含匹配文档总数、属性列表及可选的游标 ID，用于分页读取大量聚合数据。
 *
 * @author Nikita Koksharov
 *
 */
public final class AggregationResult {

    private final long total;
    private final List<Map<String, Object>> attributes;

    private long cursorId = -1;

    /**
     * 构造不含游标的聚合结果。
     *
     * @param total 匹配文档总数
     * @param attributes 属性列表
     */
    public AggregationResult(long total, List<Map<String, Object>> attributes) {
        this.total = total;
        this.attributes = attributes;
    }

    /**
     * 构造含游标 ID 的聚合结果。
     *
     * @param total 匹配文档总数
     * @param attributes 属性列表
     * @param cursorId 游标 ID，无游标时为 -1
     */
    public AggregationResult(long total, List<Map<String, Object>> attributes, long cursorId) {
        this.total = total;
        this.attributes = attributes;
        this.cursorId = cursorId;
    }

    /**
     * 返回游标 ID，用于后续分批读取。
     *
     * @return 游标 ID 值
     */
    public long getCursorId() {
        return cursorId;
    }

    /**
     * 返回匹配文档总数。
     *
     * @return 文档总数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 返回按属性名映射的结果列表。
     *
     * @return 属性列表
     */
    public List<Map<String, Object>> getAttributes() {
        return attributes;
    }
}
