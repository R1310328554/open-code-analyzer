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
package org.redisson.client.protocol;

import org.redisson.client.protocol.convertor.Convertor;
import org.redisson.client.protocol.decoder.MultiDecoder;

/**
 * 严格类型的 {@link RedisCommand} 子类，用于编译期约束返回类型。
 * <p>
 * 语义与普通 {@link RedisCommand} 相同，多用于 {@link RedisCommands} 中的常量声明。
 *
 * @author Nikita Koksharov
 *
 * @param <T> command type
 */
public class RedisStrictCommand<T> extends RedisCommand<T> {

    /** 指定命令名与响应解码器。 */
    public RedisStrictCommand(String name, MultiDecoder<T> replayMultiDecoder) {
        super(name, replayMultiDecoder);
    }

    public RedisStrictCommand(String name, String subName, MultiDecoder<T> replayMultiDecoder) {
        super(name, subName, replayMultiDecoder);
    }

    /** 仅指定命令名，使用默认解码逻辑。 */
    public RedisStrictCommand(String name) {
        super(name);
    }

    /** 指定命令名与结果转换器。 */
    public RedisStrictCommand(String name, Convertor<T> convertor) {
        super(name, convertor);
    }

    public RedisStrictCommand(String name, String subName) {
        super(name, subName);
    }
    
    public RedisStrictCommand(String name, String subName, Convertor<T> convertor) {
        super(name, subName, convertor);
    }

    public RedisStrictCommand(String name, String subName, MultiDecoder<T> replayMultiDecoder, Convertor convertor) {
        super(name, subName, replayMultiDecoder);
        this.convertor = convertor;
    }

}
