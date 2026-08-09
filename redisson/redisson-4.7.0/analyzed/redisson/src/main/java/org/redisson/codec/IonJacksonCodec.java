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

import com.fasterxml.jackson.dataformat.ion.IonObjectMapper;

/**
 * 基于 Jackson 2 的 Amazon Ion 编解码器。
 * <a href="https://github.com/FasterXML/jackson-dataformats-binary/tree/master/ion">
 *     https://github.com/FasterXML/jackson-dataformats-binary/tree/master/ion
 * </a>
 * <p>
 * 继承 {@link JsonJacksonCodec}，使用 Ion 紧凑二进制格式；完全线程安全。
 *
 * @author Nikita Koksharov
 *
 */
public class IonJacksonCodec extends JsonJacksonCodec {

    /** 使用默认 IonObjectMapper。 */
    public IonJacksonCodec() {
        super(new IonObjectMapper());
    }
    
    /** 按 ClassLoader 创建 Ion Mapper。 */
    public IonJacksonCodec(ClassLoader classLoader) {
        super(createObjectMapper(classLoader, new IonObjectMapper()));
    }
    
    /** 从已有 Codec 复制 Mapper 配置并应用 ClassLoader。 */
    public IonJacksonCodec(ClassLoader classLoader, IonJacksonCodec codec) {
        super(createObjectMapper(classLoader, codec.mapObjectMapper.copy()));
    }
    
}
