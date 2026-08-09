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
package org.redisson.rx;

import io.reactivex.rxjava3.core.Single;
import org.redisson.RedissonBinaryStream;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RFuture;

import java.nio.ByteBuffer;

/**
 * {@link org.redisson.api.RBinaryStreamRx} 的 Rx 辅助实现：二进制流异步读写。
 * <p>
 * 委托 {@link RedissonBinaryStream.RedissonAsynchronousByteChannel} 的 position/read/write，
 * 通过 {@link CommandRxExecutor#flowable} 将 {@link RFuture} 转为 {@link Single}。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonBinaryStreamRx {

    /** Rx 命令执行器。 */
    private final CommandRxExecutor commandExecutor;
    /** 底层异步 NIO 字节通道。 */
    private final RedissonBinaryStream.RedissonAsynchronousByteChannel channel;

    public RedissonBinaryStreamRx(CommandRxExecutor commandExecutor, RBinaryStream stream) {
        this.commandExecutor = commandExecutor;
        channel = (RedissonBinaryStream.RedissonAsynchronousByteChannel) stream.getAsynchronousChannel();
    }

    /** 当前读写字节偏移量。 */
    public long position() {
        return channel.position();
    }

    public void position(long newPosition) {
        channel.position(newPosition);
    }

    /** 异步读入 buf，Single 值为实际读取字节数。 */
    public Single<Integer> read(ByteBuffer buf) {
        return commandExecutor.flowable(() -> ((RFuture<Integer>) channel.read(buf))).singleOrError();
    }

    /** 异步写出 buf，Single 值为实际写入字节数。 */
    public Single<Integer> write(ByteBuffer buf) {
        return commandExecutor.flowable(() -> ((RFuture<Integer>) channel.write(buf))).singleOrError();
    }

}
