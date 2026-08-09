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

import java.util.*;

/**
 * {@code FT.AGGREGATE WITHCURSOR} 游标聚合结果解码器 V2。
 * <p>
 * 兼容 RESP2 扁平格式 {@code [count, doc1, doc2, ...]} 与 RESP3 映射格式
 * （含 {@code total_results}、{@code results}、{@code extra_attributes}）。
 *
 * @author Nikita Koksharov
 *
 */
public class AggregationCursorResultDecoderV2 implements MultiDecoder<Object> {

    /** 按首元素类型选择 RESP2 或 RESP3 解析路径，并附带游标 ID。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return new AggregationResult(0, Collections.emptyList(), -1);
        }

        List<Object> attrs = (List<Object>) parts.get(0);
        long cursorId = (long) parts.get(1);

        // 未指定 FORMAT 时，FT.AGGREGATE WITHCURSOR 在 RESP3 连接上仍返回
        // 传统 RESP2 扁平结构 [count, doc1, doc2, ...]。
        if (!attrs.isEmpty() && attrs.get(0) instanceof Long) {
            long total = (Long) attrs.get(0);
            List<Map<String, Object>> docs = new ArrayList<>();
            for (int i = 1; i < attrs.size(); i++) {
                docs.add((Map<String, Object>) attrs.get(i));
            }
            return new AggregationResult(total, docs, cursorId);
        }

        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < attrs.size(); i++) {
            if (i % 2 != 0) {
                m.put(attrs.get(i-1).toString(), attrs.get(i));
            }
        }

        List<Map<String, Object>> docs = new ArrayList<>();
        List<Map<String, Object>> results = (List<Map<String, Object>>) m.get("results");
        for (Map<String, Object> result : results) {
            Map<String, Object> map = (Map<String, Object>) result.get("extra_attributes");
            docs.add(map);
        }
        Long total = (Long) m.get("total_results");
        return new AggregationResult(total, docs, cursorId);
    }

}
