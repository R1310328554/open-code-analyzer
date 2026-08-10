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
 * 构造用于 PCAP 抓包的合成 TCP 报文（非真实校验和，供 Wireshark 解析会话状态）。
 *
 * <p>{@link PcapWriteHandler} 用此类模拟三次握手、ACK、FIN/RST 等控制段。</p>
 */
final class TCPPacket {

    /**
     * Data Offset + Reserved Bits.
     *
     * <p>数据偏移 5（20 字节头）+ 保留位，与标志位 OR 后写入。</p>
     */
    private static final short OFFSET = 0x5000;

    private TCPPacket() {
        // Prevent outside initialization
        // 工具类，禁止实例化
    }

    /**
     * Write TCP Packet
     *
     * @param byteBuf ByteBuf where Packet data will be set
     * @param payload Payload of this Packet
     * @param srcPort Source Port
     * @param dstPort Destination Port
     *
     * <p>按给定序号、确认号与标志位序列化 TCP 头，可选附加 payload。</p>
     */
    static void writePacket(ByteBuf byteBuf, ByteBuf payload, long segmentNumber, long ackNumber, int srcPort,
                            int dstPort, TCPFlag... tcpFlags) {

        byteBuf.writeShort(srcPort);     // Source Port — 源端口
        byteBuf.writeShort(dstPort);     // Destination Port — 目的端口
        byteBuf.writeInt((int) segmentNumber); // Segment Number — 序列号
        byteBuf.writeInt((int) ackNumber);     // Acknowledgment Number — 确认号
        byteBuf.writeShort(OFFSET | TCPFlag.getFlag(tcpFlags)); // Flags — 数据偏移与 TCP 标志
        byteBuf.writeShort(65535);       // Window Size — 窗口大小（固定最大值便于展示）
        byteBuf.writeShort(0x0001);      // Checksum — 占位校验和（PCAP 模拟用）
        byteBuf.writeShort(0);           // Urgent Pointer — 紧急指针

        if (payload != null) {
            byteBuf.writeBytes(payload); //  Payload of Data — 应用层数据
        }
    }

    /**
     * TCP 控制标志位，对应首部第 13–14 字节的低 8 位。
     */
    enum TCPFlag {
        /** 结束连接 */
        FIN(1),
        /** 同步序号，建立连接 */
        SYN(1 << 1),
        /** 复位连接 */
        RST(1 << 2),
        /** 推送，尽快交付给应用 */
        PSH(1 << 3),
        /** 确认有效 */
        ACK(1 << 4),
        /** 紧急指针有效 */
        URG(1 << 5),
        /** ECN-Echo */
        ECE(1 << 6),
        /** 拥塞窗口减小 */
        CWR(1 << 7);

        private final int value;

        TCPFlag(int value) {
            this.value = value;
        }

        /** 将多个标志 OR 合并为 16 位标志字段的低字节。 */
        static int getFlag(TCPFlag... tcpFlags) {
            int flags = 0;

            for (TCPFlag tcpFlag : tcpFlags) {
                flags |= tcpFlag.value;
            }

            return  flags;
        }
    }
}
