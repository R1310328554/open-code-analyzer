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
 * RMap {@code getAll} 响应解码器，将扁平 value 列表与请求 key 列表配对为 LinkedHashMap。
 * <p>
 * 保持 key 请求顺序；可选跳过 null value。
 *
 * @author Nikita Koksharov
 *
 */
public class MapGetAllDecoder implements MultiDecoder<Map<Object, Object>> {

    /** key 在 args 中的索引偏移。 */
    private final int shiftIndex;
    /** 请求 key 列表。 */
    private final List<Object> args;
    /** 是否保留 null value。 */
    private final boolean allowNulls;

    /** 默认过滤 null value。 */
    public MapGetAllDecoder(List<Object> args, int shiftIndex) {
        this(args, shiftIndex, false);
    }
    
    /** @param allowNulls 为 true 时保留 null value */
    public MapGetAllDecoder(List<Object> args, int shiftIndex, boolean allowNulls) {
        this.args = args;
        this.shiftIndex = shiftIndex;
        this.allowNulls = allowNulls;
    }

    /** 按索引将 parts 中的 value 与 args 中对应 key 组装为有序 Map。 */
    @Override
    public Map<Object, Object> decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return new HashMap<>();
        }
        Map<Object, Object> result = new LinkedHashMap<>(parts.size());
        for (int index = 0; index < parts.size()-shiftIndex; index++) {
            Object value = parts.get(index);
            if (!allowNulls && value == null) {
                continue;
            }
            result.put(args.get(index+shiftIndex), value);
        }
        return result;
    }

}
