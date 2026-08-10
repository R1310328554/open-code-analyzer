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
 * SOCKS5 命令请求（{@link SocksCmdRequest}）中的 CMD 字段（RFC 1928 §4）。
 * <p>{@link #CONNECT} 建立到目标主机的 TCP 流；{@link #BIND} 供 FTP 等场景等待入站连接；
 * {@link #UDP} 开启 UDP 关联；未知 CMD 字节映射为 {@link #UNKNOWN}。</p>
 */
public enum SocksCmdType {
    /** 建立出站 TCP 连接（0x01），最常见用法。 */
    CONNECT((byte) 0x01),
    /** 绑定并监听，供对端回连（0x02）。 */
    BIND((byte) 0x02),
    /** 建立 UDP 关联（0x03）。 */
    UDP((byte) 0x03),
    /** 未识别或非标准 CMD 值。 */
    UNKNOWN((byte) 0xff);

    private final byte b;

    SocksCmdType(byte b) {
        this.b = b;
    }

    /**
     * @deprecated Use {@link #valueOf(byte)} instead.
     */
    @Deprecated
    public static SocksCmdType fromByte(byte b) {
        return valueOf(b);
    }

    /** 将 wire 字节解析为命令类型；无匹配时返回 {@link #UNKNOWN}。 */
    public static SocksCmdType valueOf(byte b) {
        for (SocksCmdType code : values()) {
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
