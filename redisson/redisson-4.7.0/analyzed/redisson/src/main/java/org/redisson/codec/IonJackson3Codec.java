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

import tools.jackson.dataformat.ion.IonObjectMapper;

/**
 * 基于 Jackson 3 的 Amazon Ion 二进制编解码器。
 * <p>
 * 继承 {@link JsonJackson3Codec}，使用 {@link IonObjectMapper} 读写 Ion 格式；
 * 完全线程安全。
 *
 * @author Nikita Koksharov
 *
 */
public class IonJackson3Codec extends JsonJackson3Codec {

    /** 使用默认 IonObjectMapper。 */
    public IonJackson3Codec() {
        super(new IonObjectMapper());
    }

    /** 按 ClassLoader 创建 Ion Mapper。 */
    public IonJackson3Codec(ClassLoader classLoader) {
        super(createMapper(classLoader, new IonObjectMapper()));
    }

    /** 从已有 Codec 复制 Mapper 并应用指定 ClassLoader。 */
    public IonJackson3Codec(ClassLoader classLoader, IonJackson3Codec codec) {
        super(createMapper(classLoader, codec.mapObjectMapper.rebuild().build()));
    }
    
}
