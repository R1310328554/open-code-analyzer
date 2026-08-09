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

import static io.netty.handler.codec.haproxy.HAProxyConstants.*;

/**
 * HAProxy 代理的底层协议，由传输协议与地址族共同表示。
 * <p>
 * 含 TCP4/6、UDP4/6、UNIX 流/报文及 UNKNOWN 等组合。
 */
public enum HAProxyProxiedProtocol {
    /** 未知协议与地址族的转发连接。 */
    UNKNOWN(TPAF_UNKNOWN_BYTE, AddressFamily.AF_UNSPEC, TransportProtocol.UNSPEC),
    /** IPv4 客户端经 TCP 转发的连接。 */
    TCP4(TPAF_TCP4_BYTE, AddressFamily.AF_IPv4, TransportProtocol.STREAM),
    /** IPv6 客户端经 TCP 转发的连接。 */
    TCP6(TPAF_TCP6_BYTE, AddressFamily.AF_IPv6, TransportProtocol.STREAM),
    /** IPv4 客户端经 UDP 转发的连接。 */
    UDP4(TPAF_UDP4_BYTE, AddressFamily.AF_IPv4, TransportProtocol.DGRAM),
    /** IPv6 客户端经 UDP 转发的连接。 */
    UDP6(TPAF_UDP6_BYTE, AddressFamily.AF_IPv6, TransportProtocol.DGRAM),
    /** UNIX 流式套接字转发的连接。 */
    UNIX_STREAM(TPAF_UNIX_STREAM_BYTE, AddressFamily.AF_UNIX, TransportProtocol.STREAM),
    /** UNIX 数据报套接字转发的连接。 */
    UNIX_DGRAM(TPAF_UNIX_DGRAM_BYTE, AddressFamily.AF_UNIX, TransportProtocol.DGRAM);

    private final byte byteValue;
    private final AddressFamily addressFamily;
    private final TransportProtocol transportProtocol;

    /** 绑定规范字节值及地址族、传输协议。 */
    HAProxyProxiedProtocol(
            byte byteValue,
            AddressFamily addressFamily,
            TransportProtocol transportProtocol) {

        this.byteValue = byteValue;
        this.addressFamily = addressFamily;
        this.transportProtocol = transportProtocol;
    }

    /**
     * 从传输协议与地址族字节解析 {@link HAProxyProxiedProtocol}。
     *
     * @param tpafByte transport protocol and address family byte
     */
    public static HAProxyProxiedProtocol valueOf(byte tpafByte) {
        switch (tpafByte) {
            case TPAF_TCP4_BYTE:
                return TCP4;
            case TPAF_TCP6_BYTE:
                return TCP6;
            case TPAF_UNKNOWN_BYTE:
                return UNKNOWN;
            case TPAF_UDP4_BYTE:
                return UDP4;
            case TPAF_UDP6_BYTE:
                return UDP6;
            case TPAF_UNIX_STREAM_BYTE:
                return UNIX_STREAM;
            case TPAF_UNIX_DGRAM_BYTE:
                return UNIX_DGRAM;
            default:
                throw new IllegalArgumentException(
                        "unknown transport protocol + address family: " + (tpafByte & 0xFF));
        }
    }

    /** 返回本协议组合的字节值。 */
    public byte byteValue() {
        return byteValue;
    }

    /** 返回地址族部分。 */
    public AddressFamily addressFamily() {
        return addressFamily;
    }

    /** 返回传输协议部分。 */
    public TransportProtocol transportProtocol() {
        return transportProtocol;
    }

    /** PROXY 协议头部中的地址族。 */
    public enum AddressFamily {
        /** 未指定地址族（未知协议）。 */
        AF_UNSPEC(AF_UNSPEC_BYTE),
        /** IPv4 地址族。 */
        AF_IPv4(AF_IPV4_BYTE),
        /** IPv6 地址族。 */
        AF_IPv6(AF_IPV6_BYTE),
        /** UNIX 域套接字地址族。 */
        AF_UNIX(AF_UNIX_BYTE);

        /** 传输协议与地址族字节的高 4 位为地址族。 */
        private static final byte FAMILY_MASK = (byte) 0xf0;

        private final byte byteValue;

        /** 绑定规范字节值。 */
        AddressFamily(byte byteValue) {
            this.byteValue = byteValue;
        }

        /**
         * 从字节高 4 位解析 {@link AddressFamily}。
         *
         * @param tpafByte transport protocol and address family byte
         */
        public static AddressFamily valueOf(byte tpafByte) {
            int addressFamily = tpafByte & FAMILY_MASK;
            switch((byte) addressFamily) {
                case AF_IPV4_BYTE:
                    return AF_IPv4;
                case AF_IPV6_BYTE:
                    return AF_IPv6;
                case AF_UNSPEC_BYTE:
                    return AF_UNSPEC;
                case AF_UNIX_BYTE:
                    return AF_UNIX;
                default:
                    throw new IllegalArgumentException("unknown address family: " + addressFamily);
            }
        }

        /** 返回地址族字节值。 */
        public byte byteValue() {
            return byteValue;
        }
    }

    /** PROXY 协议头部中的传输协议。 */
    public enum TransportProtocol {
        /** 未指定传输协议。 */
        UNSPEC(TRANSPORT_UNSPEC_BYTE),
        /** 流式传输（TCP 等）。 */
        STREAM(TRANSPORT_STREAM_BYTE),
        /** 数据报传输（UDP 等）。 */
        DGRAM(TRANSPORT_DGRAM_BYTE);

        /** 传输协议位于字节低 4 位。 */
        private static final byte TRANSPORT_MASK = 0x0f;

        private final byte transportByte;

        /** 绑定规范字节值。 */
        TransportProtocol(byte transportByte) {
            this.transportByte = transportByte;
        }

        /**
         * 从字节低 4 位解析 {@link TransportProtocol}。
         *
         * @param tpafByte transport protocol and address family byte
         */
        public static TransportProtocol valueOf(byte tpafByte) {
            int transportProtocol = tpafByte & TRANSPORT_MASK;
            switch ((byte) transportProtocol) {
                case TRANSPORT_STREAM_BYTE:
                    return STREAM;
                case TRANSPORT_UNSPEC_BYTE:
                    return UNSPEC;
                case TRANSPORT_DGRAM_BYTE:
                    return DGRAM;
                default:
                    throw new IllegalArgumentException("unknown transport protocol: " + transportProtocol);
            }
        }

        /** 返回传输协议字节值。 */
        public byte byteValue() {
            return transportByte;
        }
    }
}
