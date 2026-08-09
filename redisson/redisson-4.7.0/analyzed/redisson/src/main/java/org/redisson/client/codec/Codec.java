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
package org.redisson.client.codec;

import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

/**
 * Redis 对象编解码器接口。
 * <p>
 * 实现类需提供无参构造器及接受 {@link ClassLoader} 的构造器，以便在自定义类加载器下复制实例。
 *
 * @author Nikita Koksharov
 *
 */
public interface Codec {

    /**
     * 返回 HMAP 结构中哈希表值的对象解码器。
     *
     * @return decoder
     */
    Decoder<Object> getMapValueDecoder();

    /**
     * 返回 HMAP 结构中哈希表值的对象编码器。
     *
     * @return encoder
     */
    Encoder getMapValueEncoder();

    /**
     * 返回 HMAP 结构中哈希表键的对象解码器。
     *
     * @return decoder
     */
    Decoder<Object> getMapKeyDecoder();

    /**
     * 返回 HMAP 结构中哈希表键的对象编码器。
     *
     * @return encoder
     */
    Encoder getMapKeyEncoder();

    /**
     * 返回除 HMAP 外其他 Redis 结构存储对象的通用解码器。
     *
     * @return decoder
     */
    Decoder<Object> getValueDecoder();

    /**
     * 返回除 HMAP 外其他 Redis 结构存储对象的通用编码器。
     *
     * @return encoder
     */
    Encoder getValueEncoder();
    
    /**
     * 返回解码过程中加载类所使用的类加载器。
     *
     * @return class loader
     */
    ClassLoader getClassLoader();

}
