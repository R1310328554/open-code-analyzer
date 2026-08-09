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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Jackson 2 泛型 JSON 编解码器，实现 {@link JsonCodec}。
 * <p>
 * 支持 {@code Class<T>} 或 {@link TypeReference} 指定反序列化类型；
 * 默认 NON_NULL、字段 ANY 可见、忽略未知属性；完全线程安全。
 *
 * @author Nikita Koksharov
 *
 */
public class JacksonCodec<T> implements JsonCodec {

    /** JSON 序列化到 ByteBuf。 */
    private final Encoder encoder = new Encoder() {
        @Override
        public ByteBuf encode(Object in) throws IOException {
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            try {
                ByteBufOutputStream os = new ByteBufOutputStream(out);
                mapObjectMapper.writeValue((OutputStream) os, in);
                return os.buffer();
            } catch (IOException e) {
                out.release();
                throw e;
            }
        }
    };

    /** 从 ByteBuf 反序列化为指定类型。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            if (valueClass != null) {
                return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), valueClass);
            }
            return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), valueTypeReference);
        }
    };

    private Class<T> valueClass;
    private TypeReference<T> valueTypeReference;

    private final ObjectMapper mapObjectMapper;

    /** 按 Class 构造并应用默认 init 配置。 */
    public JacksonCodec(Class<T> valueClass) {
        if (valueClass == null) {
            throw new NullPointerException("valueClass isn't defined");
        }
        this.valueClass = valueClass;
        this.mapObjectMapper = new ObjectMapper();
        init(mapObjectMapper);
    }

    /** 按 TypeReference 构造。 */
    public JacksonCodec(TypeReference<T> valueTypeReference) {
        if (valueTypeReference == null) {
            throw new NullPointerException("valueTypeReference isn't defined");
        }
        this.valueTypeReference = valueTypeReference;
        this.mapObjectMapper = new ObjectMapper();
        init(mapObjectMapper);
    }

    /** 使用已有 ObjectMapper 与 TypeReference。 */
    public JacksonCodec(ObjectMapper mapObjectMapper, TypeReference<T> valueTypeReference) {
        if (mapObjectMapper == null) {
            throw new NullPointerException("mapObjectMapper isn't defined");
        }
        if (valueTypeReference == null) {
            throw new NullPointerException("valueTypeReference isn't defined");
        }
        this.mapObjectMapper = mapObjectMapper;
        this.valueTypeReference = valueTypeReference;
    }

    /** 使用已有 ObjectMapper 与 Class。 */
    public JacksonCodec(ObjectMapper mapObjectMapper, Class<T> valueClass) {
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
    public JacksonCodec(ClassLoader classLoader, JacksonCodec<T> codec) {
        this.valueClass = codec.valueClass;
        this.valueTypeReference = codec.valueTypeReference;
        this.mapObjectMapper = createObjectMapper(classLoader, codec.mapObjectMapper.copy());
    }

    /** 为 ObjectMapper 设置带 ClassLoader 的 TypeFactory。 */
    protected static ObjectMapper createObjectMapper(ClassLoader classLoader, ObjectMapper om) {
        TypeFactory tf = TypeFactory.defaultInstance().withClassLoader(classLoader);
        om.setTypeFactory(tf);
        return om;
    }

    /** Redisson 默认 Jackson 2 序列化/反序列化选项。 */
    protected void init(ObjectMapper objectMapper) {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setVisibility(objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
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
