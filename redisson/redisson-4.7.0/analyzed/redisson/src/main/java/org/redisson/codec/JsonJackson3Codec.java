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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 基于 Jackson 3.x 的通用 JSON 编解码器，Redisson 默认 JSON Codec 之一。
 * <p>
 * 支持多态类型（{@code @class} 属性）、UUID MixIn、NON_NULL 等 Redisson 约定配置。
 *
 * @author Nikita Koksharov
 *
 */
public class JsonJackson3Codec extends BaseCodec {

    /** UUID 的 Jackson MixIn：序列化为字符串、反序列化 via fromString。 */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public abstract static class UuidMixin {

        @JsonValue
        public abstract String toString();

        @JsonCreator
        public static UUID fromString(String value) {
            if (value != null) {
                return UUID.fromString(value);
            }
            return null;
        }
    }

    /** 共享单例实例。 */
    public static final JsonJackson3Codec INSTANCE = new JsonJackson3Codec();

    /** 底层 Jackson 3 ObjectMapper。 */
    final ObjectMapper mapObjectMapper;

    /** 序列化任意对象为 JSON 写入 ByteBuf。 */
    private final Encoder encoder = new Encoder() {
        @Override
        public ByteBuf encode(Object in) throws IOException {
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            try {
                ByteBufOutputStream os = new ByteBufOutputStream(out);
                mapObjectMapper.writeValue((OutputStream) os, in);
                return os.buffer();
            } catch (Exception e) {
                out.release();
                throw e;
            }
        }
    };

    /** 反序列化为 Object.class（含多态类型信息）。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), Object.class);
        }
    };

    /**
     * 使用默认 Redisson 配置创建编解码器。
     */
    public JsonJackson3Codec() {
        this.mapObjectMapper = createDefaultMapper();
    }

    /**
     * 使用指定 ClassLoader 创建编解码器。
     *
     * @param classLoader 用于类型解析的类加载器
     */
    public JsonJackson3Codec(ClassLoader classLoader) {
        this.mapObjectMapper = createDefaultMapper(classLoader);
    }

    /**
     * 从已有 Codec 复制 Mapper 并绑定 ClassLoader。
     *
     * @param classLoader 类加载器
     * @param codec 源编解码器
     */
    public JsonJackson3Codec(ClassLoader classLoader, JsonJackson3Codec codec) {
        this(createMapper(classLoader, codec.mapObjectMapper.rebuild().build()));
    }

    /**
     * 使用预配置的 ObjectMapper。
     *
     * @param mapObjectMapper 序列化/反序列化用的 ObjectMapper
     */
    public JsonJackson3Codec(ObjectMapper mapObjectMapper) {
        this.mapObjectMapper = mapObjectMapper;
    }

    /** @param copy 为 true 时对传入 Mapper 执行 rebuild 拷贝 */
    public JsonJackson3Codec(ObjectMapper mapObjectMapper, boolean copy) {
        if (copy) {
            this.mapObjectMapper = mapObjectMapper.rebuild().build();
        } else {
            this.mapObjectMapper = mapObjectMapper;
        }
    }


    /** 配置多态类型嵌入（{@code @class}）与 UUID MixIn。 */
    protected void initTypeInclusion(JsonMapper.Builder builder) {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType(Object.class)
                .build();

        builder.addMixIn(UUID.class, UuidMixin.class);
        builder.activateDefaultTypingAsProperty(typeValidator,
                DefaultTyping.NON_FINAL, "@class");
    }

    /**
     * 创建带 Redisson 标准配置的默认 ObjectMapper。
     *
     * @return 配置完成的 ObjectMapper
     */
    protected ObjectMapper createDefaultMapper() {
        return createDefaultMapper(null);
    }

    /**
     * 创建默认 ObjectMapper，可选绑定 ClassLoader。
     *
     * @param classLoader 类加载器，null 表示使用默认
     * @return 配置完成的 ObjectMapper
     */
    protected ObjectMapper createDefaultMapper(ClassLoader classLoader) {
        TypeFactory typeFactory = TypeFactory.createDefaultInstance();
        if (classLoader != null) {
            typeFactory = typeFactory.withClassLoader(classLoader);
        }

        JsonMapper.Builder b = JsonMapper.builder()
                .typeFactory(typeFactory)
                // Serialization settings
                .changeDefaultPropertyInclusion(incl -> incl
                        .withValueInclusion(JsonInclude.Include.NON_NULL)
                        .withContentInclusion(JsonInclude.Include.NON_NULL))
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

                // Deserialization settings
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // Mapper settings
                .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                // Generator settings - don't close the stream, let Redisson handle it
                .disable(StreamWriteFeature.AUTO_CLOSE_TARGET);

        initTypeInclusion(b);
        return b.build();
    }

    /** 复制已有 Mapper 并可选替换 TypeFactory 的 ClassLoader。 */
    protected static ObjectMapper createMapper(ClassLoader classLoader, ObjectMapper existingMapper) {
        TypeFactory typeFactory = existingMapper.getTypeFactory();
        if (classLoader != null) {
            typeFactory = typeFactory.withClassLoader(classLoader);
        }

        return existingMapper.rebuild()
                .typeFactory(typeFactory)
                .build();
    }

    /**
     * 返回内部 ObjectMapper，供高级定制使用。
     *
     * @return ObjectMapper 实例
     */
    public ObjectMapper getObjectMapper() {
        return mapObjectMapper;
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

    /** 优先返回 Mapper TypeFactory 绑定的 ClassLoader。 */
    @Override
    public ClassLoader getClassLoader() {
        TypeFactory tf = mapObjectMapper.getTypeFactory();
        if (tf.getClassLoader() != null) {
            return tf.getClassLoader();
        }
        return super.getClassLoader();
    }
}
