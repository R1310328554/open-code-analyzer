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
 * 构造用于 PCAP 抓包的 UDP 报文头与载荷。
 *
 * <p>UDP 无连接状态，{@link PcapWriteHandler} 直接封装 Datagram  payload。</p>
 */
final class UDPPacket {

    /** UDP 固定头部长度（字节）。 */
    private static final short UDP_HEADER_SIZE = 8;

    private UDPPacket() {
        // Prevent outside initialization
        // 工具类，禁止实例化
    }

    /**
     * Write UDP Packet
     *
     * @param byteBuf ByteBuf where Packet data will be set
     * @param payload Payload of this Packet
     * @param srcPort Source Port
     * @param dstPort Destination Port
     *
     * <p>写入 8 字节 UDP 头（含长度与占位校验和）及 payload。</p>
     */
    static void writePacket(ByteBuf byteBuf, ByteBuf payload, int srcPort, int dstPort) {
        byteBuf.writeShort(srcPort); // Source Port — 源端口
        byteBuf.writeShort(dstPort); // Destination Port — 目的端口
        byteBuf.writeShort(UDP_HEADER_SIZE + payload.readableBytes()); // 头长 + 载荷长度
        byteBuf.writeShort(0x0001);  // Checksum — 占位校验和
        byteBuf.writeBytes(payload); //  Payload of Data — UDP 载荷
    }
}
