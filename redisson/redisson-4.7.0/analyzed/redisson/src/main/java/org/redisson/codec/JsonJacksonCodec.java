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

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 基于 Jackson 2 的通用 JSON 编解码器。
 * https://github.com/FasterXML/jackson
 * <p>
 * 支持多态 {@code @class}、Throwable/UUID MixIn、Long 精度保护等；完全线程安全。
 *
 * @see org.redisson.codec.CborJacksonCodec
 * @see org.redisson.codec.MsgPackJacksonCodec
 *
 * @author Nikita Koksharov
 *
 */
public class JsonJacksonCodec extends BaseCodec {

    /** 共享单例。 */
    public static final JsonJacksonCodec INSTANCE = new JsonJacksonCodec();

    /** Throwable 序列化 MixIn：使用 {@code @id} 避免循环引用栈溢出。 */
    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
    @JsonAutoDetect(fieldVisibility = Visibility.NON_PRIVATE,
                    getterVisibility = Visibility.PUBLIC_ONLY, 
                    setterVisibility = Visibility.NONE, 
                    isGetterVisibility = Visibility.NONE)
    public static class ThrowableMixIn {
        
    }

    /** UUID MixIn：字符串形式读写。 */
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
    
    /** 底层 ObjectMapper。 */
    protected final ObjectMapper mapObjectMapper;

    /** JSON 编码到 ByteBuf。 */
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
            } catch (Exception e) {
                out.release();
                throw new IOException(e);
            }
        }
    };

    /** 反序列化为 Object（含默认类型信息）。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), Object.class);
        }
    };
    
    /** 默认空 ObjectMapper 并 init。 */
    public JsonJacksonCodec() {
        this(new ObjectMapper());
    }
    
    /** 按 ClassLoader 创建 Mapper。 */
    public JsonJacksonCodec(ClassLoader classLoader) {
        this(createObjectMapper(classLoader, new ObjectMapper()));
    }

    /** 从已有 Codec 复制 Mapper 并绑定 ClassLoader。 */
    public JsonJacksonCodec(ClassLoader classLoader, JsonJacksonCodec codec) {
        this(createObjectMapper(classLoader, codec.mapObjectMapper.copy()));
    }

    /** 是否已完成 JVM 预热（避免首包慢）。 */
    private static boolean warmedup = false;

    /** 首次使用时执行一次编解码预热。 */
    private void warmup() {
        if (getValueEncoder() == null || getValueDecoder() == null || warmedup) {
            return;
        }
        warmedup = true;

        ByteBuf d = null;
        try {
            d = getValueEncoder().encode("testValue");
            getValueDecoder().decode(d, null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (d != null) {
                d.release();
            }
        }
    }

    /** 为 Mapper 设置带 ClassLoader 的 TypeFactory。 */
    protected static ObjectMapper createObjectMapper(ClassLoader classLoader, ObjectMapper om) {
        TypeFactory tf = om.getTypeFactory().withClassLoader(classLoader);
        om.setTypeFactory(tf);
        return om;
    }

    /** 拷贝 Mapper、init 并 warmup。 */
    public JsonJacksonCodec(ObjectMapper mapObjectMapper) {
        this(mapObjectMapper, true);
        warmup();
    }

    /** @param copy 是否 copy ObjectMapper */
    public JsonJacksonCodec(ObjectMapper mapObjectMapper, boolean copy) {
        if (copy) {
            this.mapObjectMapper = mapObjectMapper.copy();
        } else {
            this.mapObjectMapper = mapObjectMapper;
        }
        init(this.mapObjectMapper);
        initTypeInclusion(this.mapObjectMapper);
        warmup();
    }

    /**
     * 配置 Jackson 2 默认多态类型解析：NON_FINAL 类型写入 {@code @class}；
     * Long 强制保留类型信息；XMLGregorianCalendar 排除在外。
     */
    protected void initTypeInclusion(ObjectMapper mapObjectMapper) {
        mapObjectMapper.addMixIn(UUID.class, UuidMixin.class);
        TypeResolverBuilder<?> mapTyper = new DefaultTypeResolverBuilder(DefaultTyping.NON_FINAL) {
            public boolean useForType(JavaType t) {
                switch (_appliesFor) {
                case NON_CONCRETE_AND_ARRAYS:
                    while (t.isArrayType()) {
                        t = t.getContentType();
                    }
                    // fall through
                case OBJECT_AND_NON_CONCRETE:
                    return t.getRawClass() == Object.class || !t.isConcrete();
                case NON_FINAL:
                    while (t.isArrayType()) {
                        t = t.getContentType();
                    }
                    // to fix problem with wrong long to int conversion
                    if (t.getRawClass() == Long.class) {
                        return true;
                    }
                    if (t.getRawClass() == XMLGregorianCalendar.class) {
                        return false;
                    }
                    return !t.isFinal(); // includes Object.class
                default:
                    // case JAVA_LANG_OBJECT:
                    return t.getRawClass() == Object.class;
                }
            }
        };
        mapTyper.init(JsonTypeInfo.Id.CLASS, null);
        mapTyper.inclusion(JsonTypeInfo.As.PROPERTY);
        mapObjectMapper.setDefaultTyping(mapTyper);
    }

    /** Redisson 标准 Jackson 2 序列化选项与 Throwable MixIn。 */
    protected void init(ObjectMapper objectMapper) {
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setVisibility(objectMapper.getSerializationConfig()
                                                    .getDefaultVisibilityChecker()
                                                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                                                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                                                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.enable(Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        objectMapper.addMixIn(Throwable.class, ThrowableMixIn.class);
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
        if (mapObjectMapper.getTypeFactory().getClassLoader() != null) {
            return mapObjectMapper.getTypeFactory().getClassLoader();
        }

        return super.getClassLoader();
    }

    /** 暴露内部 ObjectMapper。 */
    public ObjectMapper getObjectMapper() {
        return mapObjectMapper;
    }
}
