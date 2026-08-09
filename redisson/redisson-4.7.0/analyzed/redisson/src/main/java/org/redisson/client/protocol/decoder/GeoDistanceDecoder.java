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

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * {@code GEODIST} / {@code GEORADIUS} 等带距离字段的 Geo 回复解码器。
 * <p>
 * 奇数参数位解码为 {@code Double} 距离，偶数位保持默认字符串解码；
 * 最终原样返回 {@code parts} 供上层配对成员名与距离。
 *
 * @author Nikita Koksharov
 *
 */
public class GeoDistanceDecoder implements MultiDecoder<List<Object>> {

    /** 奇数索引（距离位）使用 {@link DoubleCodec}，其余走默认解码。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        // 成员名与距离交替出现，距离在奇数 paramNum
        if (paramNum % 2 != 0) {
            return DoubleCodec.INSTANCE.getValueDecoder();
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);
    }
    
    /** 子元素已在 getDecoder 中按类型解码，此处直接返回列表。 */
    @Override
    public List<Object> decode(List<Object> parts, State state) {
        return parts;
    }

}
