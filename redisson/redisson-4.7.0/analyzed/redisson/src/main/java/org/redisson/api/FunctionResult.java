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
package org.redisson.api;

import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;

/**
 * {@code FCALL} 返回值类型，决定使用的 Redis 命令与解码方式。
 *
 * @author Nikita Koksharov
 */
public enum FunctionResult {

    /** 返回 {@code Boolean} 类型。 */
    BOOLEAN(RedisCommands.FCALL_BOOLEAN_SAFE),

    /** 返回 {@code Long} 类型。 */
    LONG(RedisCommands.FCALL_LONG),

    /** 返回 {@code List} 类型。 */
    LIST(RedisCommands.FCALL_LIST),

    /** 返回普通 {@code String} 类型。 */
    STRING(RedisCommands.FCALL_STRING),

    /** 返回用户自定义类型（由 {@link org.redisson.client.codec.Codec} 解码）。 */
    VALUE(RedisCommands.FCALL_OBJECT),

    /** 返回 Map 值类型；使用 {@code Codec.getMapValueDecoder/Encoder()} 编解码。 */
    MAPVALUE(RedisCommands.FCALL_MAP_VALUE),

    /** 返回 Map 值类型的 {@code List}；使用 {@code Codec.getMapValueDecoder/Encoder()} 编解码。 */
    MAPVALUELIST(RedisCommands.FCALL_MAP_VALUE_LIST);

    private final RedisCommand<?> command;

    FunctionResult(RedisCommand<?> command) {
        this.command = command;
    }

    /** @return 对应的 {@code FCALL_*} Redis 命令 */
    public RedisCommand<?> getCommand() {
        return command;
    }

}
