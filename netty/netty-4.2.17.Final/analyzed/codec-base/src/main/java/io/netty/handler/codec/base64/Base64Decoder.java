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
package io.netty.handler.codec.base64;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.internal.ObjectUtil;

import java.util.List;

/**
 * 将 Base64 编码的 {@link ByteBuf}（或 US-ASCII {@link String}）解码为 {@link ByteBuf}。
 * <p>
 * 在 TCP 等流式传输中须配合 {@link DelimiterBasedFrameDecoder} 等
 * {@link ByteToMessageDecoder} 先切分完整 Base64 帧。  A typical decoder
 * setup for TCP/IP would be:
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 *
 * // Decoders
 * pipeline.addLast("frameDecoder", new {@link DelimiterBasedFrameDecoder}(80, {@link Delimiters#nulDelimiter()}));
 * pipeline.addLast("base64Decoder", new {@link Base64Decoder}());
 *
 * // Encoder
 * pipeline.addLast("base64Encoder", new {@link Base64Encoder}());
 * </pre>
 */
@Sharable
public class Base64Decoder extends MessageToMessageDecoder<ByteBuf> {

    /** 使用的 Base64 字母表方言。 */
    private final Base64Dialect dialect;

    /** 使用 {@link Base64Dialect#STANDARD} 创建解码器。 */
    public Base64Decoder() {
        this(Base64Dialect.STANDARD);
    }

    /** @param dialect Base64 编码方言 */
    public Base64Decoder(Base64Dialect dialect) {
        super(ByteBuf.class);
        this.dialect = ObjectUtil.checkNotNull(dialect, "dialect");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        out.add(Base64.decode(msg, msg.readerIndex(), msg.readableBytes(), dialect));
    }
}
