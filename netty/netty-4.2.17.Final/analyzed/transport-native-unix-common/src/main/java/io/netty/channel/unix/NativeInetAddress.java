/*
 * Copyright 2015 The Netty Project
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
package io.netty.channel.unix;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * <strong>Internal usage only!</strong>
 * <p>JNI 侧 Inet 地址封装：统一 IPv4/IPv6 为 16 字节布局，附带 scopeId； 供 {@link Socket} 与 {@link DatagramSocketAddress} 解析/构造套接字地址。</p>
 */
public final class NativeInetAddress {
    /** IPv4-mapped IPv6 前缀：{@code ::ffff:0:0/96} */
    private static final byte[] IPV4_MAPPED_IPV6_PREFIX = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xff, (byte) 0xff };
    /** 16 字节地址（IPv4 时为 mapped IPv6 布局） */
    final byte[] address;
    /** IPv6 链路/站点 scope 标识 */
    final int scopeId;

    /** 从 {@link InetAddress} 构造 JNI 可用地址（IPv4 自动映射） */
    public static NativeInetAddress newInstance(InetAddress addr) {
        byte[] bytes = addr.getAddress();
        if (addr instanceof Inet6Address) {
            return new NativeInetAddress(bytes, ((Inet6Address) addr).getScopeId());
        } else {
            // IPv4 转为 IPv4-mapped IPv6 布局
            return new NativeInetAddress(ipv4MappedIpv6Address(bytes));
        }
    }

    public NativeInetAddress(byte[] address, int scopeId) {
        this.address = address;
        this.scopeId = scopeId;
    }

    public NativeInetAddress(byte[] address) {
        this(address, 0);
    }

    /** 返回底层 16 字节地址数组 */
    public byte[] address() {
        return address;
    }

    /** 返回 IPv6 scopeId（IPv4 时为 0） */
    public int scopeId() {
        return scopeId;
    }

    /** 分配并填充 IPv4-mapped IPv6 地址 */
    public static byte[] ipv4MappedIpv6Address(byte[] ipv4) {
        byte[] address = new byte[16];
        copyIpv4MappedIpv6Address(ipv4, address);
        return address;
    }

    /** 将 4 字节 IPv4 写入目标数组的 mapped IPv6 布局 */
    public static void copyIpv4MappedIpv6Address(byte[] ipv4, byte[] ipv6) {
        System.arraycopy(IPV4_MAPPED_IPV6_PREFIX, 0, ipv6, 0, IPV4_MAPPED_IPV6_PREFIX.length);
        System.arraycopy(ipv4, 0, ipv6, 12, ipv4.length);
    }

    public static InetSocketAddress address(byte[] addr, int offset, int len) {
        // 末尾 4 字节始终为网络序端口号
        final int port = decodeInt(addr, offset + len - 4);
        final InetAddress address;
        try {
            switch (len) {
                // 8 字节：4 字节 IPv4 + 4 字节端口
                case 8:
                    byte[] ipv4 = new byte[4];
                    System.arraycopy(addr, offset, ipv4, 0, 4);
                    address = InetAddress.getByAddress(ipv4);
                    break;

                // 24 字节：16 字节 IPv6 + 4 字节 scopeId + 4 字节端口
                case 24:
                    byte[] ipv6 = new byte[16];
                    System.arraycopy(addr, offset, ipv6, 0, 16);
                    int scopeId = decodeInt(addr, offset + len  - 8);
                    // scopeId 非 0 或为链路本地地址时才传入 Inet6Address
                    // See also https://man7.org/linux/man-pages/man7/ipv6.7.html
                    if (scopeId != 0 || (ipv6[0] == (byte) 0xfe && ipv6[1] == (byte) 0x80)) {
                        address = Inet6Address.getByAddress(null, ipv6, scopeId);
                    } else {
                        address = InetAddress.getByAddress(null, ipv6);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported length: " + len + " (allowed: 8 or 24)");
            }
            return new InetSocketAddress(address, port);
        } catch (UnknownHostException e) {
            throw new Error(e); // Should never happen
        }
    }

    /** 从大端字节数组解码 32 位整数（端口/scopeId） */
    static int decodeInt(byte[] addr, int index) {
        return  (addr[index]     & 0xff) << 24 |
                (addr[index + 1] & 0xff) << 16 |
                (addr[index + 2] & 0xff) <<  8 |
                addr[index + 3] & 0xff;
    }
}
