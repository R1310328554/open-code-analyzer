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

import java.util.List;

/**
 * {@link org.redisson.api.RSearch#search(String, String, QueryOptions)} 返回的搜索结果。
 *
 * @author Nikita Koksharov
 *
 */
public final class SearchResult {

    private long total;

    private List<Document> documents;

    public SearchResult(long total, List<Document> documents) {
        this.total = total;
        this.documents = documents;
    }

    /**
     * 返回匹配文档总数。
     *
     * @return 匹配总数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 返回命中的文档列表。
     *
     * @return 文档列表
     */
    public List<Document> getDocuments() {
        return documents;
    }
}
