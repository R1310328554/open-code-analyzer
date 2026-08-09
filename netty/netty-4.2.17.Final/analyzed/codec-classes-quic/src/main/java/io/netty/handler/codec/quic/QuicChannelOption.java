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

import io.netty.channel.ChannelOption;

/**
 * QUIC 专用的 {@link ChannelOption} 选项集合。
 */
public final class QuicChannelOption<T> extends ChannelOption<T> {

    /**
     * 为 {@code true} 时，{@link QuicStreamChannel} 读取 {@link QuicStreamFrame} 并沿 pipeline 向下游传播；
     * 为 {@code false} 时读取 {@link io.netty.buffer.ByteBuf}，并将 FIN 标志转换为相应事件。
     */
    public static final ChannelOption<Boolean> READ_FRAMES =
            valueOf(QuicChannelOption.class, "READ_FRAMES");

    /**
     * 为 {@link QuicChannel} 启用
     * <a href="https://quiclog.github.io/internet-drafts/draft-marx-qlog-main-schema.html">qlog</a> 日志。
     */
    public static final ChannelOption<QLogConfiguration> QLOG = valueOf(QuicChannelOption.class, "QLOG");

    /**
     * 在可用时为 QUIC 报文启用
     * <a href="https://blog.cloudflare.com/accelerating-udp-packet-transmission-for-quic/">GSO</a>（通用分段卸载）。
     */
    public static final ChannelOption<SegmentedDatagramPacketAllocator> SEGMENTED_DATAGRAM_PACKET_ALLOCATOR =
            valueOf(QuicChannelOption.class, "SEGMENTED_DATAGRAM_PACKET_ALLOCATOR");

    /** 私有构造，禁止外部实例化。 */
    @SuppressWarnings({ "deprecation" })
    private QuicChannelOption() {
        super(null);
    }
}
