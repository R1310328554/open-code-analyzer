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
package org.redisson.client.codec;

import java.util.BitSet;

import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

/**
 * {@link BitSet} 的 Redis 编解码器（已弃用）。
 * <p>
 * 仅实现解码：将 Redis 字节串按大端位序解析为 {@link BitSet}，编码操作不支持。
 *
 * @author Nikita Koksharov
 *
 */
@Deprecated
public class BitSetCodec implements Codec {

    /** 单例实例。 */
    public static final BitSetCodec INSTANCE = new BitSetCodec();

    private final Decoder<Object> decoder = (buf, state) -> {
        byte[] result = new byte[buf.readableBytes()];
        buf.readBytes(result);
        return fromByteArrayReverse(result);
    };

    /** 将 Redis 返回的字节数组按 MSB 优先顺序转换为 {@link BitSet}。 */
    private static BitSet fromByteArrayReverse(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[i / 8] & (1 << (7 - (i % 8)))) != 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    /** 返回值解码器。 */
    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    /** 不支持编码，调用时抛出 {@link UnsupportedOperationException}。 */
    @Override
    public Encoder getValueEncoder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Decoder<Object> getMapValueDecoder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Encoder getMapValueEncoder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Encoder getMapKeyEncoder() {
        throw new UnsupportedOperationException();
    }

    /** 返回当前类的类加载器。 */
    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

}
