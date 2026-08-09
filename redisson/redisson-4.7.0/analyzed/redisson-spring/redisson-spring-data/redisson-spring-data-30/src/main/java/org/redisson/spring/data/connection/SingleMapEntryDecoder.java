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

import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * 单条 Map 键值对解码器：将 Redis 返回的 key-value 二元组解析为 {@link Entry}。
 * <p>奇偶位分别使用 Codec 的 map key/value 解码器。
 *
 * @author Nikita Koksharov
 *
 */
public class SingleMapEntryDecoder implements MultiDecoder<Entry<Object, Object>> {

    /** 奇数位返回 value 解码器，偶数位返回 key 解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        // 奇数索引为 value，偶数索引为 key。
        if (paramNum % 2 != 0) {
            return codec.getMapValueDecoder();
        }
        return codec.getMapKeyDecoder();
    }

    /** 将 parts 的前两个元素组装为 {@link AbstractMap.SimpleEntry}。 */
    @Override
    public Entry<Object, Object> decode(List<Object> parts, State state) {
        return new AbstractMap.SimpleEntry<>(parts.get(0), parts.get(1));
    }

}
