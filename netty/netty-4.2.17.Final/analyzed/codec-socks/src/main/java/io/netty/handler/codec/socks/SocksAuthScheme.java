/*
 * Copyright 2013 The Netty Project
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

/**
 * SOCKS 握手阶段客户端提供的认证方式列表（RFC 1928 METHOD 字段）。
 * <p>出现在 {@link SocksInitRequest} 中；服务端在 {@link SocksInitResponse} 里择一回复。
 * {@code 0x02} 选中后进入用户名/密码子协商（RFC 1929）。</p>
 */
public enum SocksAuthScheme {
    /** 无需认证（0x00）。 */
    NO_AUTH((byte) 0x00),
    /** GSSAPI 认证（0x01），本模块未实现具体 GSS 载荷。 */
    AUTH_GSSAPI((byte) 0x01),
    /** 用户名/密码认证（0x02），对应 {@link SocksAuthRequest}/{@link SocksAuthResponse}。 */
    AUTH_PASSWORD((byte) 0x02),
    /** 未知或未支持的 METHOD 字节。 */
    UNKNOWN((byte) 0xff);

    private final byte b;

    SocksAuthScheme(byte b) {
        this.b = b;
    }

    /**
     * @deprecated Use {@link #valueOf(byte)} instead.
     */
    @Deprecated
    public static SocksAuthScheme fromByte(byte b) {
        return valueOf(b);
    }

    /** 线性扫描枚举值；无匹配返回 {@link #UNKNOWN}。 */
    public static SocksAuthScheme valueOf(byte b) {
        for (SocksAuthScheme code : values()) {
            if (code.b == b) {
                return code;
            }
        }
        return UNKNOWN;
    }

    public byte byteValue() {
        return b;
    }
}

