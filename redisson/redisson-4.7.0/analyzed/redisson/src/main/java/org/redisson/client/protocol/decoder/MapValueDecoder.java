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

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import java.util.List;

/**
 * 仅解码 Map 值字段的多段解码器包装器。
 * <p>
 * 所有嵌套字段统一使用 {@link Codec#getMapValueDecoder()}；
 * 无委托解码器时直接返回原始 parts 列表。
 *
 * @author Nikita Koksharov
 */
public class MapValueDecoder<T> implements MultiDecoder<Object> {

    /** 负责将完整 parts 列表转为目标结构的委托解码器，可为 null。 */
    private final MultiDecoder<Object> decoder;

    /** 指定聚合解码器。 */
    public MapValueDecoder(MultiDecoder<Object> decoder) {
        this.decoder = decoder;
    }

    /** 无委托时使用默认构造。 */
    public MapValueDecoder() {
        this(null);
    }

    /** 始终返回 Map 值解码器，忽略 paramNum。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return codec.getMapValueDecoder();
    }

    /** 有委托时交给其聚合；否则直接返回 parts 作为结果。 */
    @Override
    public T decode(List<Object> parts, State state) {
        if (decoder != null) {
            return (T) decoder.decode(parts, state);
        }
        return (T) parts;
    }

}
