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
 * SOCKS5 命令应答（{@link SocksCmdResponse}）中的 REP 字段状态码（RFC 1928 §6）。
 * <p>0x00 表示代理已成功执行请求；0x01–0x08 为各类失败原因；
 * 未在 RFC 中分配的值经 {@link #valueOf(byte)} 映射为 {@link #UNASSIGNED}。</p>
 */
public enum SocksCmdStatus {
    /** 成功（0x00）。 */
    SUCCESS((byte) 0x00),
    /** 一般 SOCKS 服务器故障（0x01）。 */
    FAILURE((byte) 0x01),
    /** 规则禁止连接（0x02）。 */
    FORBIDDEN((byte) 0x02),
    /** 网络不可达（0x03）。 */
    NETWORK_UNREACHABLE((byte) 0x03),
    /** 主机不可达（0x04）。 */
    HOST_UNREACHABLE((byte) 0x04),
    /** 连接被拒绝（0x05）。 */
    REFUSED((byte) 0x05),
    /** TTL 过期（0x06）。 */
    TTL_EXPIRED((byte) 0x06),
    /** 命令不支持（0x07）。 */
    COMMAND_NOT_SUPPORTED((byte) 0x07),
    /** 地址类型不支持（0x08）。 */
    ADDRESS_NOT_SUPPORTED((byte) 0x08),
    /** RFC 未定义或未识别的状态字节（默认回退值）。 */
    UNASSIGNED((byte) 0xff);

    private final byte b;

    SocksCmdStatus(byte b) {
        this.b = b;
    }

    /**
     * @deprecated Use {@link #valueOf(byte)} instead.
     */
    @Deprecated
    public static SocksCmdStatus fromByte(byte b) {
        return valueOf(b);
    }

    /**
     * 按 REP 字节查找枚举；未知 REP 返回 {@link #UNASSIGNED} 而非抛异常。
     */
    public static SocksCmdStatus valueOf(byte b) {
        for (SocksCmdStatus code : values()) {
            if (code.b == b) {
                return code;
            }
        }
        return UNASSIGNED;
    }

    public byte byteValue() {
        return b;
    }
}
