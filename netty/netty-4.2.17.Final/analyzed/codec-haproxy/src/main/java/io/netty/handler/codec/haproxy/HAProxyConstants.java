/*
 * Copyright 2014 The Netty Project
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

package io.netty.handler.codec.haproxy;

/** HAProxy 代理协议内部常量（命令、版本、传输层与地址族等字节值）。 */
final class HAProxyConstants {

    /** 命令字节：LOCAL。 */
    static final byte COMMAND_LOCAL_BYTE = 0x00;
    /** 命令字节：PROXY。 */
    static final byte COMMAND_PROXY_BYTE = 0x01;

    /** 协议版本字节：v1（文本行格式）。 */
    static final byte VERSION_ONE_BYTE = 0x10;
    /** 协议版本字节：v2（二进制格式）。 */
    static final byte VERSION_TWO_BYTE = 0x20;

    /** 传输协议：未指定。 */
    static final byte TRANSPORT_UNSPEC_BYTE = 0x00;
    /** 传输协议：流式（TCP 等）。 */
    static final byte TRANSPORT_STREAM_BYTE = 0x01;
    /** 传输协议：数据报（UDP 等）。 */
    static final byte TRANSPORT_DGRAM_BYTE = 0x02;

    /** 地址族：未指定。 */
    static final byte AF_UNSPEC_BYTE = 0x00;
    /** 地址族：IPv4。 */
    static final byte AF_IPV4_BYTE = 0x10;
    /** 地址族：IPv6。 */
    static final byte AF_IPV6_BYTE = 0x20;
    /** 地址族：Unix 域套接字。 */
    static final byte AF_UNIX_BYTE = 0x30;

    /** 传输协议与地址族组合：未知。 */
    static final byte TPAF_UNKNOWN_BYTE = 0x00;
    /** 传输协议与地址族组合：TCP over IPv4。 */
    static final byte TPAF_TCP4_BYTE = 0x11;
    /** 传输协议与地址族组合：TCP over IPv6。 */
    static final byte TPAF_TCP6_BYTE = 0x21;
    /** 传输协议与地址族组合：UDP over IPv4。 */
    static final byte TPAF_UDP4_BYTE = 0x12;
    /** 传输协议与地址族组合：UDP over IPv6。 */
    static final byte TPAF_UDP6_BYTE = 0x22;
    /** 传输协议与地址族组合：Unix 流式套接字。 */
    static final byte TPAF_UNIX_STREAM_BYTE = 0x31;
    /** 传输协议与地址族组合：Unix 数据报套接字。 */
    static final byte TPAF_UNIX_DGRAM_BYTE = 0x32;

    /** v2 二进制协议固定 12 字节魔数前缀。 */
    static final byte[] BINARY_PREFIX = {
            (byte) 0x0D,
            (byte) 0x0A,
            (byte) 0x0D,
            (byte) 0x0A,
            (byte) 0x00,
            (byte) 0x0D,
            (byte) 0x0A,
            (byte) 0x51,
            (byte) 0x55,
            (byte) 0x49,
            (byte) 0x54,
            (byte) 0x0A
    };

    /** v1 文本协议行前缀 "PROXY"。 */
    static final byte[] TEXT_PREFIX = {
            (byte) 'P',
            (byte) 'R',
            (byte) 'O',
            (byte) 'X',
            (byte) 'Y',
    };

    private HAProxyConstants() { }
}
