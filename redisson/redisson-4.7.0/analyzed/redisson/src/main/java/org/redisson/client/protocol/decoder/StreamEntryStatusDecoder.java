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
import org.redisson.client.protocol.StreamEntryStatus;

import java.util.*;

/**
 * Stream 条目状态批量解码器。
 * <p>
 * 将 Redis 返回的整数状态码列表，按构造时传入的键顺序
 * 映射为 {@link StreamEntryStatus} 枚举值的 {@link Map}。
 *
 * @author seakider
 *
 */
public class StreamEntryStatusDecoder<K, V> implements MultiDecoder<Map<K, V>> {
    /** 与响应中各状态码一一对应的键列表。 */
    private final List<K> args;

    /** 保存键顺序，供 decode 时按索引对齐状态码。 */
    public StreamEntryStatusDecoder(Collection<K> args) {
        if (args instanceof List) {
            this.args = (List<K>) args;
        } else {
            this.args = new ArrayList<>(args);
        }
    }

    /** 逐索引将 Long 状态码转为 {@link StreamEntryStatus} 并填入 Map。 */
    @Override
    public Map<K, V> decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<K, V> result = MultiDecoder.newLinkedHashMap(parts.size());
        for (int index = 0; index < parts.size(); index++) {
            Long value = (Long) parts.get(index);
            result.put(args.get(index), (V) StreamEntryStatus.valueOfStatus(value.intValue()));
        }

        return result;
    }
}
