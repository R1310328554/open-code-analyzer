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
import org.redisson.client.protocol.convertor.EmptyConvertor;
import org.redisson.client.protocol.decoder.MultiDecoder;

/**
 * Redis 命令元数据：命令名、子命令、响应解码器与结果转换器。
 * <p>
 * 客户端通过 {@link RedisCommands} 中的常量引用预定义命令。
 *
 * @author Nikita Koksharov
 *
 * @param <R> return type
 */
public class RedisCommand<R> {

    /** Redis 主命令名（如 GET、CLUSTER）。 */
    private final String name;
    /** 子命令名（如 CLUSTER NODES 中的 NODES），可为 null。 */
    private final String subName;
    /** 缓存的 Lua 脚本 SHA，脚本类命令使用。 */
    private String script;

    /** 将 RESP 多段回复解码为中间结构。 */
    private final MultiDecoder<R> replayMultiDecoder;
    /** 将解码结果转换为最终 Java 类型。 */
    Convertor<R> convertor = new EmptyConvertor<R>();

    /**
     * 复制源命令并替换主命令名。
     *
     * @param command - source command
     * @param name - new command name
     */
    public RedisCommand(RedisCommand<R> command, String name) {
        this.name = name;
        this.subName = command.subName;
        this.replayMultiDecoder = command.replayMultiDecoder;
        this.convertor = command.convertor;
    }

    public RedisCommand(RedisCommand<R> command, String name, String script) {
        this.name = name;
        this.subName = command.subName;
        this.replayMultiDecoder = command.replayMultiDecoder;
        this.convertor = command.convertor;
        this.script = script;
    }

    public RedisCommand(RedisCommand<R> command, String name, Convertor<R> convertor) {
        this.name = name;
        this.subName = command.subName;
        this.replayMultiDecoder = command.replayMultiDecoder;
        this.convertor = convertor;
    }

    public RedisCommand(String name) {
        this(name, (String) null);
    }

    public RedisCommand(String name, String subName) {
        this(name, subName, (MultiDecoder<R>) null);
    }

    public RedisCommand(String name, String subName, Convertor<R> convertor) {
        this(name, subName);
        this.convertor = convertor;
    }

    public RedisCommand(String name, Convertor<R> convertor) {
        this(name, null, (MultiDecoder<R>) null);
        this.convertor = convertor;
    }

    public RedisCommand(String name, MultiDecoder<R> replayMultiDecoder) {
        this(name, null, replayMultiDecoder);
    }

    public RedisCommand(String name, MultiDecoder<R> replayMultiDecoder, Convertor<R> convertor) {
        this(name, replayMultiDecoder);
        this.convertor = convertor;
    }

    public RedisCommand(String name, String subName, MultiDecoder<R> replayMultiDecoder) {
        super();
        this.name = name;
        this.subName = subName;
        if (replayMultiDecoder != null) {
            this.replayMultiDecoder = replayMultiDecoder;
        } else {
            this.replayMultiDecoder = (parts, state) -> (R) parts;
        }
    }

    public String getSubName() {
        return subName;
    }

    /** 返回主命令名。 */
    public String getName() {
        return name;
    }

    /** 返回响应多段解码器。 */
    public MultiDecoder<R> getReplayMultiDecoder() {
        return replayMultiDecoder;
    }

    public Convertor<R> getConvertor() {
        return convertor;
    }

    /** 判断命令是否在 {@link RedisCommands#NO_RETRY} 集合中（不可安全重试）。 */
    public boolean isNoRetry() {
        return RedisCommands.NO_RETRY.contains(getName())
                || RedisCommands.NO_RETRY_COMMANDS.contains(this);
    }

    /** 判断是否为阻塞命令名或预注册的阻塞 {@link RedisCommand} 实例。 */
    public boolean isBlockingCommand() {
        return RedisCommands.BLOCKING_COMMAND_NAMES.contains(getName())
                || RedisCommands.BLOCKING_COMMANDS.contains(this);
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("(").append(name);
        if (subName != null) {
            str.append(" ").append(subName);
        }
        if (script != null) {
            str.append(", cached script: ").append(script);
        }
        str.append(")");
        return str.toString();
    }

}
