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

import java.util.LinkedHashMap;
import java.util.List;

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 多段 RESP 数组聚合解码器接口。
 * <p>
 * 与 {@link Decoder} 不同，{@code MultiDecoder} 负责将已逐段解码的
 * {@code List<Object>} 组装为最终业务对象；子字段解码器由
 * {@link #getDecoder} 按索引动态选择。
 *
 * @author Nikita Koksharov
 *
 * @param <T> type
 */
public interface MultiDecoder<T> {

    /** 带已解码 parts 上下文的 getDecoder 扩展，默认委托给四参数版本。 */
    default Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size, List<Object> parts) {
        return getDecoder(codec, paramNum, state, size);
    }

    /**
     * 为第 {@code paramNum} 个子字段选择解码器（已弃用，请使用五参数版本）。
     * <p>
     * 默认在 codec 为 null 时回退到 {@link StringCodec}，否则使用通用值解码器。
     */
    @Deprecated
    default Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (codec == null) {
            codec = StringCodec.INSTANCE;
        }
        return codec.getValueDecoder();
    }
    
    /** 将完整 parts 列表聚合为目标类型 {@code T}。 */
    T decode(List<Object> parts, State state);

    /** 按预期条目数创建容量合适的 {@link LinkedHashMap}，避免扩容与 rehash。 */
    static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int expectedSize) {
        if (expectedSize < 3) {
            return new LinkedHashMap<>(expectedSize + 1);
        }
        return new LinkedHashMap<>((int) Math.ceil(expectedSize / 0.75));
    }

}
