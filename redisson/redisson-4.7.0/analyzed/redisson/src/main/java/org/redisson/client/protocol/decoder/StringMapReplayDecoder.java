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
 * 字符串键值对 Map 批量回放解码器。
 * <p>
 * 将 RESP 数组中交替出现的键、值元素（{@code [k1, v1, k2, v2, ...]}）
 * 组装为保持插入顺序的 {@link Map}{@code <String, String>}。
 *
 * @author Nikita Koksharov
 *
 */
public class StringMapReplayDecoder implements MultiDecoder<Map<String, String>> {

    /** 按奇偶索引配对键值，调用 {@link MultiDecoder#newLinkedHashMap} 保序。 */
    @Override
    public Map<String, String> decode(List<Object> parts, State state) {
        Map<String, String> result = MultiDecoder.newLinkedHashMap(parts.size()/2);
        for (int i = 0; i < parts.size(); i++) {
            if (i % 2 != 0) {
                result.put(parts.get(i-1).toString(), parts.get(i).toString());
            }
        }
        return result;
    }

}
