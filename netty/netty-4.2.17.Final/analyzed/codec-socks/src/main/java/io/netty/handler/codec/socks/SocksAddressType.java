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
 * SOCKS4/5 请求与应答中目标地址的类型字段（ATYP）。
 * <p>Wire 值定义见 RFC 1928：{@code 0x01} IPv4、{@code 0x03} 域名、{@code 0x04} IPv6；
 * 未知字节映射为 {@link #UNKNOWN}，解码器据此产出 {@link SocksCommonUtils#UNKNOWN_SOCKS_REQUEST}。</p>
 */
public enum SocksAddressType {
    /** 32 位 IPv4 地址，后跟 4 字节大端地址与 2 字节端口。 */
    IPv4((byte) 0x01),
    /** 变长域名：1 字节长度 + ASCII 主机名 + 2 字节端口。 */
    DOMAIN((byte) 0x03),
    /** 128 位 IPv6 地址，后跟 16 字节与 2 字节端口。 */
    IPv6((byte) 0x04),
    /** 无法识别的 ATYP 字节。 */
    UNKNOWN((byte) 0xff);

    private final byte b;

    SocksAddressType(byte b) {
        this.b = b;
    }

    /**
     * @deprecated Use {@link #valueOf(byte)} instead.
     */
    @Deprecated
    public static SocksAddressType fromByte(byte b) {
        return valueOf(b);
    }

    /**
     * 按协议字节解析地址类型；无匹配时返回 {@link #UNKNOWN}。
     */
    public static SocksAddressType valueOf(byte b) {
        for (SocksAddressType code : values()) {
            if (code.b == b) {
                return code;
            }
        }
        return UNKNOWN;
    }

    /** 返回写入 SOCKS 报文的 ATYP 字节。 */
    public byte byteValue() {
        return b;
    }
}

