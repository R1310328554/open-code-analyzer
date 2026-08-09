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

import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 基于 Jackson 2 与 MessagePack 的二进制 JSON 编解码器。
 * <p>
 * 继承 {@link JsonJacksonCodec}，使用 {@link MessagePackFactory} 输出紧凑二进制格式，
 * 相比纯 JSON 体积更小、解析更快，完全线程安全。
 *
 * @author Nikita Koksharov
 *
 */
public class MsgPackJacksonCodec extends JsonJacksonCodec {

    /** 默认 MessagePack ObjectMapper。 */
    public MsgPackJacksonCodec() {
        super(new ObjectMapper(new MessagePackFactory()));
    }
    
    /** @param classLoader 反序列化时使用的类加载器 */
    public MsgPackJacksonCodec(ClassLoader classLoader) {
        super(createObjectMapper(classLoader, new ObjectMapper(new MessagePackFactory())));
    }
    
    /** 在指定类加载器下复制现有编解码器的 Mapper 配置。 */
    public MsgPackJacksonCodec(ClassLoader classLoader, MsgPackJacksonCodec codec) {
        super(createObjectMapper(classLoader, codec.mapObjectMapper.copy()));
    }
    
}
