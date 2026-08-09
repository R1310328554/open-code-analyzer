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

import java.nio.ByteBuffer;

/**
 * 生成 QUIC 连接 ID（Connection ID）的工厂接口。
 */
public interface QuicConnectionIdGenerator {
    /**
     * 生成指定长度的新连接 ID。签名型生成器可能不支持无输入调用，
     * 否则可能重复生成相同 ID 并引发不可预期问题。
     *
     * @param length    the length of the id.
     * @return          the id.
     */
    ByteBuffer newId(int length);

    /**
     * 基于给定输入生成指定长度的连接 ID；实现可选择签名、种子或直接忽略输入。
     *
     * @param input     the input which may be used to generate the id.
     * @param length    the length of the id.
     * @return          the id.
     */
    ByteBuffer newId(ByteBuffer input, int length);

    /**
     * 基于源/目的连接 ID 生成新 ID；实现可选择用于签名或种子，也可能忽略。
     *
     * @param scid      the source connection id which may be used to generate the id.
     * @param dcid      the destination connection id which may be used to generate the id.
     * @param length    the length of the id.
     * @return          the id.
     */
    default ByteBuffer newId(ByteBuffer scid, ByteBuffer dcid, int length) {
        return newId(dcid, length);
    }

    /**
     * 返回本生成器支持的最大连接 ID 长度。
     *
     * @return the maximum length of a connection id that is supported.
     */
    int maxConnectionIdLength();

    /**
     * 若相同输入始终产生相同 ID（幂等）则返回 {@code true}，否则返回 {@code false}。
     *
     * @return whether the implementation is idempotent.
     */
    boolean isIdempotent();

    /**
     * 返回基于安全随机数生成连接 ID 的 {@link QuicConnectionIdGenerator}。
     *
     * @return a {@link QuicConnectionIdGenerator} which randomly generated ids.
     */
    static QuicConnectionIdGenerator randomGenerator() {
        return SecureRandomQuicConnectionIdGenerator.INSTANCE;
    }

    /**
     * 返回通过对输入签名生成连接 ID 的 {@link QuicConnectionIdGenerator}。
     *
     * @return a {@link QuicConnectionIdGenerator} which generates ids by signing the given input.
     */
    static QuicConnectionIdGenerator signGenerator() {
        return HmacSignQuicConnectionIdGenerator.INSTANCE;
    }
}
