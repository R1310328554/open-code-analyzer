/*
 * Copyright 2016 The Netty Project
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
package io.netty.handler.codec.string;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 为 {@link String} 追加行分隔符并编码为 {@link ByteBuf}。
 * <p>
 * 基于 TCP/IP 的文本行协议典型配置如下：
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 *
 * // Decoders
 * pipeline.addLast("frameDecoder", new {@link LineBasedFrameDecoder}(80));
 * pipeline.addLast("stringDecoder", new {@link StringDecoder}(CharsetUtil.UTF_8));
 *
 * // Encoder
 * pipeline.addLast("lineEncoder", new {@link LineEncoder}(LineSeparator.UNIX, CharsetUtil.UTF_8));
 * </pre>
 * 之后可直接以 {@link String} 作为消息读写：
 * <pre>
 * void channelRead({@link ChannelHandlerContext} ctx, {@link String} msg) {
 *     ch.write("Did you say '" + msg + "'?");
 * }
 * </pre>
 */
@Sharable
public class LineEncoder extends MessageToMessageEncoder<CharSequence> {

    /** 字符集，用于将字符串编码为字节。 */
    private final Charset charset;
    /** 行分隔符的字节形式（已按 charset 编码）。 */
    private final byte[] lineSeparator;

    /**
     * 使用当前系统行分隔符与 UTF-8 字符集创建实例。
     */
    public LineEncoder() {
        this(LineSeparator.DEFAULT, CharsetUtil.UTF_8);
    }

    /**
     * 使用指定行分隔符与 UTF-8 字符集创建实例。
     */
    public LineEncoder(LineSeparator lineSeparator) {
        this(lineSeparator, CharsetUtil.UTF_8);
    }

    /**
     * 使用指定字符集与默认行分隔符创建实例。
     */
    public LineEncoder(Charset charset) {
        this(LineSeparator.DEFAULT, charset);
    }

    /**
     * 使用指定行分隔符与字符集创建实例。
     */
    public LineEncoder(LineSeparator lineSeparator, Charset charset) {
        super(CharSequence.class);
        this.charset = ObjectUtil.checkNotNull(charset, "charset");
        this.lineSeparator = ObjectUtil.checkNotNull(lineSeparator, "lineSeparator").value().getBytes(charset);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {
        // 预分配含行分隔符长度的缓冲区，再写入分隔符字节
        ByteBuf buffer = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), charset, lineSeparator.length);
        buffer.writeBytes(lineSeparator);
        out.add(buffer);
    }
}
