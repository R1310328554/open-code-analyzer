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
 * SOCKS5 子协商（subnegotiation）协议版本。
 * <p>当主握手选中 {@link SocksAuthScheme#AUTH_PASSWORD} 后，
 * {@link SocksAuthRequest}/{@link SocksAuthResponse} 报文首字节 VER 固定为 {@link #AUTH_PASSWORD}（0x01，RFC 1929）。
 * 与主协议 {@link SocksProtocolVersion#SOCKS5} 相互独立。</p>
 */
public enum SocksSubnegotiationVersion {
    /** RFC 1929 用户名/密码认证子协议版本（0x01）。 */
    AUTH_PASSWORD((byte) 0x01),
    /** 未知子协商版本；解码器容错时的兜底值。 */
    UNKNOWN((byte) 0xff);

    /** 子协商报文线格式中的 VER 字节。 */
    private final byte b;

    SocksSubnegotiationVersion(byte b) {
        this.b = b;
    }

    /**
     * @deprecated Use {@link #valueOf(byte)} instead.
     */
    @Deprecated
    public static SocksSubnegotiationVersion fromByte(byte b) {
        return valueOf(b);
    }

    /** 线性扫描已知子协商版本；无匹配返回 {@link #UNKNOWN}。 */
    public static SocksSubnegotiationVersion valueOf(byte b) {
        for (SocksSubnegotiationVersion code : values()) {
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

