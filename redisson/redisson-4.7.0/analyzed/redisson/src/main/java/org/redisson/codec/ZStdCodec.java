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

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import io.netty.buffer.*;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;

/**
 * ZStandard（Zstd）压缩编解码器。
 * <p>先用内部 {@link Codec} 将对象序列化为二进制，再经 Zstd 压缩写入 Redis；
 * 解码时先解压再委托内部编解码器。默认内部编解码器为 {@link Kryo5Codec}。
 * <p>基于 <a href="https://github.com/luben/zstd-jni">zstd-jni</a>，完全线程安全。
 *
 * @see Kryo5Codec
 *
 * @author Nikita Koksharov
 *
 */
public class ZStdCodec extends BaseCodec {

    /** 内层编解码器，负责对象与字节的互转。 */
    private final Codec innerCodec;

    /** 使用默认 {@link Kryo5Codec} 构造。 */
    public ZStdCodec() {
        this(new Kryo5Codec());
    }

    /** @param innerCodec 内层编解码器 */
    public ZStdCodec(Codec innerCodec) {
        this.innerCodec = innerCodec;
    }

    /** @param classLoader 传递给内层 Kryo 编解码器的类加载器 */
    public ZStdCodec(ClassLoader classLoader) {
        this(new Kryo5Codec(classLoader));
    }

    /** 复制已有编解码器并绑定新的类加载器。 */
    public ZStdCodec(ClassLoader classLoader, ZStdCodec codec) throws ReflectiveOperationException {
        this(copy(classLoader, codec.innerCodec));
    }
    
    /** 解码：读压缩前原始长度 → Zstd 解压 → 内层解码。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            // 压缩前原始字节长度
            int size = buf.readInt();
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer(size);

            try {
                ZstdInputStream in = new ZstdInputStream(new ByteBufInputStream(buf));
                out.writeBytes(in, size);
                in.close();

                return innerCodec.getValueDecoder().decode(out, state);
            } finally {
                out.release();
            }
        }
    };

    /** 编码：内层序列化 → 写入原始长度 → Zstd 压缩。 */
    private final Encoder encoder = new Encoder() {

        @Override
        public ByteBuf encode(Object in) throws IOException {
            ByteBuf encoded = innerCodec.getValueEncoder().encode(in);

            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            ZstdOutputStream o = new ZstdOutputStream(new ByteBufOutputStream(out));

            int size = encoded.readableBytes();
            out.writeInt(size);
            encoded.readBytes(o, size);
            encoded.release();

            o.flush();
            o.close();
            return out;
        }
    };

    /** 返回值解码器。 */
    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    /** 返回值编码器。 */
    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }
    
}
