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

import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import io.netty.buffer.Unpooled;

/**
 * 原始字节数组（{@code byte[]}）的 Redis 编解码器。
 * <p>
 * 编码时使用 Netty {@code wrappedBuffer}，解码时完整读取可读字节。
 *
 * @author Nikita Koksharov
 *
 */
public class ByteArrayCodec extends BaseCodec {

    /** 单例实例。 */
    public static final ByteArrayCodec INSTANCE = new ByteArrayCodec();

    private final Encoder encoder = in -> Unpooled.wrappedBuffer((byte[]) in);

    private final Decoder<Object> decoder = (buf, state) -> {
        byte[] result = new byte[buf.readableBytes()];
        buf.readBytes(result);
        return result;
    };

    /** 返回将缓冲区读为 {@code byte[]} 的解码器。 */
    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    /** 返回将 {@code byte[]} 包装为 Netty 缓冲区的编码器。 */
    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

}
