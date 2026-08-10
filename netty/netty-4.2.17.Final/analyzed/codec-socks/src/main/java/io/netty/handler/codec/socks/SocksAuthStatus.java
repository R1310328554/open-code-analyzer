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
 * SOCKS5 用户名/密码子协商（RFC 1929）中的认证结果状态码。
 * <p>服务器在 {@link SocksAuthResponse} 中回传单字节状态：{@link #SUCCESS} 表示凭据有效，
 * {@link #FAILURE} 表示拒绝；客户端据此决定能否进入命令阶段。</p>
 */
public enum SocksAuthStatus {
    /** 认证成功（0x00），可继续发送 {@link SocksCmdRequest}。 */
    SUCCESS((byte) 0x00),
    /** 认证失败（0xff），连接应终止或重试其他认证方式。 */
    FAILURE((byte) 0xff);

    /** 协议 wire 上的单字节值。 */
    private final byte b;

    SocksAuthStatus(byte b) {
        this.b = b;
    }

    /**
     * @deprecated Use {@link #valueOf(byte)} instead.
     */
    @Deprecated
    public static SocksAuthStatus fromByte(byte b) {
        return valueOf(b);
    }

    /**
     * 将协议字节映射为枚举；未知值保守映射为 {@link #FAILURE}。
     */
    public static SocksAuthStatus valueOf(byte b) {
        for (SocksAuthStatus code : values()) {
            if (code.b == b) {
                return code;
            }
        }
        return FAILURE;
    }

    /** 返回写入 {@link ByteBuf} 时使用的单字节状态码。 */
    public byte byteValue() {
        return b;
    }
}
