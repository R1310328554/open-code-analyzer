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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroFactory;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;

/**
 * 基于 Jackson 2 的 Avro 二进制编解码器。
 * <p>
 * 继承 {@link JsonJacksonCodec}，使用 {@link AvroFactory} 进行紧凑二进制序列化；
 * 可绑定固定 {@link AvroSchema} 对特定类型进行强类型读写。
 *
 * @author Nikita Koksharov
 *
 */
public class AvroJacksonCodec extends JsonJacksonCodec {

    /**
     * 绑定 Avro Schema 与目标类型的 {@link AvroMapper} 扩展。
     * <p>
     * 读写时始终通过 {@code writerFor(type).with(schema)} 确保符合 Schema 约束。
     */
    public static class AvroExtendedMapper extends AvroMapper {

        private static final long serialVersionUID = -560070554221164163L;

        /** 序列化/反序列化使用的 Avro Schema。 */
        private final AvroSchema schema;
        /** 绑定的 Java 类型。 */
        private final Class<?> type;
        
        /** @param type 目标类型 @param schema Avro Schema */
        public AvroExtendedMapper(Class<?> type, AvroSchema schema) {
            super();
            this.type = type;
            this.schema = schema;
        }
        
        /** 复制 Mapper 实例，保留 Schema 与类型绑定。 */
        @Override
        public AvroMapper copy() {
            _checkInvalidCopy(AvroExtendedMapper.class);
            return new AvroExtendedMapper(type, schema);
        }

        /** 按 Schema 将对象写入输出流。 */
        @Override
        public void writeValue(OutputStream out, Object value)
                throws IOException, JsonGenerationException, JsonMappingException {
            writerFor(type).with(schema).writeValue(out, value);
        }
        /** 按 Schema 将对象序列化为字节数组。 */
        @Override
        public byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
            return writerFor(type).with(schema).writeValueAsBytes(value);
        }
        
        /** 从输入流按 Schema 反序列化为指定类型。 */
        @Override
        public <T> T readValue(InputStream src, Class<T> valueType)
                throws IOException, JsonParseException, JsonMappingException {
            return readerFor(type).with(schema).readValue(src);
        } 
        
    }

    /** 使用绑定 Schema 的 Avro Mapper 构造。 */
    public AvroJacksonCodec(Class<?> type, AvroSchema schema) {
        super(new AvroExtendedMapper(type, schema));
    }
    
    /** 按 ClassLoader 创建默认 Avro Mapper。 */
    public AvroJacksonCodec(ClassLoader classLoader) {
        super(createObjectMapper(classLoader, new ObjectMapper(new AvroFactory())));
    }
    
    /** 从已有 Codec 复制 Mapper 配置并应用指定 ClassLoader。 */
    public AvroJacksonCodec(ClassLoader classLoader, AvroJacksonCodec codec) {
        super(createObjectMapper(classLoader, codec.mapObjectMapper.copy()));
    }
    
    /** Avro 格式不需要 Jackson 多态类型信息，故留空。 */
    @Override
    protected void initTypeInclusion(ObjectMapper mapObjectMapper) {
    }
    
}
