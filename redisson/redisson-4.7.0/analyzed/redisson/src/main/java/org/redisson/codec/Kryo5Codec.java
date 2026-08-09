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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryo.util.Pool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.esotericsoftware.kryo.util.Util.className;

/**
 * Kryo 5 二进制编解码器，使用对象池保证线程安全。
 * <p>
 * 支持类白名单、可选对象引用（{@code useReferences}）、Collections 包装类与 JDK 常用类型的默认序列化器。
 *
 * @author Nikita Koksharov
 *
 */
public class Kryo5Codec extends BaseCodec {

    private static final Logger logger = LoggerFactory.getLogger(Kryo5Codec.class);
    /** Collections 内部包装类名片段，需用 JavaSerializer 处理。 */
    private static final List<String> MISSED_COLLECTION_CLASSES = Arrays.asList("Unmodifiable", "Synchronized", "Checked");

    /** 优先无参构造实例化，失败时回退 Objenesis StdInstantiatorStrategy。 */
    private static final class SimpleInstantiatorStrategy implements org.objenesis.strategy.InstantiatorStrategy {

        private final StdInstantiatorStrategy ss = new StdInstantiatorStrategy();

        @Override
        public <T> ObjectInstantiator<T> newInstantiatorOf(Class<T> type) {
            // Reflection.
            try {
                Constructor ctor;
                try {
                    ctor = type.getConstructor((Class[]) null);
                } catch (Exception ex) {
                    ctor = type.getDeclaredConstructor((Class[]) null);
                    ctor.setAccessible(true);
                }
                final Constructor constructor = ctor;
                return (ObjectInstantiator) () -> {
                    try {
                        return constructor.newInstance();
                    } catch (Exception ex) {
                        throw new KryoException("Error constructing instance of class: " + className(type), ex);
                    }
                };
            } catch (Exception ignored) {
            }

            return ss.newInstantiatorOf(type);
        }
    }

    /** Kryo 实例池（最大 1024）。 */
    private final Pool<Kryo> kryoPool;
    /** Input 缓冲池。 */
    private final Pool<Input> inputPool;
    /** Output 缓冲池。 */
    private final Pool<Output> outputPool;
    /** 允许序列化的类名；非空时 requireRegistration。 */
    private final Set<String> allowedClasses;
    /** 是否启用 Kryo 对象引用图。 */
    private final boolean useReferences;

    /** 默认：无白名单、不启用引用。 */
    public Kryo5Codec() {
        this(null, Collections.emptySet(), false);
    }

    public Kryo5Codec(Set<String> allowedClasses, boolean useReferences) {
        this(null, allowedClasses, useReferences);
    }

    /** 从已有 Codec 复制配置并绑定 ClassLoader。 */
    public Kryo5Codec(ClassLoader classLoader, Kryo5Codec codec) {
        this(classLoader, codec.allowedClasses, codec.useReferences);
    }

    public Kryo5Codec(ClassLoader classLoader) {
        this(classLoader, Collections.emptySet(), false);
    }

    /**
     * 初始化三个对象池并保存白名单与引用选项。
     *
     * @param classLoader 类加载器
     * @param allowedClasses 允许序列化的类全限定名
     * @param useReferences 是否启用 Kryo 引用
     */
    public Kryo5Codec(ClassLoader classLoader, Set<String> allowedClasses, boolean useReferences) {
        this.allowedClasses = allowedClasses.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
        this.useReferences = useReferences;

        this.kryoPool = new Pool<Kryo>(true, false, 1024) {
            @Override
            protected Kryo create() {
                try {
                    return createKryo(classLoader, useReferences);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };

        this.inputPool = new Pool<Input>(true, false, 512) {
            @Override
            protected Input create() {
                return new Input(8192) {
                    @Override
                    public void reset() {
                        super.reset();
                        if (chars != null && chars.length > capacity) {
                            chars = new char[capacity];
                        }
                    }
                };
            }
        };

        this.outputPool = new Pool<Output>(true, false, 512) {
            @Override
            protected Output create() {
                return new Output(8192, -1);
            }
        };
    }

    /**
     * 创建并配置 Kryo 实例：实例化策略、默认序列化器、白名单注册等。
     *
     * @param classLoader 类加载器
     * @param useReferences 是否启用引用
     */
    protected Kryo createKryo(ClassLoader classLoader, boolean useReferences) throws ClassNotFoundException {
        Kryo kryo = new Kryo();
        if (classLoader != null) {
            kryo.setClassLoader(classLoader);
        }
        kryo.setInstantiatorStrategy(new SimpleInstantiatorStrategy());
        kryo.setRegistrationRequired(!allowedClasses.isEmpty());
        kryo.setReferences(useReferences);

        for (String allowedClass : allowedClasses) {
            kryo.register(Class.forName(allowedClass));
        }

        try {
            Class<?>[] f = Collections.class.getDeclaredClasses();
            Arrays.stream(f)
                    .filter(cls -> MISSED_COLLECTION_CLASSES.stream().anyMatch(s -> cls.getName().contains(s)))
                    .forEach(cls -> kryo.addDefaultSerializer(cls, new JavaSerializer()));
        } catch (Exception e) {
            logger.warn("Unable to register Collections serializer", e);
        }
        kryo.addDefaultSerializer(EnumMap.class, new JavaSerializer());
        kryo.addDefaultSerializer(Throwable.class, new JavaSerializer());
        kryo.addDefaultSerializer(UUID.class, new DefaultSerializers.UUIDSerializer());
        kryo.addDefaultSerializer(URI.class, new DefaultSerializers.URISerializer());
        kryo.addDefaultSerializer(Pattern.class, new DefaultSerializers.PatternSerializer());
        kryo.addDefaultSerializer(SocketAddress.class, new JavaSerializer());
        kryo.addDefaultSerializer(InetAddress.class, new JavaSerializer());
        kryo.addDefaultSerializer(AtomicBoolean.class, new DefaultSerializers.AtomicBooleanSerializer());
        kryo.addDefaultSerializer(AtomicInteger.class, new DefaultSerializers.AtomicIntegerSerializer());
        kryo.addDefaultSerializer(AtomicLong.class, new DefaultSerializers.AtomicLongSerializer());
        kryo.addDefaultSerializer(AtomicReference.class, new DefaultSerializers.AtomicReferenceSerializer());
        // once kryo5 releases a new version, this serializer will be included in kryo5s DefaultSerializers
        kryo.addDefaultSerializer(ConcurrentHashMap.KeySetView.class, new Kryo5KeySetViewSerializer());
        return kryo;
    }

    /** 从池中借 Kryo/Input，readClassAndObject 后归还。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            Kryo kryo = kryoPool.obtain();
            Input input = inputPool.obtain();
            boolean success = false;
            try {
                input.setInputStream(new ByteBufInputStream(buf));
                Object result = kryo.readClassAndObject(input);
                success = true;
                return result;
            } finally {
                kryoPool.free(kryo);
                if (success) {
                    inputPool.free(input);
                }
            }
        }
    };

    /** 从池中借 Kryo/Output，writeClassAndObject 写入 ByteBuf。 */
    private final Encoder encoder = new Encoder() {
        @Override
        @SuppressWarnings("IllegalCatch")
        public ByteBuf encode(Object in) throws IOException {
            Kryo kryo = kryoPool.obtain();
            Output output = outputPool.obtain();
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            try {
                ByteBufOutputStream baos = new ByteBufOutputStream(out);
                output.setOutputStream(baos);
                kryo.writeClassAndObject(output, in);
                output.flush();
                return baos.buffer();
            } catch (RuntimeException e) {
                out.release();
                throw e;
            } finally {
                kryoPool.free(kryo);
                outputPool.free(output);
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
