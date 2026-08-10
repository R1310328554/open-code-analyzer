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

/**
 * An unknown socks response.
 *
 * <p>解码器在无法识别响应类型或解析失败时返回的占位对象（{@link SocksResponseType#UNKNOWN}）。
 * 与 {@link UnknownSocksRequest} 对称，{@link #encodeAsByteBuf} 不写入任何字节。</p>
 *
 * @see SocksInitResponseDecoder
 * @see SocksAuthResponseDecoder
 * @see SocksCmdResponseDecoder
 */
public final class UnknownSocksResponse extends SocksResponse {

    public UnknownSocksResponse() {
        super(SocksResponseType.UNKNOWN);
    }

    @Override
    public void encodeAsByteBuf(ByteBuf byteBuf) {
        // 占位响应无合法线格式，编码器不应调用
    }
}
