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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.TypeFactory;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Jackson 3 泛型 JSON 编解码器，实现 {@link JsonCodec}。
 * <p>
 * 通过 {@code Class<T>} 或 {@link TypeReference} 指定反序列化目标类型；
 * 默认忽略 null 字段、未知属性，字段可见性 ANY、getter/setter 隐藏；完全线程安全。
 *
 * @author Nikita Koksharov
 *
 */
public class Jackson3Codec<T> implements JsonCodec {

    /** 将对象写入 ByteBuf 输出流。 */
    private final Encoder encoder = new Encoder() {
        @Override
        public ByteBuf encode(Object in) {
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            try {
                ByteBufOutputStream os = new ByteBufOutputStream(out);
                mapObjectMapper.writeValue((OutputStream) os, in);
                return os.buffer();
            } catch (JacksonException e) {
                out.release();
                throw e;
            }
        }
    };

    /** 按 valueClass 或 valueTypeReference 从 ByteBuf 反序列化。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) {
            if (valueClass != null) {
                return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), valueClass);
            }
            return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), valueTypeReference);
        }
    };

    /** 反序列化目标 Class；与 valueTypeReference 二选一。 */
    private Class<T> valueClass;
    private TypeReference<T> valueTypeReference;

    /** 共享的 Jackson 3 ObjectMapper。 */
    private final ObjectMapper mapObjectMapper;

    /** 指定具体类型 Class 构造。 */
    public Jackson3Codec(Class<T> valueClass) {
        if (valueClass == null) {
            throw new NullPointerException("valueClass isn't defined");
        }
        this.valueClass = valueClass;
        this.mapObjectMapper = createDefaultMapper();
    }

    /** 指定泛型 TypeReference 构造。 */
    public Jackson3Codec(TypeReference<T> valueTypeReference) {
        if (valueTypeReference == null) {
            throw new NullPointerException("valueTypeReference isn't defined");
        }
        this.valueTypeReference = valueTypeReference;
        this.mapObjectMapper = createDefaultMapper();
    }

    /** 使用外部 ObjectMapper 与 TypeReference。 */
    public Jackson3Codec(ObjectMapper mapObjectMapper, TypeReference<T> valueTypeReference) {
        if (mapObjectMapper == null) {
            throw new NullPointerException("mapObjectMapper isn't defined");
        }
        if (valueTypeReference == null) {
            throw new NullPointerException("valueTypeReference isn't defined");
        }
        this.mapObjectMapper = mapObjectMapper;
        this.valueTypeReference = valueTypeReference;
    }

    /** 使用外部 ObjectMapper 与 Class。 */
    public Jackson3Codec(ObjectMapper mapObjectMapper, Class<T> valueClass) {
        if (mapObjectMapper == null) {
            throw new NullPointerException("mapObjectMapper isn't defined");
        }
        if (valueClass == null) {
            throw new NullPointerException("valueClass isn't defined");
        }
        this.mapObjectMapper = mapObjectMapper;
        this.valueClass = valueClass;
    }

    /** 复制类型信息并按 ClassLoader 重建 Mapper。 */
    public Jackson3Codec(ClassLoader classLoader, Jackson3Codec<T> codec) {
        this.valueClass = codec.valueClass;
        this.valueTypeReference = codec.valueTypeReference;
        this.mapObjectMapper = createObjectMapper(classLoader, codec.mapObjectMapper.rebuild().build());
    }

    /** 为 Mapper 绑定指定 ClassLoader 的 TypeFactory。 */
    protected static ObjectMapper createObjectMapper(ClassLoader classLoader, ObjectMapper sourceMapper) {
        TypeFactory tf = TypeFactory.createDefaultInstance().withClassLoader(classLoader);
        return sourceMapper.rebuild()
                .typeFactory(tf)
                .build();
    }

    /** 创建带 Redisson 默认选项的 JsonMapper。 */
    ObjectMapper createDefaultMapper() {
        return init(JsonMapper.builder()).build();
    }

    /**
     * 配置 Redisson 默认 Jackson 3 选项：NON_NULL、字段可见、忽略未知属性等。
     *
     * @param builder JsonMapper 构建器
     */
    protected JsonMapper.Builder init(JsonMapper.Builder builder) {
        return builder.changeDefaultPropertyInclusion(incl -> incl
                            .withValueInclusion(JsonInclude.Include.NON_NULL)
                            .withContentInclusion(JsonInclude.Include.NON_NULL))
                      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

                      .changeDefaultVisibility(vc -> vc
                           .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                           .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                           .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                           .withCreatorVisibility(JsonAutoDetect.Visibility.NONE))
                      .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                      .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    }

    @Override
    public Encoder getEncoder() {
        return encoder;
    }

    @Override
    public Decoder<Object> getDecoder() {
        return decoder;
    }
}
