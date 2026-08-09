/*
 * Copyright 2019 The Netty Project
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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 基于 TCP 的 DNS 查询编码器（RFC 7766）。
 * <p>
 * 在报文前写入 2 字节长度前缀，再编码 {@link DnsQuery} 主体。
 */
@ChannelHandler.Sharable
public final class TcpDnsQueryEncoder extends MessageToByteEncoder<DnsQuery> {

    private final DnsQueryEncoder encoder;

    /**
     * 使用 {@linkplain DnsRecordEncoder#DEFAULT 默认记录编码器} 创建编码器。
     */
    public TcpDnsQueryEncoder() {
        this(DnsRecordEncoder.DEFAULT);
    }

    /**
     * 使用指定 {@code recordEncoder} 创建编码器。
     */
    public TcpDnsQueryEncoder(DnsRecordEncoder recordEncoder) {
        super(DnsQuery.class);
        this.encoder = new DnsQueryEncoder(recordEncoder);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DnsQuery msg, ByteBuf out) throws Exception {
        // RFC 7766：长度字段为 2 字节
        // See https://tools.ietf.org/html/rfc7766#section-8
        out.writerIndex(out.writerIndex() + 2);
        encoder.encode(msg, out);

        // 回填实际报文长度（不含长度字段本身）
        out.setShort(0, out.readableBytes() - 2);
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, @SuppressWarnings("unused") DnsQuery msg,
                                     boolean preferDirect) {
        if (preferDirect) {
            return ctx.alloc().ioBuffer(1024);
        } else {
            return ctx.alloc().heapBuffer(1024);
        }
    }
}
