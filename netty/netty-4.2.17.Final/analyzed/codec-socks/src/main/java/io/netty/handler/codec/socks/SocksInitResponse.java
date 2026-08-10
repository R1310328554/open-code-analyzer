/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ObjectUtil;

/**
 * An socks init response.
 *
 * <p>服务器对 {@link SocksInitRequest} 的应答（RFC 1928 §3）：VER + 选定的单一
 * {@link SocksAuthScheme}。若为 {@link SocksAuthScheme#NO_AUTH} 则直接进入命令阶段；
 * 若为 {@link SocksAuthScheme#AUTH_PASSWORD} 则后续进行 RFC 1929 子协商。</p>
 *
 * @see SocksInitRequest
 * @see SocksInitResponseDecoder
 */
public final class SocksInitResponse extends SocksResponse {
    private final SocksAuthScheme authScheme;

    public SocksInitResponse(SocksAuthScheme authScheme) {
        super(SocksResponseType.INIT);
        this.authScheme = ObjectUtil.checkNotNull(authScheme, "authScheme");
    }

    /**
     * Returns the {@link SocksAuthScheme} of this {@link SocksInitResponse}
     *
     * @return The {@link SocksAuthScheme} of this {@link SocksInitResponse}
     */
    public SocksAuthScheme authScheme() {
        return authScheme;
    }

    @Override
    public void encodeAsByteBuf(ByteBuf byteBuf) {
        byteBuf.writeByte(protocolVersion().byteValue());
        byteBuf.writeByte(authScheme.byteValue());
    }
}
