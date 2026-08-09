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
package org.redisson.connection.decoder;

import org.redisson.client.handler.State;
import org.redisson.client.protocol.decoder.MultiDecoder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 原生 Map 结构（如 RMapNative）批量读取解码器。
 * <p>
 * Redis 返回 Long 序列；-2 表示 key 不存在；Boolean 类型时将 1/0 转为 true/false。
 *
 * @author Nikita Koksharov
 *
 */
public class MapNativeAllDecoder implements MultiDecoder<Map<Object, Object>> {

    /** 请求 key 列表。 */
    private final List<Object> args;
    /** 目标 value Java 类型（Long 或 Boolean）。 */
    private final Class<?> valueClass;

    /** @param args key 列表；@param valueClass 期望的 value 类型 */
    public MapNativeAllDecoder(List<Object> args, Class<?> valueClass) {
        this.args = args;
        this.valueClass = valueClass;
    }

    /** 将 Long 响应按 valueClass 转换后配对为 Map；-2 对非 Long 类型表示缺失 key。 */
    @Override
    public Map<Object, Object> decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return new HashMap<>();
        }
        Map<Object, Object> result = new LinkedHashMap<>(parts.size());
        for (int index = 0; index < parts.size(); index++) {
            Long value = (Long) parts.get(index);
            if (value == -2 && valueClass != Long.class) {
                continue;
            }
            if (valueClass == Boolean.class) {
                result.put(args.get(index), value == 1);
            } else {
                result.put(args.get(index), value);
            }
        }
        return result;
    }

}
