/*
 * Copyright 2023 The Netty Project
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
 * 生成 QUIC
 * <a href="https://www.ietf.org/archive/id/draft-ietf-quic-transport-29.html#name-calculating-a-stateless-res">
 *     无状态重置 token</a> 的接口。
 */
public interface QuicResetTokenGenerator {

    /**
     * 为给定连接 ID 生成重置 token，返回值长度必须为 16 字节。
     * @param cid the connection id
     * @return a newly generated reset token
     */
    ByteBuffer newResetToken(ByteBuffer cid);

    /**
     * 返回基于 HMAC 签名输入生成 token 的默认 {@link QuicResetTokenGenerator} 实现。
     *
     * @return a {@link QuicResetTokenGenerator} which generates new reset tokens by signing the given input.
     */
    static QuicResetTokenGenerator signGenerator() {
        return HmacSignQuicResetTokenGenerator.INSTANCE;
    }
}
