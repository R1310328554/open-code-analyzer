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

import io.netty.util.internal.ObjectUtil;

import java.nio.ByteBuffer;

/**
 * 通过对连接 ID 做 HMAC 签名生成 Reset Token 的 {@link QuicResetTokenGenerator}。
 * 算法参见
 * <a href="https://www.ietf.org/archive/id/draft-ietf-quic-transport-29.html#section-10.4.2">QUIC 传输草案 §10.4.2</a>。
 */
final class HmacSignQuicResetTokenGenerator implements QuicResetTokenGenerator {
    /** 单例实例。 */
    static final QuicResetTokenGenerator INSTANCE = new HmacSignQuicResetTokenGenerator();

    private HmacSignQuicResetTokenGenerator() {
    }

    @Override
    public ByteBuffer newResetToken(ByteBuffer cid) {
        ObjectUtil.checkNotNull(cid, "cid");
        ObjectUtil.checkPositive(cid.remaining(), "cid");
        return Hmac.signToken(cid, Quic.RESET_TOKEN_LEN);
    }
}
