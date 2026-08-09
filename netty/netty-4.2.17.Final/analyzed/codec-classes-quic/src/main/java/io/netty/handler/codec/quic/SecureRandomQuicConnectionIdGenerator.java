/*
 * Copyright 2020 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.quic;

import io.netty.util.internal.ObjectUtil;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

/**
 * 使用 {@link SecureRandom} 生成随机 QUIC 连接 ID 的 {@link QuicConnectionIdGenerator} 实现。
 * 每次调用 {@link #newId(int)} 均产生新随机字节，非幂等。
 */
final class SecureRandomQuicConnectionIdGenerator implements QuicConnectionIdGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    /** 全局单例，供默认连接 ID 生成策略使用。 */
    static final QuicConnectionIdGenerator INSTANCE = new SecureRandomQuicConnectionIdGenerator();

    private SecureRandomQuicConnectionIdGenerator() {
    }

    /** 生成指定长度的随机连接 ID 字节。 */
    @Override
    public ByteBuffer newId(int length) {
        ObjectUtil.checkInRange(length, 0, maxConnectionIdLength(), "length");
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return ByteBuffer.wrap(bytes);
    }

    /** 忽略 buffer 参数，直接生成新随机 ID。 */
    @Override
    public ByteBuffer newId(ByteBuffer buffer, int length) {
        return newId(length);
    }

    /** 返回 Quiche 允许的最大连接 ID 长度。 */
    @Override
    public int maxConnectionIdLength() {
        return Quiche.QUICHE_MAX_CONN_ID_LEN;
    }

    /** 随机生成，同一输入不产生相同 ID，返回 {@code false}。 */
    @Override
    public boolean isIdempotent() {
        return false;
    }
}
