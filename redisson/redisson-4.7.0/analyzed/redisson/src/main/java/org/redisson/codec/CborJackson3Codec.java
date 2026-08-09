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
import tools.jackson.dataformat.cbor.CBORFactory;

/**
 * 基于 Jackson 3 的 CBOR 二进制 JSON 编解码器。
 * <p>
 * 继承 {@link JsonJackson3Codec}，使用 {@link CBORFactory} 输出紧凑 CBOR 格式，
 * 相比纯 JSON 体积更小、解析更快。
 *
 * @author Nikita Koksharov
 * 
 */
public class CborJackson3Codec extends JsonJackson3Codec {

    /** 使用默认 CBOR ObjectMapper 构造。 */
    public CborJackson3Codec() {
        super(new ObjectMapper(new CBORFactory()));
    }

    /** 按 ClassLoader 创建 CBOR Mapper。 */
    public CborJackson3Codec(ClassLoader classLoader) {
        super(createMapper(classLoader, new ObjectMapper(new CBORFactory())));
    }

    /** 从已有 Codec 复制 Mapper 配置并应用指定 ClassLoader。 */
    public CborJackson3Codec(ClassLoader classLoader, CborJackson3Codec codec) {
        super(createMapper(classLoader, codec.mapObjectMapper.rebuild().build()));
    }
    
}
