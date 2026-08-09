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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.avro.AvroFactory;
import tools.jackson.dataformat.avro.AvroMapper;
import tools.jackson.dataformat.avro.AvroSchema;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 基于 Jackson 3 的 Avro 二进制编解码器。
 * <p>
 * 继承 {@link JsonJackson3Codec}，使用 {@link AvroFactory} 输出紧凑二进制格式；
 * 可绑定固定 {@link AvroSchema} 对特定类型进行强类型序列化。
 *
 * @author Nikita Koksharov
 *
 */
public class AvroJackson3Codec extends JsonJackson3Codec {

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

        /** 按 Schema 将对象写入输出流。 */
        @Override
        public void writeValue(OutputStream out, Object value)
                throws JacksonException {
            writerFor(type).with(schema).writeValue(out, value);
        }
        /** 按 Schema 将对象序列化为字节数组。 */
        @Override
        public byte[] writeValueAsBytes(Object value) throws JacksonException {
            return writerFor(type).with(schema).writeValueAsBytes(value);
        }

        /** 从输入流按 Schema 反序列化为指定类型。 */
        @Override
        public <T> T readValue(InputStream src, Class<T> valueType)
                throws JacksonException {
            return readerFor(type).with(schema).readValue(src);
        }

    }

    /** 使用绑定 Schema 的 Avro Mapper 构造。 */
    public AvroJackson3Codec(Class<?> type, AvroSchema schema) {
        super(new AvroExtendedMapper(type, schema));
    }

    /** 按 ClassLoader 创建默认 Avro Mapper。 */
    public AvroJackson3Codec(ClassLoader classLoader) {
        super(createMapper(classLoader, new ObjectMapper(new AvroFactory())));
    }

    /** 从已有 Codec 复制 Mapper 配置并应用指定 ClassLoader。 */
    public AvroJackson3Codec(ClassLoader classLoader, AvroJackson3Codec codec) {
        super(createMapper(classLoader, codec.mapObjectMapper.rebuild().build()));
    }

    /** Avro 格式不需要 Jackson 多态类型信息，故留空。 */
    @Override
    protected void initTypeInclusion(JsonMapper.Builder builder) {
    }

}
