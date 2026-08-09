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
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.internal.ObjectUtil;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 将 {@link String} 编码为 {@link ByteBuf}。
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
 * pipeline.addLast("stringEncoder", new {@link StringEncoder}(CharsetUtil.UTF_8));
 * </pre>
 * 之后可直接以 {@link String} 作为消息读写：
 * <pre>
 * void channelRead({@link ChannelHandlerContext} ctx, {@link String} msg) {
 *     ch.write("Did you say '" + msg + "'?\n");
 * }
 * </pre>
 */
@Sharable
public class StringEncoder extends MessageToMessageEncoder<CharSequence> {

    /** 编码所用字符集。 */
    private final Charset charset;

    /**
     * 使用当前系统默认字符集创建实例。
     */
    public StringEncoder() {
        this(Charset.defaultCharset());
    }

    /**
     * 使用指定字符集创建实例。
     */
    public StringEncoder(Charset charset) {
        super(CharSequence.class);
        this.charset = ObjectUtil.checkNotNull(charset, "charset");
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {
        if (msg.length() == 0) {
            return;
        }

        out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), charset));
    }
}
