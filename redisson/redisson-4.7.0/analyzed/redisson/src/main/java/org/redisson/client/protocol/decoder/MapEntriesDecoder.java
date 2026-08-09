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
 * Map 键值对条目解码器（交替选择键/值解码器）。
 * <p>
 * 在扁平的 [key1, val1, key2, val2, ...] 数组上，
 * 偶数索引用 {@link Codec#getMapKeyDecoder()}，奇数索引用 {@link Codec#getMapValueDecoder()}。
 * 最终聚合逻辑委托给内部 {@link MultiDecoder}。
 *
 * @author Nikita Koksharov
 */
public class MapEntriesDecoder<T> implements MultiDecoder<Object> {

    /** 负责将完整 parts 列表转为目标结构的委托解码器。 */
    private final MultiDecoder<Object> decoder;

    /** 指定聚合解码器。 */
    public MapEntriesDecoder(MultiDecoder<Object> decoder) {
        this.decoder = decoder;
    }

    /** 无委托时使用默认构造，decode 时需外部保证 decoder 非空。 */
    public MapEntriesDecoder() {
        this(null);
    }

    /** 按索引奇偶切换 Map 键解码器与值解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (paramNum % 2 != 0) {
            return codec.getMapValueDecoder();
        } else {
            return codec.getMapKeyDecoder();
        }
    }

    /** 将已解码的 parts 交给委托解码器完成最终转换。 */
    @Override
    public T decode(List<Object> parts, State state) {
        return (T) decoder.decode(parts, state);
    }

}
