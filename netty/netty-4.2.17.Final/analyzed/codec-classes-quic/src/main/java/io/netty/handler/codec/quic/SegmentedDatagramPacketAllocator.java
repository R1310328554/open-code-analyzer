/*
 * Copyright 2021 The Netty Project
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

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

/**
 * 分配支持 UDP_SEGMENT（GSO 分段卸载）的分段 {@link DatagramPacket} 的分配器接口。
 * 在 Linux 上可将多个 UDP 段合并为一次系统调用发送。
 */
@FunctionalInterface
public interface SegmentedDatagramPacketAllocator {

    /** 不支持 UDP_SEGMENT 时的占位实现，调用 {@link #newPacket} 将抛出异常。 */
    SegmentedDatagramPacketAllocator NONE = new SegmentedDatagramPacketAllocator() {
        @Override
        public int maxNumSegments() {
            return 0;
        }

        @Override
        public DatagramPacket newPacket(ByteBuf buffer, int segmentSize, InetSocketAddress remoteAddress) {
            throw new UnsupportedOperationException();
        }
    };

    /**
     * 每个 UDP 报文允许的最大分段数，默认 {@code 10}，实现类可覆盖。
     *
     * @return  the segments.
     */
    default int maxNumSegments() {
        return 10;
    }

    /**
     * 创建带 UDP_SEGMENT 控制信息的分段 {@link DatagramPacket}。
     *
     * @param buffer        the {@link ByteBuf} that is used as content.
     * @param segmentSize   the size of each segment.
     * @param remoteAddress the remote address to send to.
     * @return              the packet.
     */
    DatagramPacket newPacket(ByteBuf buffer, int segmentSize, InetSocketAddress remoteAddress);
}
