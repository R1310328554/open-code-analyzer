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

import java.util.List;
import java.util.Map;

/**
 * {@link org.redisson.api.RSearch#hybridSearch(String, HybridQueryArgs)} 方法返回的混合搜索结果对象。
 * <p>
 * 包含匹配总数与每条结果的字段映射列表。
 *
 * @author Nikita Koksharov
 *
 */
public final class HybridSearchResult {

    /** 匹配结果总数。 */
    private final long total;

    /** 结果列表，每项为字段名到值的映射。 */
    private final List<Map<String, String>> results;

    public HybridSearchResult(long total, List<Map<String, String>> scores) {
        this.total = total;
        this.results = scores;
    }

    /**
     * 返回匹配结果的总数。
     *
     * @return 结果总数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 返回结果数据列表。
     *
     * @return 字段映射列表
     */
    public List<Map<String, String>> getResults() {
        return results;
    }
}
