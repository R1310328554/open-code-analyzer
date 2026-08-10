/*
 * Copyright 2020 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.pcap;

import io.netty.buffer.ByteBuf;

/**
 * IP 层报文封装工具：为 TCP/UDP 载荷构造简化的 IPv4/IPv6 头。
 */
final class IPPacket {

    /** 最大 TTL / 跳数限制。 */
    private static final byte MAX_TTL = (byte) 255;
    /** IPv4 固定头部长度（20 字节）。 */
    private static final short V4_HEADER_SIZE = 20;
    /** IP 协议号：TCP。 */
    private static final byte TCP = 6 & 0xff;
    /** IP 协议号：UDP。 */
    private static final byte UDP = 17 & 0xff;

    /**
     * Version + Traffic class + Flow label
     *
     * <p>IPv6 首 32 位：版本 6 + 流量类别与流标签占位值。</p>
     */
    private static final int IPV6_VERSION_TRAFFIC_FLOW = 60000000;

    private IPPacket() {
        // Prevent outside initialization
        // 工具类禁止实例化
    }

    /**
     * Write IPv4 Packet for UDP Packet
     *
     * @param byteBuf    ByteBuf where IP Packet data will be set
     * @param payload    Payload of UDP
     * @param srcAddress Source IPv4 Address
     * @param dstAddress Destination IPv4 Address
     *
     * <p>封装 IPv4 UDP 报文，地址为 32 位整型网络序。</p>
     */
    static void writeUDPv4(ByteBuf byteBuf, ByteBuf payload, int srcAddress, int dstAddress) {
        writePacketv4(byteBuf, payload, UDP, srcAddress, dstAddress);
    }

    /**
     * Write IPv6 Packet for UDP Packet
     *
     * @param byteBuf    ByteBuf where IP Packet data will be set
     * @param payload    Payload of UDP
     * @param srcAddress Source IPv6 Address
     * @param dstAddress Destination IPv6 Address
     *
     * <p>封装 IPv6 UDP 报文。</p>
     */
    static void writeUDPv6(ByteBuf byteBuf, ByteBuf payload, byte[] srcAddress, byte[] dstAddress) {
        writePacketv6(byteBuf, payload, UDP, srcAddress, dstAddress);
    }

    /**
     * Write IPv4 Packet for TCP Packet
     *
     * @param byteBuf    ByteBuf where IP Packet data will be set
     * @param payload    Payload of TCP
     * @param srcAddress Source IPv4 Address
     * @param dstAddress Destination IPv4 Address
     *
     * <p>封装 IPv4 TCP 报文。</p>
     */
    static void writeTCPv4(ByteBuf byteBuf, ByteBuf payload, int srcAddress, int dstAddress) {
        writePacketv4(byteBuf, payload, TCP, srcAddress, dstAddress);
    }

    /**
     * Write IPv6 Packet for TCP Packet
     *
     * @param byteBuf    ByteBuf where IP Packet data will be set
     * @param payload    Payload of TCP
     * @param srcAddress Source IPv6 Address
     * @param dstAddress Destination IPv6 Address
     *
     * <p>封装 IPv6 TCP 报文。</p>
     */
    static void writeTCPv6(ByteBuf byteBuf, ByteBuf payload, byte[] srcAddress, byte[] dstAddress) {
        writePacketv6(byteBuf, payload, TCP, srcAddress, dstAddress);
    }

    /** 写入简化 IPv4 头并附加 L4 载荷；校验和置 0。 */
    private static void writePacketv4(ByteBuf byteBuf, ByteBuf payload, int protocol, int srcAddress,
                                      int dstAddress) {

        byteBuf.writeByte(0x45);      //  Version + IHL / 版本 4 + 头长 5 字
        byteBuf.writeByte(0x00);      //  DSCP / 区分服务
        byteBuf.writeShort(V4_HEADER_SIZE + payload.readableBytes()); // Length / 总长度
        byteBuf.writeShort(0x0000);   // Identification / 标识
        byteBuf.writeShort(0x0000);   // Fragment / 片偏移与标志
        byteBuf.writeByte(MAX_TTL);   // TTL / 生存时间
        byteBuf.writeByte(protocol);  // Protocol / 上层协议
        byteBuf.writeShort(0);        // Checksum / 校验和（未计算）
        byteBuf.writeInt(srcAddress); // Source IPv4 Address / 源地址
        byteBuf.writeInt(dstAddress); // Destination IPv4 Address / 目的地址
        byteBuf.writeBytes(payload);  // Payload of L4 / 四层载荷
    }

    /** 写入简化 IPv6 头并附加 L4 载荷。 */
    private static void writePacketv6(ByteBuf byteBuf, ByteBuf payload, int protocol, byte[] srcAddress,
                                      byte[] dstAddress) {

        byteBuf.writeInt(IPV6_VERSION_TRAFFIC_FLOW); // Version  + Traffic class + Flow label
        byteBuf.writeShort(payload.readableBytes()); // Payload length / 载荷长度
        byteBuf.writeByte(protocol & 0xff); // Next header / 下一首部
        byteBuf.writeByte(MAX_TTL);         // Hop limit / 跳数限制
        byteBuf.writeBytes(srcAddress);     // Source IPv6 Address / 源地址
        byteBuf.writeBytes(dstAddress);     // Destination IPv6 Address / 目的地址
        byteBuf.writeBytes(payload);        // Payload of L4 / 四层载荷
    }
}
