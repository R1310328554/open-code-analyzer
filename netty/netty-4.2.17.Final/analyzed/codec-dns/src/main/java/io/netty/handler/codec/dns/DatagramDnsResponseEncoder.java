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

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 将 {@link DatagramDnsResponse}（或 {@link DnsResponse} 的 {@link AddressedEnvelope}）
 * 编码为 {@link DatagramPacket}。
 */
@ChannelHandler.Sharable
public class DatagramDnsResponseEncoder
    extends MessageToMessageEncoder<AddressedEnvelope<DnsResponse, InetSocketAddress>> {

    private final DnsRecordEncoder recordEncoder;

    /**
     * 使用 {@linkplain DnsRecordEncoder#DEFAULT 默认记录编码器} 创建编码器。
     */
    public DatagramDnsResponseEncoder() {
        this(DnsRecordEncoder.DEFAULT);
    }

    /**
     * 使用指定 {@code recordEncoder} 创建编码器。
     */
    public DatagramDnsResponseEncoder(DnsRecordEncoder recordEncoder) {
        this.recordEncoder = checkNotNull(recordEncoder, "recordEncoder");
    }

    @Override
    protected void encode(ChannelHandlerContext ctx,
                          AddressedEnvelope<DnsResponse, InetSocketAddress> in, List<Object> out) throws Exception {

        final InetSocketAddress recipient = in.recipient();
        final DnsResponse response = in.content();
        final ByteBuf buf = allocateBuffer(ctx, in);

        DnsMessageUtil.encodeDnsResponse(recordEncoder, response, buf);

        out.add(new DatagramPacket(buf, recipient, null));
    }

    /**
     * 分配用于构造数据报包的 {@link ByteBuf}。
     * 子类可覆写以返回初始容量更精确的缓冲。
     */
    protected ByteBuf allocateBuffer(
        ChannelHandlerContext ctx,
        @SuppressWarnings("unused") AddressedEnvelope<DnsResponse, InetSocketAddress> msg) throws Exception {
        return ctx.alloc().ioBuffer(1024);
    }
}
