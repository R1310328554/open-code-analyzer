/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.sctp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.sctp.SctpMessage;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * A ChannelHandler which transform {@link ByteBuf} to {@link SctpMessage}  and send it through a specific stream
 * with given protocol identifier.
 * Unordered delivery of all messages may be requested by passing unordered = true to the constructor.
 * <p>出站 SCTP 字节流编码器：将 {@link ByteBuf} 包装为指定流号与 PPID 的 {@link SctpMessage}。 构造时 {@code unordered=true} 可设置 SCTP 无序交付标志（U 位）。</p>
 */
public class SctpOutboundByteStreamHandler extends MessageToMessageEncoder<ByteBuf> {
    private final int streamIdentifier;
    private final int protocolIdentifier;
    private final boolean unordered;

    /**
     * @param streamIdentifier      stream number, this should be >=0 or <= max stream number of the association.
     * @param protocolIdentifier    supported application protocol id.
     * <p>有序发送：使用关联内有效流号与应用协议标识。</p>
     */
    public SctpOutboundByteStreamHandler(int streamIdentifier, int protocolIdentifier) {
        this(streamIdentifier, protocolIdentifier, false);
    }

    /**
     * @param streamIdentifier      stream number, this should be >=0 or <= max stream number of the association.
     * @param protocolIdentifier    supported application protocol id.
     * @param unordered             if {@literal true}, SCTP Data Chunks will be sent with the U (unordered) flag set.
     * <p>可显式指定是否无序交付 SCTP 数据块。</p>
     */
    public SctpOutboundByteStreamHandler(int streamIdentifier, int protocolIdentifier, boolean unordered) {
        super(ByteBuf.class);
        this.streamIdentifier = streamIdentifier;
        this.protocolIdentifier = protocolIdentifier;
        this.unordered = unordered;
    }

    @Override
    /** retain 负载并构造 {@link SctpMessage} 写入 out */
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        out.add(new SctpMessage(protocolIdentifier, streamIdentifier, unordered, msg.retain()));
    }
}
