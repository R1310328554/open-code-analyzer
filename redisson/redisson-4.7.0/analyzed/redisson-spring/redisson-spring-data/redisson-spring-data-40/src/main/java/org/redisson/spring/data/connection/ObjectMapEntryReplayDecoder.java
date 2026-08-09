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
package org.redisson.spring.data.connection;

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.decoder.MultiDecoder;

import java.util.*;
import java.util.Map.Entry;

/**
 * Map 键值对回放解码器：将 Redis 扁平 key-value 序列解析为 {@link Entry} 列表。
 * <p>奇偶位分别使用 Codec 的 map key/value 解码器；结果保持插入顺序。
 *
 * @author Nikita Koksharov
 *
 */
public class ObjectMapEntryReplayDecoder implements MultiDecoder<List<Entry<Object, Object>>> {

    /** 奇数位返回 value 解码器，偶数位返回 key 解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        // 奇数索引为 value，偶数索引为 key。
        if (paramNum % 2 != 0) {
            return codec.getMapValueDecoder();
        }
        return codec.getMapKeyDecoder();
    }

    /** 将扁平 parts 按相邻 key-value 对组装为有序条目列表。 */
    @Override
    public List<Entry<Object, Object>> decode(List<Object> parts, State state) {
        Map<Object, Object> result = new LinkedHashMap<>(parts.size() / 2);
        for (int i = 0; i < parts.size(); i++) {
            // 每两个元素构成一对 key-value。
            if (i % 2 != 0) {
                result.put(parts.get(i-1), parts.get(i));
           }
        }
        return new ArrayList<>(result.entrySet());
    }

}
