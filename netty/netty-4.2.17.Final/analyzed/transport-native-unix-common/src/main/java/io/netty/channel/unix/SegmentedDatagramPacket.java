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
package io.netty.channel.unix;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.internal.ObjectUtil;

import java.net.InetSocketAddress;

/**
 * Allows to use <a href="https://blog.cloudflare.com/accelerating-udp-packet-transmission-for-quic/">GSO</a>
 * if the underlying OS supports it. Before using this you should ensure your system support it.
 * <p>分段 UDP 数据报：在支持 UDP GSO 的内核上，单次 sendmsg 可发送多个等长 segment（如 QUIC）； 负载须为连续 {@link ByteBuf}。</p>
 */
public class SegmentedDatagramPacket extends DatagramPacket {

    /** 每个 UDP segment 的字节大小（末段可更短） */
    private final int segmentSize;

    /**
     * Create a new instance.
     * <p>创建新实例。</p>
     *
     * @param data          the {@link ByteBuf} which must be continguous.
     * @param segmentSize   the segment size.
     * @param recipient     the recipient.
     */
    /** 构造仅含接收方地址的分段数据报 */
    public SegmentedDatagramPacket(ByteBuf data, int segmentSize, InetSocketAddress recipient) {
        super(data, recipient);
        this.segmentSize = ObjectUtil.checkPositive(segmentSize, "segmentSize");
    }

    /**
     * Create a new instance.
     * <p>创建新实例。</p>
     *
     * @param data          the {@link ByteBuf} which must be continguous.
     * @param segmentSize   the segment size.
     * @param recipient     the recipient.
     */
    /** 构造含发送方与接收方地址的分段数据报 */
    public SegmentedDatagramPacket(ByteBuf data, int segmentSize,
                                   InetSocketAddress recipient, InetSocketAddress sender) {
        super(data, recipient, sender);
        this.segmentSize = ObjectUtil.checkPositive(segmentSize, "segmentSize");
    }

    /**
     * Return the size of each segment (the last segment can be smaller).
     *
     * @return size of segments.
     * <p>返回 GSO segment 大小（字节）。</p>
     */
    public int segmentSize() {
        return segmentSize;
    }

    /** 深拷贝负载并保留 segment 大小与地址 */
    @Override
    public SegmentedDatagramPacket copy() {
        return new SegmentedDatagramPacket(content().copy(), segmentSize, recipient(), sender());
    }

    /** 共享底层存储的浅拷贝视图 */
    @Override
    public SegmentedDatagramPacket duplicate() {
        return new SegmentedDatagramPacket(content().duplicate(), segmentSize, recipient(), sender());
    }

    /** 返回引用计数 +1 的 duplicate 视图 */
    @Override
    public SegmentedDatagramPacket retainedDuplicate() {
        return new SegmentedDatagramPacket(content().retainedDuplicate(), segmentSize, recipient(), sender());
    }

    /** 替换负载 {@link ByteBuf}，segment 大小与地址不变 */
    @Override
    public SegmentedDatagramPacket replace(ByteBuf content) {
        return new SegmentedDatagramPacket(content, segmentSize, recipient(), sender());
    }

    @Override
    public SegmentedDatagramPacket retain() {
        super.retain();
        return this;
    }

    @Override
    public SegmentedDatagramPacket retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public SegmentedDatagramPacket touch() {
        super.touch();
        return this;
    }

    @Override
    public SegmentedDatagramPacket touch(Object hint) {
        super.touch(hint);
        return this;
    }
}
