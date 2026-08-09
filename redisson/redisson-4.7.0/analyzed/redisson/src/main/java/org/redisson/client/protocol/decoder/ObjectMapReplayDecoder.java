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
import java.util.Map;
import java.util.Optional;

/**
 * 泛型 Map 回放解码器。
 * <p>
 * 将扁平 [key1, val1, key2, val2, ...] 数组聚合为 {@code Map<K, V>}，
 * 支持键值对调（{@code swapKeyValue}）及固定 {@link Codec} 覆盖。
 *
 * @author Nikita Koksharov
 *
 */
public class ObjectMapReplayDecoder<K, V> implements MultiDecoder<Map<K, V>> {

    /** 是否交换键值顺序（值作键、键作值）。 */
    private boolean swapKeyValue;
    /** 可选的固定编解码器，为 null 时使用调用方传入的 codec。 */
    private final Codec codec;

    /** 使用运行时传入的 codec。 */
    public ObjectMapReplayDecoder(Codec codec) {
        this.codec = codec;
    }

    /** 无固定 codec，完全依赖 getDecoder 参数。 */
    public ObjectMapReplayDecoder() {
        this(null);
    }

    /** 同时指定键值对调标志与固定 codec。 */
    public ObjectMapReplayDecoder(boolean swapKeyValue, Codec codec) {
        this.swapKeyValue = swapKeyValue;
        this.codec = codec;
    }

    /** 偶数索引选键解码器，奇数索引选值解码器；优先使用构造时的 codec。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        Codec c = Optional.ofNullable(this.codec).orElse(codec);
        if (paramNum % 2 != 0) {
            return c.getMapValueDecoder();
        }
        return c.getMapKeyDecoder();
    }

    /** 逐对写入 LinkedHashMap，swapKeyValue 时颠倒键值。 */
    @Override
    public Map<K, V> decode(List<Object> parts, State state) {
        Map<K, V> result = MultiDecoder.newLinkedHashMap(parts.size()/2);
        for (int i = 0; i < parts.size(); i++) {
            if (i % 2 != 0) {
                if (swapKeyValue) {
                    result.put((K) parts.get(i), (V) parts.get(i-1));
                } else {
                    result.put((K) parts.get(i-1), (V) parts.get(i));
                }
            }
        }
        return result;
    }

}
