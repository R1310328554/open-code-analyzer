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
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.internal.ObjectUtil;

import java.util.List;

/**
 * 将 {@link ByteBuf} 编码为 Base64 格式的 {@link ByteBuf}。
 * <p>
 * TCP/IP 场景下的典型管线配置：
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
public class Base64Encoder extends MessageToMessageEncoder<ByteBuf> {

    /** 是否在输出中插入换行符。 */
    /** 是否在输出中插入换行符。 */
    private final boolean breakLines;
    /** 使用的 Base64 方言。 */
    /** 使用的 Base64 方言。 */
    private final Base64Dialect dialect;

    /** 默认启用换行，使用 {@link Base64Dialect#STANDARD} 方言。 */
    /** 默认启用换行，使用 {@link Base64Dialect#STANDARD} 方言。 */
    public Base64Encoder() {
        this(true);
    }

    /**
     * 指定是否在编码结果中插入换行符。
     *
      * @param breakLines 为 {@code true} 时在输出中按行插入换行
     */
    public Base64Encoder(boolean breakLines) {
        this(breakLines, Base64Dialect.STANDARD);
    }

    /**
     * 指定换行策略与 Base64 方言。
     *
      * @param breakLines 为 {@code true} 时在输出中按行插入换行
      * @param dialect    编码所用的 {@link Base64Dialect}
     */
    public Base64Encoder(boolean breakLines, Base64Dialect dialect) {
        super(ByteBuf.class);
        this.dialect = ObjectUtil.checkNotNull(dialect, "dialect");
        this.breakLines = breakLines;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // 对可读字节段执行 Base64 编码，结果追加到 out
        out.add(Base64.encode(msg, msg.readerIndex(), msg.readableBytes(), breakLines, dialect));
    }
}
