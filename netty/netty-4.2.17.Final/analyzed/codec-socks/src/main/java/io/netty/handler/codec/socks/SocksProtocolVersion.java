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
 * 旧版 {@code io.netty.handler.codec.socks} 包中的 SOCKS 协议版本枚举。
 * <p>对应各 SOCKS 报文首字节的 VER 字段：SOCKS4/4a 为 {@code 0x04}，SOCKS5 为 {@code 0x05}。
 * 解码器读到未知版本时映射为 {@link #UNKNOWN}，便于上层以占位消息继续处理或关闭连接。</p>
 */
public enum SocksProtocolVersion {
    /** SOCKS4/4a 协议版本（0x04）。 */
    SOCKS4a((byte) 0x04),
    /** SOCKS5 协议版本（0x05）；本包中 {@link SocksMessage} 固定使用此值。 */
    SOCKS5((byte) 0x05),
    /** 无法识别的版本字节；{@link #valueOf(byte)} 的兜底返回值。 */
    UNKNOWN((byte) 0xff);

    /** 协议线格式中的单字节版本值。 */
    private final byte b;

    SocksProtocolVersion(byte b) {
        this.b = b;
    }

    /**
     * @deprecated Use {@link #valueOf(byte)} instead.
     */
    @Deprecated
    public static SocksProtocolVersion fromByte(byte b) {
        return valueOf(b);
    }

    /**
     * 将线格式 VER 字节解析为枚举常量。
     * <p>线性扫描全部已知值；无匹配时返回 {@link #UNKNOWN} 而非抛异常，
     * 以便解码器在协议探测阶段容错。</p>
     */
    public static SocksProtocolVersion valueOf(byte b) {
        for (SocksProtocolVersion code : values()) {
            if (code.b == b) {
                return code;
            }
        }
        return UNKNOWN;
    }

    /** 返回写入 {@link io.netty.buffer.ByteBuf} 时使用的版本字节。 */
    public byte byteValue() {
        return b;
    }
}
