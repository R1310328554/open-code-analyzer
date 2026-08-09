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
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 单一 {@link Decoder} 的多段解码包装器。
 * <p>
 * 所有嵌套字段复用同一个底层解码器；
 * {@link #decode} 直接返回已解码的 parts 列表，不做额外聚合。
 *
 * @author Nikita Koksharov
 *
 */
public class ObjectDecoder<T> implements MultiDecoder<Object> {

    /** 用于所有子字段的底层解码器。 */
    private final Decoder<T> decoder;
    
    /** 指定复用的解码器实例。 */
    public ObjectDecoder(Decoder<T> decoder) {
        super();
        this.decoder = decoder;
    }

    /** 始终返回构造时注入的解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return (Decoder<Object>) decoder;
    }

    /** 子段已全部解码，直接透传 parts 列表。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        return parts;
    }

}
