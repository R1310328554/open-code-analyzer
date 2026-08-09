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
package org.redisson.reactive;

import org.redisson.RedissonBinaryStream;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RFuture;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * {@link org.redisson.api.RBinaryStreamReactive} 的 Reactor 读写门面。
 * <p>
 * 委托 {@link RedissonBinaryStream.RedissonAsynchronousByteChannel} 异步读写
 * {@link ByteBuffer}，并通过 {@link CommandReactiveExecutor} 包装为 {@link Mono}。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonBinaryStreamReactive {

    /** Reactor 命令执行器。 */
    private final CommandReactiveExecutor commandExecutor;
    /** 底层 Redis 二进制流异步通道。 */
    private final RedissonBinaryStream.RedissonAsynchronousByteChannel channel;

    /** 从 {@link RBinaryStream} 提取异步通道并绑定执行器。 */
    public RedissonBinaryStreamReactive(CommandReactiveExecutor commandExecutor, RBinaryStream stream) {
        this.commandExecutor = commandExecutor;
        channel = (RedissonBinaryStream.RedissonAsynchronousByteChannel) stream.getAsynchronousChannel();
    }

    /** 返回当前读写游标位置。 */
    public long position() {
        return channel.position();
    }

    /** 设置读写游标位置。 */
    public void position(long newPosition) {
        channel.position(newPosition);
    }

    /** 异步从流中读取字节到缓冲区，返回实际读取长度。 */
    public Mono<Integer> read(ByteBuffer buf) {
        return commandExecutor.reactive(() -> (RFuture<Integer>) channel.read(buf));
    }

    /** 异步将缓冲区内容写入流，返回实际写入长度。 */
    public Mono<Integer> write(ByteBuffer buf) {
        return commandExecutor.reactive(() -> (RFuture<Integer>) channel.write(buf));
    }

}
