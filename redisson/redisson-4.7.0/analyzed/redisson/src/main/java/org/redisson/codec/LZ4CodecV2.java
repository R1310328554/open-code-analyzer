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

import io.netty.buffer.*;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * 基于 Apache Commons Compress 的 LZ4 块压缩编解码器（V2 实现）。
 * <p>
 * 与 {@link LZ4Codec} 不同，本类使用 Block LZ4 流式压缩格式，
 * 内层默认仍为 {@link Kryo5Codec}，完全线程安全。
 * <p>
 * 实现参考 <a href="https://github.com/apache/commons-compress">commons-compress</a>。
 *
 * @see Kryo5Codec
 *
 * @author Nikita Koksharov
 *
 */
public class LZ4CodecV2 extends BaseCodec {

    /** 内层对象序列化编解码器。 */
    private final Codec innerCodec;

    /** 默认内层为 {@link Kryo5Codec}。 */
    public LZ4CodecV2() {
        this(new Kryo5Codec());
    }

    /** @param innerCodec 自定义内层编解码器 */
    public LZ4CodecV2(Codec innerCodec) {
        this.innerCodec = innerCodec;
    }

    /** @param classLoader Kryo 反序列化类加载器 */
    public LZ4CodecV2(ClassLoader classLoader) {
        this(new Kryo5Codec(classLoader));
    }

    /** 在指定类加载器下复制编解码器。 */
    public LZ4CodecV2(ClassLoader classLoader, LZ4CodecV2 codec) throws ReflectiveOperationException {
        this(copy(classLoader, codec.innerCodec));
    }
    
    /** 解码：读取原始长度，Block LZ4 解压后委托内层解码。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            int decompressionSize = buf.readInt();
            byte[] bytes = new byte[decompressionSize];
            ByteBufInputStream ios = new ByteBufInputStream(buf);
            try (DataInputStream in = new DataInputStream(new BlockLZ4CompressorInputStream(ios))) {
                in.readFully(bytes, 0, decompressionSize);
            }
            ByteBuf out = Unpooled.wrappedBuffer(bytes);
            try {
                return innerCodec.getValueDecoder().decode(out, state);
            } finally {
                out.release();
            }
        }
    };

    /** 编码：内层序列化、写入长度前缀，再 Block LZ4 压缩。 */
    private final Encoder encoder = new Encoder() {

        @Override
        public ByteBuf encode(Object in) throws IOException {
            ByteBuf bytes = null;
            try {
                ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
                bytes = innerCodec.getValueEncoder().encode(in);
                out.writeInt(bytes.readableBytes());
                ByteBufOutputStream baos = new ByteBufOutputStream(out);
                BlockLZ4CompressorOutputStream compressor = new BlockLZ4CompressorOutputStream(baos);
                bytes.getBytes(bytes.readerIndex(), compressor, bytes.readableBytes());
                compressor.close();
                return out;
            } finally {
                if (bytes != null) {
                    bytes.release();
                }
            }
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
