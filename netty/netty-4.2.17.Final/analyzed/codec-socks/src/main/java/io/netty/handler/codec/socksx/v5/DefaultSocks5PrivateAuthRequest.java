/*
 * Copyright 2025 The Netty Project
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
package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * The default {@link Socks5PrivateAuthRequest} implementation.
 * <p>
 * For custom private authentication protocols, you should implement the {@link Socks5PrivateAuthRequest}
 * interface directly. Custom protocols should also implement their own encoder/decoder to handle the wire format.
 * </p>
 *
 * <p>私有认证方法（RFC 1928 中 0x80–0xFE 范围）的默认请求实现。
 * 令牌以字节数组持有，构造与访问时均 {@code clone()} 以防外部篡改；
 * 自定义私有协议应直接实现接口并配套编解码器。</p>
 */
public final class DefaultSocks5PrivateAuthRequest extends AbstractSocks5Message
    implements Socks5PrivateAuthRequest {

    /**
     * The private authentication token.
     *
     * <p>私有认证令牌原始字节；wire 格式为 VER(1) + LEN(1) + TOKEN。</p>
     */
    private final byte[] privateToken;

    /**
     * Creates a new instance with the specified token.
     *
     * @param privateAuthToken the private authentication token
     */
    public DefaultSocks5PrivateAuthRequest(final byte[] privateAuthToken) {
        // 防御性拷贝，避免调用方后续修改传入数组
        this.privateToken = ObjectUtil.checkNotNull(privateAuthToken, "privateToken").clone();
    }

    @Override
    public byte[] privateToken() {
        return privateToken.clone();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(StringUtil.simpleClassName(this));

        DecoderResult decoderResult = decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", privateToken: ****)");
        } else {
            // 日志中隐藏令牌明文
            buf.append("(privateToken: ****)");
        }

        return buf.toString();
    }
}
