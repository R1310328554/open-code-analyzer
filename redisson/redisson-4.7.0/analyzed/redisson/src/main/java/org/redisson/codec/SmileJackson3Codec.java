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

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.smile.SmileFactory;

/**
 * 基于 Jackson 3 的 Smile 二进制 JSON 编解码器。
 * <p>
 * 继承 {@link JsonJackson3Codec}，使用 {@link SmileFactory} 输出 Smile 格式，
 * 比文本 JSON 更紧凑，完全线程安全。
 *
 * @author Nikita Koksharov
 *
 */
public class SmileJackson3Codec extends JsonJackson3Codec {

    /** 默认 Smile ObjectMapper。 */
    public SmileJackson3Codec() {
        super(new ObjectMapper(new SmileFactory()));
    }

    /** @param classLoader 反序列化类加载器 */
    public SmileJackson3Codec(ClassLoader classLoader) {
        super(createMapper(classLoader, new ObjectMapper(new SmileFactory())));
    }

    /** 在指定类加载器下复制现有编解码器配置。 */
    public SmileJackson3Codec(ClassLoader classLoader, SmileJackson3Codec codec) {
        super(createMapper(classLoader, codec.mapObjectMapper.rebuild().build()));
    }
    
}
