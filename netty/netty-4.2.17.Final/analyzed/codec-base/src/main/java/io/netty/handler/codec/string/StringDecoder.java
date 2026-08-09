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
package io.netty.handler.codec.string;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.internal.ObjectUtil;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 将收到的 {@link ByteBuf} 解码为 {@link String}。
 * <p>
 * 在 TCP/IP 等流式传输上使用时，须配合 {@link ByteToMessageDecoder}（如
 * {@link DelimiterBasedFrameDecoder} 或 {@link LineBasedFrameDecoder}）先完成帧切分。
 * 典型文本行协议配置：
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 *
 * // Decoders
 * pipeline.addLast("frameDecoder", new {@link LineBasedFrameDecoder}(80));
 * pipeline.addLast("stringDecoder", new {@link StringDecoder}(CharsetUtil.UTF_8));
 *
 * // Encoder
 * pipeline.addLast("stringEncoder", new {@link StringEncoder}(CharsetUtil.UTF_8));
 * </pre>
 * 之后可直接以 {@link String} 作为消息：
 * <pre>
 * void channelRead({@link ChannelHandlerContext} ctx, {@link String} msg) {
 *     ch.write("Did you say '" + msg + "'?\n");
 * }
 * </pre>
 */
@Sharable
public class StringDecoder extends MessageToMessageDecoder<ByteBuf> {

    // TODO Use CharsetDecoder instead.
    /** 解码所用字符集。 */
    private final Charset charset;

    /**
     * 使用当前系统默认字符集创建实例。
     */
    public StringDecoder() {
        this(Charset.defaultCharset());
    }

    /**
     * 使用指定字符集创建实例。
     */
    public StringDecoder(Charset charset) {
        super(ByteBuf.class);
        this.charset = ObjectUtil.checkNotNull(charset, "charset");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        out.add(msg.toString(charset));
    }
}
