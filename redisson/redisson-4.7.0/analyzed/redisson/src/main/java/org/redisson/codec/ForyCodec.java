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
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.ForyBuilder;
import org.apache.fory.config.Language;
import org.apache.fory.io.ForyStreamReader;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.memory.MemoryUtils;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * 基于 <a href="https://github.com/apache/fory">Apache Fory</a> 的高性能二进制编解码器。
 * <p>
 * 线程安全（{@link ThreadSafeFory}）；可选类白名单与 {@link Language}（默认 Java）。
 * 序列化/反序列化优先零拷贝写入 Netty {@link ByteBuf} 的堆内存或 NIO 缓冲区。
 *
 * @author Nikita Koksharov
 *
 */
public class ForyCodec extends BaseCodec {

    /** 线程安全的 Fory 实例。 */
    private final ThreadSafeFory fory;
    /** 允许序列化的类全限定名；非空时启用类注册限制。 */
    private final Set<String> allowedClasses;
    /** 序列化语言模式。 */
    private final Language language;

    /** 默认 Java 语言、无类白名单。 */
    public ForyCodec() {
        this(null, Collections.emptySet(), Language.JAVA);
    }

    /** 指定类白名单。 */
    public ForyCodec(Set<String> allowedClasses) {
        this(null, allowedClasses, Language.JAVA);
    }

    /** 指定序列化语言。 */
    public ForyCodec(Language language) {
        this(null, Collections.emptySet(), language);
    }

    public ForyCodec(Set<String> allowedClasses, Language language) {
        this(null, allowedClasses, language);
    }

    /** 从已有 Codec 复制配置并应用新 ClassLoader。 */
    public ForyCodec(ClassLoader classLoader, ForyCodec codec) {
        this(classLoader, codec.allowedClasses, codec.language);
    }

    public ForyCodec(ClassLoader classLoader) {
        this(classLoader, Collections.emptySet(), Language.JAVA);
    }

    /**
     * 完整构造：配置 ClassLoader、白名单与语言，并预注册白名单中的类。
     *
     * @param classLoader 类加载器，可为 null
     * @param allowedClasses 允许序列化的类名集合
     * @param language Fory 语言模式
     */
    public ForyCodec(ClassLoader classLoader, Set<String> allowedClasses, Language language) {
        this.allowedClasses = allowedClasses;
        this.language = language;

        ForyBuilder builder = Fory.builder();
        if (classLoader != null) {
            builder.withClassLoader(classLoader);
        }
        builder.withLanguage(language);
        builder.requireClassRegistration(!allowedClasses.isEmpty());
        fory = create(builder);

        for (String allowedClass : allowedClasses) {
            try {
                fory.register(Class.forName(allowedClass));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /** 子类可覆盖以自定义 Fory 构建方式。 */
    protected ThreadSafeFory create(ForyBuilder builder) {
        return builder.buildThreadSafeFory();
    }

    /** 反序列化：单 NIO 缓冲区时零拷贝，否则走流式读取。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            if (buf.nioBufferCount() == 1) {
                MemoryBuffer furyBuffer = MemoryUtils.wrap(buf.nioBuffer());
                try {
                    return fory.deserialize(furyBuffer);
                } finally {
                    buf.readerIndex(buf.readerIndex() + furyBuffer.readerIndex());
                }
            } else {
                return fory.deserialize(ForyStreamReader.of(new ByteBufInputStream(buf)));
            }
        }
    };

    /** 序列化：尽量直接写入 ByteBuf 底层数组或 NIO 缓冲区，否则回退到 OutputStream。 */
    private final Encoder encoder = new Encoder() {
        @Override
        public ByteBuf encode(Object in) throws IOException {
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            MemoryBuffer furyBuffer = null;
            int remainingSize = out.capacity() - out.writerIndex();
            if (out.hasArray()) {
                furyBuffer = MemoryUtils.wrap(out.array(), out.arrayOffset() + out.writerIndex(),
                  remainingSize);
            } else if (out.nioBufferCount() == 1) {
                furyBuffer = MemoryUtils.wrap(out.nioBuffer(out.writerIndex(), remainingSize));
            }
            if (furyBuffer != null) {
                int size = furyBuffer.size();
                fory.serialize(furyBuffer, in);
                if (furyBuffer.size() > size) {
                    out.writeBytes(furyBuffer.getHeapMemory(), 0, furyBuffer.size());
                } else {
                    out.writerIndex(out.writerIndex() + furyBuffer.writerIndex());
                }
                return out;
            } else {
                try {
                    ByteBufOutputStream baos = new ByteBufOutputStream(out);
                    fory.serialize(baos, in);
                    return baos.buffer();
                } catch (Exception e) {
                    out.release();
                    throw e;
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
