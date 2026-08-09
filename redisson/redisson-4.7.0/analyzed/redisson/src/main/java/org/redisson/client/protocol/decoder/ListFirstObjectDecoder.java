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
import org.redisson.client.protocol.convertor.Convertor;

import java.util.List;

/**
 * 列表首元素提取解码器。
 * <p>
 * 可选地先经内部 {@link MultiDecoder} 预处理，再返回列表第一个元素；
 * 空列表时可通过 {@link Convertor} 将 {@code null} 转为默认值。
 *
 * @author Nikita Koksharov
 *
 */
public class ListFirstObjectDecoder implements MultiDecoder<Object> {

    /** 可选的内层解码器，用于先转换整个列表。 */
    private MultiDecoder<Object> inner;
    /** 空列表时的 null 值转换器。 */
    private Convertor<?> convertor;

    /** 无转换器的默认构造。 */
    public ListFirstObjectDecoder() {
        this((Convertor<?>) null);
    }

    /** 指定空列表时的值转换器。 */
    public ListFirstObjectDecoder(Convertor<?> convertor) {
        this.convertor = convertor;
    }

    /** 指定内层列表解码器。 */
    public ListFirstObjectDecoder(MultiDecoder<Object> inner) {
        this.inner = inner;
    }

    /** 委托内层解码器选择子字段解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size, List<Object> parts) {
        if (inner != null) {
            return inner.getDecoder(codec, paramNum, state, size, parts);
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size, parts);
    }

    /** 委托内层解码器选择子字段解码器（无 parts 上下文）。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (inner != null) {
            return inner.getDecoder(codec, paramNum, state, size);
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);
    }

    /** 解码后取首元素；空列表走 convertor 或返回 null。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (inner != null) {
            parts = (List) inner.decode(parts, state);
        }
        if (!parts.isEmpty()) {
            return parts.get(0);
        }
        if (convertor != null) {
            return convertor.convert(null);
        }
        return null;
    }

}
