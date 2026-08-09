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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量 {@code GEOPOS} 回复与成员名映射解码器。
 * <p>
 * 将按请求顺序返回的坐标数组与构造时传入的成员列表对齐，
 * 跳过空/null/空列表项，得到 {@code Map<成员, 坐标>}。
 *
 * @author Nikita Koksharov
 *
 */
public class GeoPositionMapDecoder implements MultiDecoder<Map<Object, Object>> {

    /** 与回复逐位对应的 Geo 成员名（或 key）列表。 */
    private final List<Object> args;

    /** @param args 与 GEOPOS 批量查询顺序一致的成员列表 */
    public GeoPositionMapDecoder(List<Object> args) {
        this.args = args;
    }

    /** 空回复返回空 Map；否则按索引将非空坐标写入结果。 */
    @Override
    public Map<Object, Object> decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Object, Object> result = new HashMap<Object, Object>(parts.size());
        for (int index = 0; index < args.size(); index++) {
            Object value = parts.get(index);
            // 该成员无坐标或 Redis 返回 nil
            if (value == null || value == Collections.emptyMap()) {
                continue;
            }
            // 空数组同样表示无有效坐标
            if (value instanceof List && ((List) value).isEmpty()) {
                continue;
            }
            
            result.put(args.get(index), value);
        }
        return result;
    }

}
