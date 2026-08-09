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
import java.util.stream.Collectors;

/**
 * 多 Map 合并解码器。
 * <p>
 * 输入为若干已解码的 {@code Map} 对象列表，将所有条目的键值对
 * 扁平合并为单个 {@code Map}；同名键以后出现的值覆盖先前的值。
 *
 * @author Nikita Koksharov
 *
 */
public class MapMergeDecoder implements MultiDecoder<Map<Object, Object>> {

    /** 展开每个子 Map 的 entrySet 并 collect 为单一 Map。 */
    @Override
    public Map<Object, Object> decode(List<Object> parts, State state) {
        return parts.stream()
                    .flatMap(l -> ((Map<Object, Object>) l).entrySet().stream())
                    .collect(Collectors.toMap(v -> v.getKey(), v -> v.getValue()));
    }

}
