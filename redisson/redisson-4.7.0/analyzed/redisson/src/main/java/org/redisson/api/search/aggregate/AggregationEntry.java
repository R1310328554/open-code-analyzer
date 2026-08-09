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

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RediSearch 聚合查询结果条目。
 * <p>
 * 表示单条聚合记录，包含匹配文档总数及属性键值映射。
 *
 * @author seakider
 *
 */
public class AggregationEntry {
    private final long total;
    private final Map<String, Object> attributes;

    /**
     * 构造聚合结果条目。
     *
     * @param total 匹配文档总数
     * @param attributes 属性键值映射
     */
    public AggregationEntry(long total, Map<String, Object> attributes) {
        this.total = total;
        this.attributes = attributes;
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
     * 返回属性键值映射。
     *
     * @return 属性映射
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
