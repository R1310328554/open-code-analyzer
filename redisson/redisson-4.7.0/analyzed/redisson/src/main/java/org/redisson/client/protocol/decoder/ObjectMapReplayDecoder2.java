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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 嵌套键值对 Map 回放解码器（第二版）。
 * <p>
 * 与 {@link ObjectMapReplayDecoder} 处理扁平 [k1,v1,k2,v2,...] 不同，
 * 本解码器接收外层 List，其中每个元素为 {@code [key, value]} 二元组，
 * 过滤 null 后聚合为保持插入顺序的 {@code Map<Object, Object>}。
 *
 * @author Nikita Koksharov
 *
 */
public class ObjectMapReplayDecoder2 implements MultiDecoder<Map<Object, Object>> {

    /** 将嵌套 [[k,v],...] 结构转为 LinkedHashMap，重复键保留首次出现。 */
    @Override
    public Map<Object, Object> decode(List<Object> parts, State state) {
        List<List<Object>> list = (List<List<Object>>) (Object) parts;
        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(e -> e.get(0), e -> e.get(1),
                        (a, b) -> a, LinkedHashMap::new));
    }

}
