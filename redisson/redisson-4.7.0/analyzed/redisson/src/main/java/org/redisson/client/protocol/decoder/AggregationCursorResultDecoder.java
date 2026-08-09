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

import org.redisson.api.search.aggregate.AggregationResult;
import org.redisson.client.handler.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@code FT.AGGREGATE WITHCURSOR} 游标聚合结果解码器（RESP2 格式）。
 * <p>
 * 回复结构为 {@code [[total, doc1, doc2, ...], cursorId]}，
 * 解析为含游标 ID 的 {@link AggregationResult}。
 *
 * @author Nikita Koksharov
 *
 */
public class AggregationCursorResultDecoder implements MultiDecoder<Object> {

    /** 解析聚合行与游标 ID；空回复时返回总数 0、游标 -1。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return new AggregationResult(0, Collections.emptyList(), -1);
        }

        List<Object> list = (List<Object>) parts.get(0);
        List<Map<String, Object>> docs = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            Map<String, Object> attrs = (Map<String, Object>) list.get(i);
            docs.add(attrs);
        }

        long total = (long) list.get(0);
        long cursorId = (long) parts.get(1);
        return new AggregationResult(total, docs, cursorId);
    }

}
