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
import java.util.List;
import java.util.Map;

/**
 * {@code FT.AGGREGATE} 聚合结果解码器（RESP2 位置式格式）。
 * <p>
 * 回复结构为 {@code [total, row1, row2, ...]}，首元素为匹配总数，
 * 后续每个元素为一行的属性 {@link Map}。
 *
 * @author Nikita Koksharov
 *
 */
public class AggregationResultDecoder implements MultiDecoder<Object> {

    /** 解析总数与各行属性，空回复返回 {@code null}。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return null;            
        }

        long total = (long) parts.get(0);
        List<Map<String, Object>> docs = new ArrayList<>();
        if (total > 0) {
            for (int i = 1; i < parts.size(); i++) {
                Map<String, Object> attrs = (Map<String, Object>) parts.get(i);
                docs.add(attrs);
            }
        }

        return new AggregationResult(total, docs);
    }

}
