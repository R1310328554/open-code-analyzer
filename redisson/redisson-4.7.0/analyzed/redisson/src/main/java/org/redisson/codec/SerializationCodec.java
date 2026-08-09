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
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

/**
 * 基于 JDK 原生 {@link java.io.Serializable} 的序列化编解码器。
 * <p>
 * 使用 {@link ObjectOutputStream} / {@link ObjectInputStream} 读写对象，
 * 支持指定 {@link ClassLoader} 与允许反序列化的类白名单，完全线程安全。
 *
 * @author Nikita Koksharov
 *
 */
public class SerializationCodec extends BaseCodec {

    /** 反序列化：必要时切换线程上下文类加载器并应用白名单校验。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            try {
                //set thread context class loader to be the classLoader variable as there could be reflection
                //done while reading from input stream which reflection will use thread class loader to load classes on demand
                ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
                try {
                    ByteBufInputStream in = new ByteBufInputStream(buf);
                    ObjectInputStream inputStream;
                    if (classLoader != null) {
                        Thread.currentThread().setContextClassLoader(classLoader);
                        inputStream = new CustomObjectInputStream(classLoader, in, allowedClasses);
                    } else {
                        inputStream = new ObjectInputStream(in);
                    }
                    return inputStream.readObject();
                } finally {
                    Thread.currentThread().setContextClassLoader(currentThreadClassLoader);
                }
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    };

    /** 序列化：将对象写入 ObjectOutputStream 并输出 ByteBuf。 */
    private final Encoder encoder = in -> {
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        try {
            ByteBufOutputStream result = new ByteBufOutputStream(out);
            ObjectOutputStream outputStream = new ObjectOutputStream(result);
            outputStream.writeObject(in);
            outputStream.close();
            return result.buffer();
        } catch (IOException e) {
            out.release();
            throw e;
        }
    };

    /** 允许反序列化的类名白名单，null 表示不限制。 */
    private Set<String> allowedClasses;
    /** 反序列化使用的类加载器。 */
    private final ClassLoader classLoader;

    /** 使用默认类加载器。 */
    public SerializationCodec() {
        this(null);
    }
    
    /** @param classLoader 反序列化类加载器 */
    public SerializationCodec(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /** 复制现有编解码器的类加载器与白名单配置。 */
    public SerializationCodec(ClassLoader classLoader, SerializationCodec codec) {
        this.classLoader = classLoader;
        this.allowedClasses = codec.allowedClasses;
    }

    /** @param allowedClasses 反序列化类白名单 */
    public SerializationCodec(ClassLoader classLoader, Set<String> allowedClasses) {
        this.classLoader = classLoader;
        this.allowedClasses = allowedClasses;
    }
    
    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }
    
    @Override
    public ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        return getClass().getClassLoader();
    }

}
