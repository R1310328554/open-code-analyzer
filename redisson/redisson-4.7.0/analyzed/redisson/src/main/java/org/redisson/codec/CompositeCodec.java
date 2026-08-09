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
package org.redisson.codec;

import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.util.Objects;

/**
 * 组合编解码器：为 Map 键、Map 值与普通值分别指定不同的 {@link Codec}。
 * <p>
 * 典型场景是 Map 的键用字符串编解码、值用 JSON 或 Kryo 等，而普通 RBucket 仍走默认 Codec。
 * 各 {@code get*Encoder/Decoder} 方法直接委托给对应的内层编解码器。
 *
 * @author Nikita Koksharov
 *
 */
public class CompositeCodec implements Codec {

    /** Map 键编解码器。 */
    private final Codec mapKeyCodec;
    /** Map 值编解码器。 */
    private final Codec mapValueCodec;
    /** 普通值编解码器；可为 null，此时 value 相关方法会 NPE。 */
    private final Codec valueCodec;
    
    /** 仅指定 Map 键/值编解码器，普通值编解码器为 null。 */
    public CompositeCodec(Codec mapKeyCodec, Codec mapValueCodec) {
        this(mapKeyCodec, mapValueCodec, null);
    }
    
    /**
     * 为 Map 键、Map 值与普通值分别指定编解码器。
     *
     * @param mapKeyCodec Map 键编解码器
     * @param mapValueCodec Map 值编解码器
     * @param valueCodec 普通值编解码器，可为 null
     */
    public CompositeCodec(Codec mapKeyCodec, Codec mapValueCodec, Codec valueCodec) {
        super();
        this.mapKeyCodec = mapKeyCodec;
        this.mapValueCodec = mapValueCodec;
        this.valueCodec = valueCodec;
    }

    /** 按 ClassLoader 深拷贝内层三个编解码器，用于跨 ClassLoader 场景。 */
    public CompositeCodec(ClassLoader classLoader, CompositeCodec codec) throws ReflectiveOperationException {
        super();
        this.mapKeyCodec = BaseCodec.copy(classLoader, codec.mapKeyCodec);
        this.mapValueCodec = BaseCodec.copy(classLoader, codec.mapValueCodec);
        this.valueCodec = BaseCodec.copy(classLoader, codec.valueCodec);
    }
    
    @Override
    public Decoder<Object> getMapValueDecoder() {
        return mapValueCodec.getMapValueDecoder();
    }

    @Override
    public Encoder getMapValueEncoder() {
        return mapValueCodec.getMapValueEncoder();
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return mapKeyCodec.getMapKeyDecoder();
    }

    @Override
    public Encoder getMapKeyEncoder() {
        return mapKeyCodec.getMapKeyEncoder();
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return valueCodec.getValueDecoder();
    }

    @Override
    public Encoder getValueEncoder() {
        return valueCodec.getValueEncoder();
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    /** 比较三个内层编解码器是否一致。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeCodec that = (CompositeCodec) o;
        return Objects.equals(mapKeyCodec, that.mapKeyCodec)
                && Objects.equals(mapValueCodec, that.mapValueCodec)
                    && Objects.equals(valueCodec, that.valueCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapKeyCodec, mapValueCodec, valueCodec);
    }
}
