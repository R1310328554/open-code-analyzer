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

import java.util.Arrays;
import java.util.List;

import org.redisson.cache.LocalCachedMessageCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.codec.ProtobufCodec;
import org.redisson.jcache.JCacheEventCodec;

/**
 * {@link Codec} 抽象基类，提供 Map 键值编解码的默认委托实现。
 * <p>
 * 内置 {@link #SKIPPED_CODECS} 列表，用于 {@link #copy} 时跳过无需克隆的单例编解码器。
 *
 * @author Nikita Koksharov
 *
 */
public abstract class BaseCodec implements Codec {

    /** 复制 {@link Codec} 时直接返回原实例、无需反射克隆的类型列表。 */
    public static final List<Class<?>> SKIPPED_CODECS = Arrays.asList(
            StringCodec.class, ByteArrayCodec.class, LocalCachedMessageCodec.class, BitSetCodec.class,
            JCacheEventCodec.class, LongCodec.class, IntegerCodec.class, ProtobufCodec.class);
    
    /**
     * 按目标 {@link ClassLoader} 复制编解码器实例。
     * <p>
     * 若类型在 {@link #SKIPPED_CODECS} 中则原样返回，否则调用 {@code (ClassLoader, Codec)} 构造器。
     *
     * @param classLoader 目标类加载器
     * @param codec 待复制的编解码器
     * @return 适配新类加载器的编解码器
     * @throws ReflectiveOperationException 反射实例化失败
     */
    public static <T> T copy(ClassLoader classLoader, T codec) throws ReflectiveOperationException {
        if (codec == null) {
            return codec;
        }

        for (Class<?> clazz : SKIPPED_CODECS) {
            if (clazz.isAssignableFrom(codec.getClass())) {
                return codec;
            }
        }

        return (T) codec.getClass().getConstructor(ClassLoader.class, codec.getClass()).newInstance(classLoader, codec);
    }
    
    /** Map 值解码器默认委托 {@link #getValueDecoder()}。 */
    @Override
    public Decoder<Object> getMapValueDecoder() {
        return getValueDecoder();
    }

    /** Map 值编码器默认委托 {@link #getValueEncoder()}。 */
    @Override
    public Encoder getMapValueEncoder() {
        return getValueEncoder();
    }

    /** Map 键解码器默认委托 {@link #getValueDecoder()}。 */
    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return getValueDecoder();
    }

    /** Map 键编码器默认委托 {@link #getValueEncoder()}。 */
    @Override
    public Encoder getMapKeyEncoder() {
        return getValueEncoder();
    }

    /** 返回当前编解码器实现类的类加载器。 */
    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    /** 返回编解码器实现类的全限定名。 */
    @Override
    public String toString() {
        return getClass().getName();
    }
    
}
