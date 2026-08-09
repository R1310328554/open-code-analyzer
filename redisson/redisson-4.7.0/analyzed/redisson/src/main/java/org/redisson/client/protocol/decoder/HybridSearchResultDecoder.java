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
package org.redisson.client.protocol.decoder;

import org.redisson.api.search.query.hybrid.HybridSearchResult;
import org.redisson.api.search.query.SearchResult;
import org.redisson.client.handler.State;

import java.util.*;

/**
 * 混合搜索（Hybrid Search）结果解码器。
 * <p>
 * 将 Redis 返回的多段数组解析为 {@link HybridSearchResult}：
 * 索引 1 为命中总数，索引 3 为文档字段映射列表（仅当 total &gt; 0 时有效）。
 * 空回复退化为 {@link SearchResult} 零命中结果。
 *
 * @author Nikita Koksharov
 *
 */
public class HybridSearchResultDecoder implements MultiDecoder<Object> {

    /** 解析混合搜索回复；空数组表示无命中。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return new SearchResult(0, Collections.emptyList());
        }

        Long total = (Long) parts.get(1);
        List<Map<String, String>> docs = Collections.emptyList();
        if (total > 0) {
            // 文档列表位于固定偏移 3
            docs = (List<Map<String, String>>) parts.get(3);
        }

        return new HybridSearchResult(total, docs);
    }

}
