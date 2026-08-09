/*
 * Copyright 2016 The Netty Project
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
package io.netty.handler.codec.dns;

import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.SocketProtocolFamily;
import io.netty.util.NetUtil;

import java.net.InetAddress;
import java.util.Arrays;

/**
 * {@link DnsOptEcsRecord} 的默认实现，携带客户端子网（ECS）信息。
 */
public final class DefaultDnsOptEcsRecord extends AbstractDnsOptPseudoRrRecord implements DnsOptEcsRecord {
    private final int srcPrefixLength;
    private final byte[] address;

    /**
     * 创建完整配置的 ECS 记录。
     *
     * @param maxPayloadSize 建议的最大 UDP 负载（字节）
     * @param extendedRcode 扩展响应码
     * @param version EDNS 版本
     * @param srcPrefixLength 源地址前缀长度
     * @param address {@link InetAddress} 的字节表示
     */
    public DefaultDnsOptEcsRecord(int maxPayloadSize, int extendedRcode, int version,
                                  int srcPrefixLength, byte[] address) {
        super(maxPayloadSize, extendedRcode, version);
        this.srcPrefixLength = srcPrefixLength;
        this.address = verifyAddress(address).clone();
    }

    /**
     * 创建 ECS 记录（扩展码与版本默认为 0）。
     *
     * @param maxPayloadSize 建议的最大 UDP 负载（字节）
     * @param srcPrefixLength 源地址前缀长度
     * @param address {@link InetAddress} 的字节表示
     */
    public DefaultDnsOptEcsRecord(int maxPayloadSize, int srcPrefixLength, byte[] address) {
        this(maxPayloadSize, 0, 0, srcPrefixLength, address);
    }

    /**
     * 按 {@link InternetProtocolFamily} 创建 ECS 记录。
     *
     * @param maxPayloadSize    建议的最大 UDP 负载（字节）
     * @param protocolFamily    协议族，应与发送查询时一致
     * @deprecated              请使用 {@link DefaultDnsOptEcsRecord#DefaultDnsOptEcsRecord(int, SocketProtocolFamily)}
     */
    @Deprecated
    public DefaultDnsOptEcsRecord(int maxPayloadSize, InternetProtocolFamily protocolFamily) {
        this(maxPayloadSize, 0, 0, 0, protocolFamily.localhost().getAddress());
    }

    /**
     * 按 {@link SocketProtocolFamily} 创建 ECS 记录。
     *
     * @param maxPayloadSize        建议的最大 UDP 负载（字节）
     * @param socketProtocolFamily  套接字协议族，应与发送查询时一致
     */
    public DefaultDnsOptEcsRecord(int maxPayloadSize, SocketProtocolFamily socketProtocolFamily) {
        this(maxPayloadSize, 0, 0, 0, localAddress(socketProtocolFamily));
    }

    private static byte[] localAddress(SocketProtocolFamily family) {
        switch (family) {
            case INET:
                return NetUtil.LOCALHOST4.getAddress();
            case INET6:
                return NetUtil.LOCALHOST6.getAddress();
            default:
                return null;
        }
    }

    private static byte[] verifyAddress(byte[] bytes) {
        if (bytes != null && bytes.length == 4 || bytes.length == 16) {
            return bytes;
        }
        throw new IllegalArgumentException("bytes.length must either 4 or 16");
    }

    @Override
    public int sourcePrefixLength() {
        return srcPrefixLength;
    }

    @Override
    public int scopePrefixLength() {
        return 0;
    }

    @Override
    public byte[] address() {
        return address.clone();
    }

    @Override
    public String toString() {
        StringBuilder sb = toStringBuilder();
        sb.setLength(sb.length() - 1);
        return sb.append(" address:")
          .append(Arrays.toString(address))
          .append(" sourcePrefixLength:")
          .append(sourcePrefixLength())
          .append(" scopePrefixLength:")
          .append(scopePrefixLength())
          .append(')').toString();
    }
}
