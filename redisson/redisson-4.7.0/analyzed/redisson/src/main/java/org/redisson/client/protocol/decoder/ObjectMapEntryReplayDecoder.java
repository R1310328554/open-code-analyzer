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

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 扁平键值数组到 {@code Set<Entry>} 的回放解码器。
 * <p>
 * 将 [key1, val1, key2, val2, ...] 聚合为 {@link LinkedHashMap}，
 * 最终返回其 {@link Map#entrySet()}，保持插入顺序。
 *
 * @author Nikita Koksharov
 *
 */
public class ObjectMapEntryReplayDecoder implements MultiDecoder<Set<Entry<Object, Object>>> {

    /** 偶数索引为键、奇数索引为值，分别选用对应 Map 解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (paramNum % 2 != 0) {
            return codec.getMapValueDecoder();
        }
        return codec.getMapKeyDecoder();
    }

    /** 逐对填充 LinkedHashMap 并返回 entrySet。 */
    @Override
    public Set<Entry<Object, Object>> decode(List<Object> parts, State state) {
        Map<Object, Object> result = MultiDecoder.newLinkedHashMap(parts.size()/2);
        for (int i = 0; i < parts.size(); i++) {
            if (i % 2 != 0) {
                result.put(parts.get(i-1), parts.get(i));
           }
        }
        return result.entrySet();
    }

}
