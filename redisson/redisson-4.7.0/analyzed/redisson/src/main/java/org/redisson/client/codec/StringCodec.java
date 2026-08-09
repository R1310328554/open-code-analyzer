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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.CharsetUtil;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.codec.JsonCodec;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 字符串 Redis 编解码器，实现 {@link JsonCodec}，默认使用 UTF-8。
 * <p>
 * 编码时将对象 {@code toString()} 后写入字节；解码时读取全部可读字节为字符串。
 *
 * @author Nikita Koksharov
 *
 */
public class StringCodec extends BaseCodec implements JsonCodec {

    /** 默认 UTF-8 单例实例。 */
    public static final StringCodec INSTANCE = new StringCodec();

    private final Charset charset;

    private final Encoder encoder = new Encoder() {
        @Override
        public ByteBuf encode(Object in) throws IOException {
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            out.writeCharSequence(in.toString(), charset);
            return out;
        }
    };

    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) {
            String str = buf.toString(charset);
            buf.readerIndex(buf.readableBytes());
            return str;
        }
    };

    /** 使用 UTF-8 字符集构造。 */
    public StringCodec() {
        this(CharsetUtil.UTF_8);
    }
    
    /** 兼容构造，忽略 classLoader 并委托默认 UTF-8 构造。 */
    public StringCodec(ClassLoader classLoader) {
        this();
    }

    /** 按字符集名称构造。 */
    public StringCodec(String charsetName) {
        this(Charset.forName(charsetName));
    }

    /** 指定 {@link Charset} 构造。 */
    public StringCodec(Charset charset) {
        this.charset = charset;
    }

    /** 返回字符串值解码器。 */
    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    /** 返回值编码器。 */
    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

    /** {@link JsonCodec} 通用编码器，与值编码器相同。 */
    @Override
    public Encoder getEncoder() {
        return encoder;
    }

    /** {@link JsonCodec} 通用解码器，与值解码器相同。 */
    @Override
    public Decoder<Object> getDecoder() {
        return decoder;
    }
}
