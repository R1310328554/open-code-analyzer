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

import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * {@link RBinaryStream} 的 Reactor 风格 API 接口。
 * <p>单个流最大容量为 512MB；支持按位置读写 {@link ByteBuffer}。
 *
 * @author Nikita Koksharov
 */
public interface RBinaryStreamReactive extends RBucketReactive<byte[]> {

    /**
     * 返回当前读写位置（字节偏移）。
     *
     * @return 当前位置
     */
    long position();

    /**
     * 设置读写位置。
     *
     * @param newPosition 新的字节偏移
     */
    void position(long newPosition);

    /**
     * 从当前位置读取字节到 {@code buf}。
     *
     * @param buf 目标缓冲区
     * @return 实际读取的字节数
     */
    Mono<Integer> read(ByteBuffer buf);

    /**
     * 将 {@code buf} 中的字节写入当前位置。
     *
     * @param buf 源缓冲区
     * @return 实际写入的字节数
     */
    Mono<Integer> write(ByteBuffer buf);

}
