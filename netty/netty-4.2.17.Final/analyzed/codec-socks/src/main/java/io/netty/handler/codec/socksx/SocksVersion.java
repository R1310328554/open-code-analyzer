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

package io.netty.handler.codec.socksx;

/**
 * The version of SOCKS protocol.
 *
 * <p>{@code socksx} 模块使用的 SOCKS 协议版本枚举，对应各报文首字节 VER 字段。
 * {@link SocksPortUnificationServerHandler} 据此在 SOCKS4/5 pipeline 间分流。</p>
 */
public enum SocksVersion {
    /**
     * SOCKS protocol version 4a (or 4)
     *
     * <p>线格式 VER = 0x04；SOCKS4 与 SOCKS4a 共用此值。</p>
     */
    SOCKS4a((byte) 0x04),
    /**
     * SOCKS protocol version 5
     *
     * <p>线格式 VER = 0x05；支持多种认证与地址类型。</p>
     */
    SOCKS5((byte) 0x05),
    /**
     * Unknown protocol version
     *
     * <p>内部占位，非 RFC 定义的有效 VER 值。</p>
     */
    UNKNOWN((byte) 0xff);

    /**
     * Returns the {@link SocksVersion} that corresponds to the specified version field value,
     * as defined in the protocol specification.
     *
     * @return {@link #UNKNOWN} if the specified value does not represent a known SOCKS protocol version
     *
     * <p>仅识别 0x04 与 0x05；其他字节（含 SOCKS4 纯 0x04 以外的探测值）均归为 {@link #UNKNOWN}。</p>
     */
    public static SocksVersion valueOf(byte b) {
        if (b == SOCKS4a.byteValue()) {
            return SOCKS4a;
        }
        if (b == SOCKS5.byteValue()) {
            return SOCKS5;
        }
        return UNKNOWN;
    }

    /** 线格式 VER 单字节值。 */
    private final byte b;

    SocksVersion(byte b) {
        this.b = b;
    }

    /**
     * Returns the value of the version field, as defined in the protocol specification.
     */
    public byte byteValue() {
        return b;
    }
}
