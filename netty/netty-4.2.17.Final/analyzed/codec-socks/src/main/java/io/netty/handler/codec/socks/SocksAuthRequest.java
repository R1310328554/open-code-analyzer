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
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;

import java.nio.charset.CharsetEncoder;

/**
 * An socks auth request.
 *
 * @see SocksAuthResponse
 * @see SocksAuthRequestDecoder
 * <p>SOCKS5 用户名/密码子协商（RFC 1929）的客户端请求：版本 0x01 + 用户名长度 + 用户名
 * + 密码长度 + 密码，字段均为 US-ASCII，单字段最长 255 字节。</p>
 */
public final class SocksAuthRequest extends SocksRequest {
    private static final SocksSubnegotiationVersion SUBNEGOTIATION_VERSION = SocksSubnegotiationVersion.AUTH_PASSWORD;
    private final String username;
    private final String password;

    /**
     * @param username 纯 ASCII 用户名，长度 ≤ 255。
     * @param password 纯 ASCII 密码，长度 ≤ 255。
     */
    public SocksAuthRequest(String username, String password) {
        super(SocksRequestType.AUTH);
        ObjectUtil.checkNotNull(username, "username");
        ObjectUtil.checkNotNull(password, "password");
        final CharsetEncoder asciiEncoder = CharsetUtil.encoder(CharsetUtil.US_ASCII);
        if (!asciiEncoder.canEncode(username) || !asciiEncoder.canEncode(password)) {
            throw new IllegalArgumentException(
                    "username: " + username + " or password: **** values should be in pure ascii");
        }
        if (username.length() > 255) {
            throw new IllegalArgumentException("username: " + username + " exceeds 255 char limit");
        }
        if (password.length() > 255) {
            throw new IllegalArgumentException("password: **** exceeds 255 char limit");
        }
        this.username = username;
        this.password = password;
    }

    /**
     * Returns username that needs to be authenticated
     *
     * @return username that needs to be authenticated
     */
    public String username() {
        return username;
    }

    /**
     * Returns password that needs to be validated
     *
     * @return password that needs to be validated
     */
    public String password() {
        return password;
    }

    @Override
    public void encodeAsByteBuf(ByteBuf byteBuf) {
        byteBuf.writeByte(SUBNEGOTIATION_VERSION.byteValue());
        byteBuf.writeByte(username.length());
        byteBuf.writeCharSequence(username, CharsetUtil.US_ASCII);
        byteBuf.writeByte(password.length());
        byteBuf.writeCharSequence(password, CharsetUtil.US_ASCII);
    }
}
