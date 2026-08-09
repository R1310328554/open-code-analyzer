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
import io.netty.buffer.ByteBufAllocator;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 基于 LZ4 算法的压缩编解码器。
 * <p>
 * 先用内层 {@link Codec} 将对象序列化为二进制，再对字节流进行 LZ4 压缩；
 * 默认内层编解码器为 {@link Kryo5Codec}。编解码器实例完全线程安全。
 * <p>
 * 实现基于 <a href="https://github.com/jpountz/lz4-java">lz4-java</a>。
 *
 * @see org.redisson.codec.Kryo5Codec
 *
 * @author Nikita Koksharov
 *
 */
public class LZ4Codec extends BaseCodec {

    /** 解压前原始长度占用的头部字节数（4 字节 int）。 */
    private static final int DECOMPRESSION_HEADER_SIZE = Integer.SIZE / 8;
    /** 使用性能最优的 LZ4 工厂实例。 */
    private final LZ4Factory factory = LZ4Factory.fastestInstance();

    /** 负责对象与二进制互转的内层编解码器。 */
    private final Codec innerCodec;

    /** 默认使用 {@link Kryo5Codec} 作为内层编解码器。 */
    public LZ4Codec() {
        this(new Kryo5Codec());
    }

    /** @param innerCodec 自定义内层编解码器 */
    public LZ4Codec(Codec innerCodec) {
        this.innerCodec = innerCodec;
    }
    
    /** @param classLoader 用于 Kryo 反序列化的类加载器 */
    public LZ4Codec(ClassLoader classLoader) {
        this(new Kryo5Codec(classLoader));
    }

    /** 在指定类加载器下复制现有编解码器配置。 */
    public LZ4Codec(ClassLoader classLoader, LZ4Codec codec) throws ReflectiveOperationException {
        this(copy(classLoader, codec.innerCodec));
    }
    
    /** 值解码：先读原始长度、LZ4 解压，再委托内层解码器。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            int decompressSize = buf.readInt();
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer(decompressSize);
            try {
                LZ4SafeDecompressor decompressor = factory.safeDecompressor();
                ByteBuffer outBuffer = out.internalNioBuffer(out.writerIndex(), out.writableBytes());
                int pos = outBuffer.position();
                decompressor.decompress(buf.internalNioBuffer(buf.readerIndex(), buf.readableBytes()), outBuffer);
                int compressedLength = outBuffer.position() - pos;
                out.writerIndex(compressedLength);
                return innerCodec.getValueDecoder().decode(out, state);
            } finally {
                out.release();
            }
        }
    };

    /** 值编码：内层序列化后写入原始长度前缀，再 LZ4 压缩。 */
    private final Encoder encoder = new Encoder() {

        @Override
        public ByteBuf encode(Object in) throws IOException {
            ByteBuf bytes = null;
            try {
                LZ4Compressor compressor = factory.fastCompressor();
                bytes = innerCodec.getValueEncoder().encode(in);
                ByteBuffer srcBuf = bytes.internalNioBuffer(bytes.readerIndex(), bytes.readableBytes());
                
                int outMaxLength = compressor.maxCompressedLength(bytes.readableBytes());
                ByteBuf out = ByteBufAllocator.DEFAULT.buffer(outMaxLength + DECOMPRESSION_HEADER_SIZE);
                out.writeInt(bytes.readableBytes());
                ByteBuffer outBuf = out.internalNioBuffer(out.writerIndex(), out.writableBytes());
                int pos = outBuf.position();
                
                compressor.compress(srcBuf, outBuf);
                
                int compressedLength = outBuf.position() - pos;
                out.writerIndex(out.writerIndex() + compressedLength);
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
