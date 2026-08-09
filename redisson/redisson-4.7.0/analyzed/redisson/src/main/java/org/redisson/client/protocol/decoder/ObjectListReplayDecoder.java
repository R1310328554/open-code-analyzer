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

import java.util.Collections;
import java.util.List;

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 列表型 RESP 数组回放解码器。
 * <p>
 * 将已逐段解码的 parts 直接作为 {@code List<T>} 返回，
 * 可选地在返回前反转顺序（如 {@code LRANGE} 倒序场景）。
 *
 * @author Nikita Koksharov
 *
 * @param <T> type
 */
public class ObjectListReplayDecoder<T> implements MultiDecoder<List<T>> {

    /** 可选的自定义子字段解码器；为 null 时使用 {@link MultiDecoder} 默认策略。 */
    private final Decoder<Object> decoder;
    /** 是否在返回前反转 parts 顺序。 */
    private final boolean reverse;

    /** 默认正序返回。 */
    public ObjectListReplayDecoder() {
        this(false);
    }

    /** 指定是否反转顺序。 */
    public ObjectListReplayDecoder(boolean reverse) {
        this(reverse, null);
    }

    /** 同时指定反转标志与自定义子字段解码器。 */
    public ObjectListReplayDecoder(boolean reverse, Decoder<Object> decoder) {
        super();
        this.reverse = reverse;
        this.decoder = decoder;
    }

    /** 按需反转后返回 parts 作为 List。 */
    @Override
    public List<T> decode(List<Object> parts, State state) {
        if (reverse) {
            Collections.reverse(parts);
        }
        return (List<T>) parts;
    }

    /** 有自定义解码器时使用之，否则回退到接口默认实现。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (decoder != null) {
            return decoder;
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);
    }
}
