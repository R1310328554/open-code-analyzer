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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

/**
 * Jackson 2 强类型 JSON 编解码器。
 * <p>
 * 编码时不写入 {@code @class} 类型信息字段，解码时依赖构造时绑定的
 * {@link Class} 或 {@link TypeReference}，适用于已知类型的 JSON 存储。
 * 
 * @author Nikita Koksharov
 * @author Andrej Kazakov
 *
 */
public class TypedJsonJacksonCodec extends JsonJacksonCodec {
    
    /** 值/Map 键值共用的 JSON 编码器，不含类型元数据。 */
    private final Encoder encoder = in -> {
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
    };
    
    /** 按 Class 或 TypeReference 创建强类型解码器。 */
    private Decoder<Object> createDecoder(final Class<?> valueClass, final TypeReference<?> valueTypeReference) {
        return (buf, state) -> {
            if (valueClass != null) {
                return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), valueClass);
            }
            if (valueTypeReference != null) {
                return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), valueTypeReference);
            }
            return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), Object.class);
        };
    }
    
    /** 值解码器。 */
    private final Decoder<Object> valueDecoder;
    /** Map 值解码器。 */
    private final Decoder<Object> mapValueDecoder;
    /** Map 键解码器。 */
    private final Decoder<Object> mapKeyDecoder;
    
    /** 值 TypeReference 绑定。 */
    private final TypeReference<?> valueTypeReference;
    /** Map 键 TypeReference 绑定。 */
    private final TypeReference<?> mapKeyTypeReference;
    /** Map 值 TypeReference 绑定。 */
    private final TypeReference<?> mapValueTypeReference;
    
    /** 值 Class 绑定。 */
    private final Class<?> valueClass;
    /** Map 键 Class 绑定。 */
    private final Class<?> mapKeyClass; 
    /** Map 值 Class 绑定。 */
    private final Class<?> mapValueClass;

    /** @param valueClass 值类型 */
    public TypedJsonJacksonCodec(Class<?> valueClass) {
        this(null, null, null,
                valueClass, null, null, new ObjectMapper(), false);
    }

    /** @param valueClass 值类型 @param mapper 自定义 ObjectMapper */
    public TypedJsonJacksonCodec(Class<?> valueClass, ObjectMapper mapper) {
        this(valueClass, null, null, mapper);
    }
    
    /** @param mapKeyClass Map 键类型 @param mapValueClass Map 值类型 */
    public TypedJsonJacksonCodec(Class<?> mapKeyClass, Class<?> mapValueClass) {
        this(null, mapKeyClass, mapValueClass, new ObjectMapper());
    }

    /** @param mapKeyClass Map 键 @param mapValueClass Map 值 @param mapper ObjectMapper */
    public TypedJsonJacksonCodec(Class<?> mapKeyClass, Class<?> mapValueClass, ObjectMapper mapper) {
        this(null, mapKeyClass, mapValueClass, mapper);
    }
    
    /** @param valueClass 值 @param mapKeyClass Map 键 @param mapValueClass Map 值 */
    public TypedJsonJacksonCodec(Class<?> valueClass, Class<?> mapKeyClass, Class<?> mapValueClass) {
        this(null, null, null,
                valueClass, mapKeyClass, mapValueClass, new ObjectMapper(), false);
    }
    
    /** 同时绑定值与 Map 键值类型及自定义 Mapper。 */
    public TypedJsonJacksonCodec(Class<?> valueClass, Class<?> mapKeyClass, Class<?> mapValueClass, ObjectMapper mapper) {
        this(null, null, null,
                valueClass, mapKeyClass, mapValueClass, mapper, true);
    }

    /** @param valueTypeReference 值泛型类型引用 */
    public TypedJsonJacksonCodec(TypeReference<?> valueTypeReference) {
        this(valueTypeReference, new ObjectMapper());
    }

    /** @param valueTypeReference 值类型 @param mapper ObjectMapper */
    public TypedJsonJacksonCodec(TypeReference<?> valueTypeReference, ObjectMapper mapper) {
        this(valueTypeReference, null, null, mapper);
    }

    /** @param mapKeyTypeReference Map 键 @param mapValueTypeReference Map 值 */
    public TypedJsonJacksonCodec(TypeReference<?> mapKeyTypeReference, TypeReference<?> mapValueTypeReference) {
        this(null, mapKeyTypeReference, mapValueTypeReference);
    }
    
    /** Map 键值 TypeReference 与自定义 Mapper。 */
    public TypedJsonJacksonCodec(TypeReference<?> mapKeyTypeReference, TypeReference<?> mapValueTypeReference, ObjectMapper mapper) {
        this(null, mapKeyTypeReference, mapValueTypeReference, mapper);
    }
    
    /** 同时绑定值与 Map 的 TypeReference。 */
    public TypedJsonJacksonCodec(TypeReference<?> valueTypeReference, TypeReference<?> mapKeyTypeReference, TypeReference<?> mapValueTypeReference) {
        this(valueTypeReference, mapKeyTypeReference, mapValueTypeReference,
                null, null, null, new ObjectMapper(), false);
    }
    
    /** 全 TypeReference 绑定并指定 Mapper。 */
    public TypedJsonJacksonCodec(TypeReference<?> valueTypeReference, TypeReference<?> mapKeyTypeReference, TypeReference<?> mapValueTypeReference, ObjectMapper mapper) {
        this(valueTypeReference, mapKeyTypeReference, mapValueTypeReference,
                null, null, null, mapper, true);
    }
    
    /** 在指定类加载器下复制编解码器。 */
    public TypedJsonJacksonCodec(ClassLoader classLoader, TypedJsonJacksonCodec codec) {
        this(codec.valueTypeReference, codec.mapKeyTypeReference, codec.mapValueTypeReference, 
              codec.valueClass, codec.mapKeyClass, codec.mapValueClass,
                createObjectMapper(classLoader, codec.mapObjectMapper.copy()), false);
    }

    /** 内部构造：初始化各解码器与类型绑定字段。 */
    TypedJsonJacksonCodec(TypeReference<?> valueTypeReference, TypeReference<?> mapKeyTypeReference, TypeReference<?> mapValueTypeReference,
                            Class<?> valueClass, Class<?> mapKeyClass, Class<?> mapValueClass, ObjectMapper mapper, boolean copy) {
        super(mapper, copy);
        this.mapValueDecoder = createDecoder(mapValueClass, mapValueTypeReference);
        this.mapKeyDecoder = createDecoder(mapKeyClass, mapKeyTypeReference);
        this.valueDecoder = createDecoder(valueClass, valueTypeReference);
        
        this.mapValueClass = mapValueClass;
        this.mapValueTypeReference = mapValueTypeReference;
        this.mapKeyClass = mapKeyClass;
        this.mapKeyTypeReference = mapKeyTypeReference;
        this.valueClass = valueClass;
        this.valueTypeReference = valueTypeReference;
    }
    
    /** 禁用 Jackson 默认的类型信息 inclusion（不写入 @class）。 */
    @Override
    protected void initTypeInclusion(ObjectMapper mapObjectMapper) {
        // avoid type inclusion
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return valueDecoder;
    }
    
    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }
    
    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return mapKeyDecoder;
    }
    
    @Override
    public Encoder getMapValueEncoder() {
        return encoder;
    }
    
    @Override
    public Encoder getMapKeyEncoder() {
        return encoder;
    }
    
    @Override
    public Decoder<Object> getMapValueDecoder() {
        return mapValueDecoder;
    }

}
