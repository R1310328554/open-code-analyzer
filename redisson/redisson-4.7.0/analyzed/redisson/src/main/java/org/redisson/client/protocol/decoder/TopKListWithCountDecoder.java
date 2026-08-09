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

import org.redisson.client.handler.State;

import java.util.List;
import java.util.Map;

/**
 * Top-K 元素及计数 Map 解码器。
 * <p>
 * 将 {@code [item1, count1, item2, count2, ...]} 交替数组
 * 转为 {@link Map}{@code <V, Long>}，跳过 {@code null} 元素键。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class TopKListWithCountDecoder<V> implements MultiDecoder<Map<V, Long>> {

    /** 按步长 2 配对元素与计数，使用保序 {@link Map} 存储。 */
    @Override
    public Map<V, Long> decode(List<Object> parts, State state) {
        Map<V, Long> result = MultiDecoder.newLinkedHashMap(parts.size() / 2);
        for (int i = 0; i < parts.size() - 1; i += 2) {
            V item = (V) parts.get(i);
            if (item == null) {
                continue;
            }
            long count = ((Number) parts.get(i + 1)).longValue();
            result.put(item, count);
        }
        return result;
    }

}
