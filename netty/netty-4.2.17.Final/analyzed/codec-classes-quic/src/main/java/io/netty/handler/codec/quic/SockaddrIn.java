/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.quic;

import io.netty.util.internal.PlatformDependent;
import org.jetbrains.annotations.Nullable;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * C {@code sockaddr_in} / {@code sockaddr_in6} 与 Java {@link InetSocketAddress} 的双向转换工具。
 * 直接读写 {@link ByteBuffer} 中的原生套接字地址结构。
 */
final class SockaddrIn {
    static final byte[] IPV4_MAPPED_IPV6_PREFIX = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xff, (byte) 0xff };
    static final int IPV4_ADDRESS_LENGTH = 4;
    static final int IPV6_ADDRESS_LENGTH = 16;
    static final byte[] SOCKADDR_IN6_EMPTY_ARRAY = new byte[Quiche.SIZEOF_SOCKADDR_IN6];
    static final byte[] SOCKADDR_IN_EMPTY_ARRAY = new byte[Quiche.SIZEOF_SOCKADDR_IN];

    private SockaddrIn() { }

    /** 比较两处原生内存中的 sockaddr 是否相等。 */
    static int cmp(long memory, long memory2) {
        return Quiche.sockaddr_cmp(memory, memory2);
    }

    /** 根据地址类型自动选择 IPv4 或 IPv6 写入。 */
    static int setAddress(ByteBuffer memory, InetSocketAddress address) {
        InetAddress addr = address.getAddress();
        return setAddress(addr instanceof Inet6Address, memory, address);
    }

    /** 按 {@code ipv6} 标志写入 IPv4 或 IPv6 sockaddr。 */
    static int setAddress(boolean ipv6, ByteBuffer memory, InetSocketAddress address) {
        if (ipv6) {
            return SockaddrIn.setIPv6(memory, address.getAddress(), address.getPort());
        } else {
            return SockaddrIn.setIPv4(memory, address.getAddress(), address.getPort());
        }
    }

    /**
     *
     * struct sockaddr_in {
     *      sa_family_t    sin_family; // address family: AF_INET
     *      in_port_t      sin_port;   // port in network byte order
     *      struct in_addr sin_addr;   // internet address
     * };
     *
     * // Internet address.
     * struct in_addr {
     *     uint32_t       s_addr;     // address in network byte order
     * };
     *
     */
    static int setIPv4(ByteBuffer memory, InetAddress address, int port) {
        int position = memory.position();
        try {
            // 先清零结构体
            memory.put(SOCKADDR_IN_EMPTY_ARRAY);

            memory.putShort(position + Quiche.SOCKADDR_IN_OFFSETOF_SIN_FAMILY, Quiche.AF_INET);
            memory.putShort(position + Quiche.SOCKADDR_IN_OFFSETOF_SIN_PORT, (short) port);

            byte[] bytes = address.getAddress();
            int offset = 0;
            if (bytes.length == IPV6_ADDRESS_LENGTH) {
                // IPv4 映射 IPv6 地址仅取末 4 字节
                offset = IPV4_MAPPED_IPV6_PREFIX.length;
            }
            assert bytes.length == offset + IPV4_ADDRESS_LENGTH;
            memory.position(position + Quiche.SOCKADDR_IN_OFFSETOF_SIN_ADDR + Quiche.IN_ADDRESS_OFFSETOF_S_ADDR);
            memory.put(bytes, offset, IPV4_ADDRESS_LENGTH);
            return Quiche.SIZEOF_SOCKADDR_IN;
        } finally {
            memory.position(position);
        }
    }

    /**
     * struct sockaddr_in6 {
     *     sa_family_t     sin6_family;   // AF_INET6
     *     in_port_t       sin6_port;     // port number
     *     uint32_t        sin6_flowinfo; // IPv6 flow information
     *     struct in6_addr sin6_addr;     // IPv6 address
     *     uint32_t        sin6_scope_id; /* Scope ID (new in 2.4)
     * };
     *
     * struct in6_addr {
     *     unsigned char s6_addr[16];   // IPv6 address
     * };
     */
    static int setIPv6(ByteBuffer memory, InetAddress address, int port) {
        int position = memory.position();
        try {
            // memset
            memory.put(SOCKADDR_IN6_EMPTY_ARRAY);

            memory.putShort(position + Quiche.SOCKADDR_IN6_OFFSETOF_SIN6_FAMILY, Quiche.AF_INET6);
            memory.putShort(position + Quiche.SOCKADDR_IN6_OFFSETOF_SIN6_PORT, (short) port);

            // flowinfo 已由 memset 置零，跳过
            byte[] bytes = address.getAddress();
            int offset = Quiche.SOCKADDR_IN6_OFFSETOF_SIN6_ADDR + Quiche.IN6_ADDRESS_OFFSETOF_S6_ADDR;

            if (bytes.length == IPV4_ADDRESS_LENGTH) {
                memory.position(position + offset);
                memory.put(IPV4_MAPPED_IPV6_PREFIX);

                memory.position(position + offset + IPV4_MAPPED_IPV6_PREFIX.length);
                memory.put(bytes, 0, IPV4_ADDRESS_LENGTH);

                // 纯 IPv4 映射时 scope_id 保持零
            } else {
                memory.position(position + offset);
                memory.put(bytes, 0, IPV6_ADDRESS_LENGTH);

                memory.putInt(position + Quiche.SOCKADDR_IN6_OFFSETOF_SIN6_SCOPE_ID,
                        ((Inet6Address) address).getScopeId());
            }
            return Quiche.SIZEOF_SOCKADDR_IN6;
        } finally {
            memory.position(position);
        }
    }

    /** 从 {@code sockaddr_in} 解析 IPv4 {@link InetSocketAddress}。 */
    @Nullable
    static InetSocketAddress getIPv4(ByteBuffer memory, byte[] tmpArray) {
        assert tmpArray.length == IPV4_ADDRESS_LENGTH;
        int position = memory.position();

        try {
            int port = memory.getShort(position + Quiche.SOCKADDR_IN_OFFSETOF_SIN_PORT) & 0xFFFF;
            memory.position(position + Quiche.SOCKADDR_IN_OFFSETOF_SIN_ADDR + Quiche.IN_ADDRESS_OFFSETOF_S_ADDR);
            memory.get(tmpArray);
            try {
                return new InetSocketAddress(InetAddress.getByAddress(tmpArray), port);
            } catch (UnknownHostException ignore) {
                return null;
            }
        } finally {
            memory.position(position);
        }
    }

    /** 从 {@code sockaddr_in6} 解析地址；自动识别 IPv4-mapped IPv6。 */
    @Nullable
    static InetSocketAddress getIPv6(ByteBuffer memory, byte[] ipv6Array, byte[] ipv4Array) {
        assert ipv6Array.length == IPV6_ADDRESS_LENGTH;
        assert ipv4Array.length == IPV4_ADDRESS_LENGTH;
        int position = memory.position();

        try {
            int port = memory.getShort(
                    position + Quiche.SOCKADDR_IN6_OFFSETOF_SIN6_PORT) & 0xFFFF;
            memory.position(position + Quiche.SOCKADDR_IN6_OFFSETOF_SIN6_ADDR + Quiche.IN6_ADDRESS_OFFSETOF_S6_ADDR);
            memory.get(ipv6Array);
            if (PlatformDependent.equals(
                    ipv6Array, 0, IPV4_MAPPED_IPV6_PREFIX, 0, IPV4_MAPPED_IPV6_PREFIX.length)) {
                System.arraycopy(ipv6Array, IPV4_MAPPED_IPV6_PREFIX.length, ipv4Array, 0, IPV4_ADDRESS_LENGTH);
                try {
                    return new InetSocketAddress(Inet4Address.getByAddress(ipv4Array), port);
                } catch (UnknownHostException ignore) {
                    return null;
                }
            } else {
                int scopeId = memory.getInt(position + Quiche.SOCKADDR_IN6_OFFSETOF_SIN6_SCOPE_ID);
                try {
                    return new InetSocketAddress(Inet6Address.getByAddress(null, ipv6Array, scopeId), port);
                } catch (UnknownHostException ignore) {
                    return null;
                }
            }
        } finally {
            memory.position(position);
        }
    }
}
