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
package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 将 {@link DatagramDnsQuery}（或 {@link DnsQuery} 的 {@link AddressedEnvelope}）
 * 编码为 {@link DatagramPacket}。
 */
@ChannelHandler.Sharable
public class DatagramDnsQueryEncoder extends MessageToMessageEncoder<AddressedEnvelope<DnsQuery, InetSocketAddress>> {

    private final DnsQueryEncoder encoder;

    /**
     * 使用 {@linkplain DnsRecordEncoder#DEFAULT 默认记录编码器} 创建编码器。
     */
    public DatagramDnsQueryEncoder() {
        this(DnsRecordEncoder.DEFAULT);
    }

    /**
     * 使用指定 {@code recordEncoder} 创建编码器。
     */
    public DatagramDnsQueryEncoder(DnsRecordEncoder recordEncoder) {
        this.encoder = new DnsQueryEncoder(recordEncoder);
    }

    @Override
    protected void encode(
        ChannelHandlerContext ctx,
        AddressedEnvelope<DnsQuery, InetSocketAddress> in, List<Object> out) throws Exception {

        final InetSocketAddress recipient = in.recipient();
        final DnsQuery query = in.content();
        final ByteBuf buf = allocateBuffer(ctx, in);

        boolean success = false;
        try {
            encoder.encode(query, buf);
            success = true;
        } finally {
            if (!success) {
                buf.release();
            }
        }

        out.add(new DatagramPacket(buf, recipient, null));
    }

    /**
     * 分配用于构造数据报包的 {@link ByteBuf}。
     * 子类可覆写以返回初始容量更精确的缓冲。
     */
    protected ByteBuf allocateBuffer(
        ChannelHandlerContext ctx,
        @SuppressWarnings("unused") AddressedEnvelope<DnsQuery, InetSocketAddress> msg) throws Exception {
        return ctx.alloc().ioBuffer(1024);
    }
}
