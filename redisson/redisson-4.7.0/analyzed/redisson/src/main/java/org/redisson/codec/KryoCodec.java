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
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Kryo 4 二进制编解码器，通过 {@link ConcurrentLinkedQueue} 复用 Kryo 实例实现线程安全。
 * <p>
 * 禁用 Kryo 引用（{@code setReferences(false)}）；可预注册指定 Class 列表。
 *
 * @author Nikita Koksharov
 *
 */
public class KryoCodec extends BaseCodec {

    /** 包装底层异常并保留原始堆栈的 RuntimeException。 */
    public class RedissonKryoCodecException extends RuntimeException {

        private static final long serialVersionUID = 9172336149805414947L;

        public RedissonKryoCodecException(Throwable cause) {
            super(cause.getMessage(), cause);
            setStackTrace(cause.getStackTrace());
        }
    }

    /** Kryo 实例复用队列。 */
    private final Queue<Kryo> objects = new ConcurrentLinkedQueue<>();
    /** 构造时预注册的 Class 列表。 */
    private final List<Class<?>> classes;
    /** 可选 ClassLoader。 */
    private final ClassLoader classLoader;

    /** 从 ByteBuf 反序列化；异常时包装为 RedissonKryoCodecException。 */
    private final Decoder<Object> decoder = (buf, state) -> {
        Kryo kryo = null;
        try {
            kryo = get();
            return kryo.readClassAndObject(new Input(new ByteBufInputStream(buf)));
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RedissonKryoCodecException(e);
        } finally {
            if (kryo != null) {
                offer(kryo);
            }
        }
    };

    /** 序列化到 ByteBuf；失败时 release 缓冲区。 */
    private final Encoder encoder = in -> {
        Kryo kryo = null;
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        try {
            ByteBufOutputStream baos = new ByteBufOutputStream(out);
            Output output = new Output(baos);
            kryo = get();
            kryo.writeClassAndObject(output, in);
            output.close();
            return baos.buffer();
        } catch (Exception e) {
            out.release();
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RedissonKryoCodecException(e);
        } finally {
            if (kryo != null) {
                offer(kryo);
            }
        }
    };

    /** 无预注册类、默认 ClassLoader。 */
    public KryoCodec() {
        this(Collections.<Class<?>>emptyList());
    }

    public KryoCodec(ClassLoader classLoader) {
        this(Collections.<Class<?>>emptyList(), classLoader);
    }
    
    /** 复制预注册类列表并绑定 ClassLoader。 */
    public KryoCodec(ClassLoader classLoader, KryoCodec codec) {
        this(codec.classes, classLoader);
    }
    
    public KryoCodec(List<Class<?>> classes) {
        this(classes, null);
    }

    public KryoCodec(List<Class<?>> classes, ClassLoader classLoader) {
        this.classes = classes;
        this.classLoader = classLoader;
    }

    /** 从队列取出 Kryo，空则 createInstance。 */
    public Kryo get() {
        Kryo kryo = objects.poll();
        if (kryo == null) {
            kryo = createInstance(classes, classLoader);
        }
        return kryo;
    }

    /** 使用完毕后归还 Kryo 到队列。 */
    public void offer(Kryo kryo) {
        objects.offer(kryo);
    }

    /**
     * 子类可覆盖以自定义 Kryo 配置。
     *
     * @return 新创建的 Kryo 实例
     */
    protected Kryo createInstance(List<Class<?>> classes, ClassLoader classLoader) {
        Kryo kryo = new Kryo();
        if (classLoader != null) {
            kryo.setClassLoader(classLoader);
        }
        kryo.setReferences(false);
        for (Class<?> clazz : classes) {
            kryo.register(clazz);
        }
        return kryo;
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
        return super.getClassLoader();
    }

}
