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
package io.netty.handler.codec.bytes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * 将收到的 {@link ByteBuf} 解码为 {@code byte[]}。
 * <p>
 * TCP/IP 场景下的典型管线配置：
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 *
 * // Decoders
 * pipeline.addLast("frameDecoder",
 *                  new {@link LengthFieldBasedFrameDecoder}(1048576, 0, 4, 0, 4));
 * pipeline.addLast("bytesDecoder",
 *                  new {@link ByteArrayDecoder}());
 *
 * // Encoder
 * pipeline.addLast("frameEncoder", new {@link LengthFieldPrepender}(4));
 * pipeline.addLast("bytesEncoder", new {@link ByteArrayEncoder}());
 * </pre>
 * 配置完成后，业务层可直接以 {@code byte[]} 作为消息类型：
 * <pre>
 * void channelRead({@link ChannelHandlerContext} ctx, byte[] bytes) {
 *     ...
 * }
 * </pre>
 */
public class ByteArrayDecoder extends MessageToMessageDecoder<ByteBuf> {
    /** 创建解码器，入站消息类型为 {@link ByteBuf}。 */
    /** 创建解码器，入站消息类型为 {@link ByteBuf}。 */
    public ByteArrayDecoder() {
        super(ByteBuf.class);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // 将 ByteBuf 内容复制为 byte 数组
        out.add(ByteBufUtil.getBytes(msg));
    }
}
