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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

/**
 * 基于 Jackson 2 的 Smile 二进制 JSON 编解码器。
 * <p>
 * 继承 {@link JsonJacksonCodec}，使用 {@link SmileFactory} 进行紧凑二进制序列化，
 * 完全线程安全。
 *
 * @author Nikita Koksharov
 *
 */
public class SmileJacksonCodec extends JsonJacksonCodec {

    /** 默认 Smile ObjectMapper。 */
    public SmileJacksonCodec() {
        super(new ObjectMapper(new SmileFactory()));
    }
    
    /** @param classLoader 反序列化类加载器 */
    public SmileJacksonCodec(ClassLoader classLoader) {
        super(createObjectMapper(classLoader, new ObjectMapper(new SmileFactory())));
    }
    
    /** 在指定类加载器下复制现有编解码器配置。 */
    public SmileJacksonCodec(ClassLoader classLoader, SmileJacksonCodec codec) {
        super(createObjectMapper(classLoader, codec.mapObjectMapper.copy()));
    }
    
}
