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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.xerial.snappy.Snappy;

import java.io.IOException;

/**
 * 基于 Google Snappy 的压缩编解码器（V2 实现）。
 * <p>
 * 先用内层 {@link Codec} 序列化对象，再对字节数组进行 Snappy 压缩/解压；
 * 默认内层为 {@link Kryo5Codec}，完全线程安全。
 * <p>
 * 基于 <a href="https://github.com/xerial/snappy-java">snappy-java</a>。
 *
 * @see org.redisson.codec.Kryo5Codec
 *
 * @author Nikita Koksharov
 *
 */
public class SnappyCodecV2 extends BaseCodec {

    /** 内层对象序列化编解码器。 */
    private final Codec innerCodec;

    /** 默认内层为 {@link Kryo5Codec}。 */
    public SnappyCodecV2() {
        this(new Kryo5Codec());
    }

    /** @param innerCodec 自定义内层编解码器 */
    public SnappyCodecV2(Codec innerCodec) {
        this.innerCodec = innerCodec;
    }

    /** @param classLoader Kryo 反序列化类加载器 */
    public SnappyCodecV2(ClassLoader classLoader) {
        this(new Kryo5Codec(classLoader));
    }
    
    /** 在指定类加载器下复制编解码器。 */
    public SnappyCodecV2(ClassLoader classLoader, SnappyCodecV2 codec) throws ReflectiveOperationException {
        this(copy(classLoader, codec.innerCodec));
    }
    
    /** 解码：Snappy 解压后委托内层解码器。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            bytes = Snappy.uncompress(bytes);
            ByteBuf bf = Unpooled.wrappedBuffer(bytes);
            try {
                return innerCodec.getValueDecoder().decode(bf, state);
            } finally {
                bf.release();
            }
        }
    };

    /** 编码：内层序列化后 Snappy 压缩。 */
    private final Encoder encoder = new Encoder() {

        @Override
        public ByteBuf encode(Object in) throws IOException {
            ByteBuf encoded = innerCodec.getValueEncoder().encode(in);
            byte[] bytes = new byte[encoded.readableBytes()];
            encoded.readBytes(bytes);
            encoded.release();
            byte[] res = Snappy.compress(bytes);
            return Unpooled.wrappedBuffer(res);
        }
    };

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }
    
}
