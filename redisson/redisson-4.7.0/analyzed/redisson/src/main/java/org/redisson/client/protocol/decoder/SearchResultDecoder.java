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

import org.redisson.api.search.query.Document;
import org.redisson.api.search.query.SearchResult;
import org.redisson.client.handler.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@code FT.SEARCH} 命令 RESP2 位置式响应解码器。
 * <p>
 * 输入格式为 {@code [total, doc_id, [attrs], doc_id, [attrs], ...]}，
 * 将每条文档 ID 与可选属性 Map 组装为 {@link Document} 列表。
 *
 * @author Nikita Koksharov
 *
 */
public class SearchResultDecoder implements MultiDecoder<Object> {

    /** 解析搜索结果：提取总数并逐条构造 {@link Document}。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return new SearchResult(0, Collections.emptyList());
        }

        Long total = (Long) parts.get(0);
        List<Document> docs = new ArrayList<>();
        if (total > 0) {
            for (int i = 1; i < parts.size(); i++) {
                String id = (String) parts.get(i);
                // 下一元素为 Map 时视为文档属性，否则仅保留 ID
                if ((i + 1) < parts.size() && parts.get(i + 1) instanceof Map) {
                    Map<String, Object> attrs = (Map<String, Object>) parts.get(++i);
                    docs.add(new Document(id, attrs));
                } else {
                    docs.add(new Document(id));
                }
            }
        }

        return new SearchResult(total, docs);
    }

}
