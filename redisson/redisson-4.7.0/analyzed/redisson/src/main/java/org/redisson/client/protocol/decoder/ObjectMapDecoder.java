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

import java.util.List;
import java.util.Map;

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 扁平键值数组到 Map 的分阶段解码器。
 * <p>
 * 首次调用将 [key1, val1, key2, val2, ...] 聚合为 {@link LinkedHashMap}，
 * 并通过 {@link State} 标记进入内层；后续嵌套层可透传原始 parts 列表
 * （当 {@code decodeList} 为 true 时）。
 *
 * @author Nikita Koksharov
 *
 */
public class ObjectMapDecoder implements MultiDecoder<Object> {

    /** 内层嵌套时是否直接返回 parts 列表而非再次聚合为 Map。 */
    private final boolean decodeList;
    
    /** 指定内层是否以列表形式透传。 */
    public ObjectMapDecoder(boolean decodeList) {
        super();
        this.decodeList = decodeList;
    }

    /** 内层或偶数索引用键解码器，奇数索引用值解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (state.getValue() != null && (Boolean) state.getValue()) {
            return codec.getMapKeyDecoder();
        }
        
        if (paramNum % 2 == 0) {
            return codec.getMapKeyDecoder();
        }
        return codec.getMapValueDecoder();
    }
    
    /** 首段聚合为 Map 并标记 state；内层且 decodeList 时透传 parts。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (decodeList && (state.getValue() != null && (Boolean) state.getValue())) {
            return parts;
        }

        Map<Object, Object> result = MultiDecoder.newLinkedHashMap(parts.size()/2);
        for (int i = 0; i < parts.size(); i++) {
            if (i % 2 != 0) {
                result.put(parts.get(i-1), parts.get(i));
           }
        }

        // 标记已进入内层，后续 getDecoder/decode 走透传分支
        state.setValue(true);
        return result;
    }

}
