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

import org.redisson.api.stream.PendingResult;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.convertor.StreamIdConvertor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis Stream 待处理汇总（XPENDING 概览）解码器。
 * <p>
 * 解析 {@code [total, min_id, max_id, [[consumer, count], ...]]} 结构，
 * 组装为 {@link PendingResult}，含各消费者的待处理消息计数。
 *
 * @author Nikita Koksharov
 *
 */
public class PendingResultDecoder implements MultiDecoder<Object> {

    /** 流消息 ID 转换器，用于 min/max 边界 ID。 */
    private final StreamIdConvertor convertor = new StreamIdConvertor();

    /** 汇总待处理总数、ID 范围及按消费者分组的计数。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return null;            
        }
        
        List<List<String>> customerParts = (List<List<String>>) parts.get(3);
        if (customerParts.isEmpty()) {
            return new PendingResult(0, null, null, Collections.emptyMap());
        }
        
        Map<String, Long> consumerNames = new LinkedHashMap<String, Long>();
        for (List<String> mapping : customerParts) {
            consumerNames.put(mapping.get(0), Long.valueOf(mapping.get(1)));
        }
        return new PendingResult((Long) parts.get(0), convertor.convert(parts.get(1)), convertor.convert(parts.get(2)), consumerNames);
    }

}
