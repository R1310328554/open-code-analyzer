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
package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.LeakPresenceDetector;

import java.util.List;

/**
 * HyBi 00 WebSocket 帧编码器，将 {@link WebSocketFrame} 写回 {@link ByteBuf} 线格式。
 * <p>{@link Sharable} 可安全复用于多条连接；示例见 {@code io.netty.example.http.websocket}。
 */
@Sharable
public class WebSocket00FrameEncoder extends MessageToMessageEncoder<WebSocketFrame> implements WebSocketFrameEncoder {
    private static final ByteBuf _0X00 = LeakPresenceDetector.staticInitializer(() -> Unpooled.unreleasableBuffer(
            Unpooled.directBuffer(1, 1).writeByte(0x00)).asReadOnly());
    private static final ByteBuf _0XFF = LeakPresenceDetector.staticInitializer(() -> Unpooled.unreleasableBuffer(
            Unpooled.directBuffer(1, 1).writeByte((byte) 0xFF)).asReadOnly());
    private static final ByteBuf _0XFF_0X00 = LeakPresenceDetector.staticInitializer(() -> Unpooled.unreleasableBuffer(
            Unpooled.directBuffer(2, 2).writeByte((byte) 0xFF).writeByte(0x00)).asReadOnly());

    public WebSocket00FrameEncoder() {
        super(WebSocketFrame.class);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
        if (msg instanceof TextWebSocketFrame) {
            // 文本：0x00 + UTF-8 载荷 + 0xFF
            ByteBuf data = msg.content();

            out.add(_0X00.duplicate());
            out.add(data.retain());
            out.add(_0XFF.duplicate());
        } else if (msg instanceof CloseWebSocketFrame) {
            // 关闭帧 0xFF 0x00；duplicate 以支持多次写入（netty#2768）
            out.add(_0XFF_0X00.duplicate());
        } else {
            // 二进制：0x80 + 变长长度 + 载荷
            ByteBuf data = msg.content();
            int dataLen = data.readableBytes();

            ByteBuf buf = ctx.alloc().buffer(5);
            boolean release = true;
            try {
                // 写入类型字节 0x80
                buf.writeByte((byte) 0x80);

                // 7 位一组的变长长度编码
                int b1 = dataLen >>> 28 & 0x7F;
                int b2 = dataLen >>> 14 & 0x7F;
                int b3 = dataLen >>> 7 & 0x7F;
                int b4 = dataLen & 0x7F;
                if (b1 == 0) {
                    if (b2 == 0) {
                        if (b3 != 0) {
                            buf.writeByte(b3 | 0x80);
                        }
                        buf.writeByte(b4);
                    } else {
                        buf.writeByte(b2 | 0x80);
                        buf.writeByte(b3 | 0x80);
                        buf.writeByte(b4);
                    }
                } else {
                    buf.writeByte(b1 | 0x80);
                    buf.writeByte(b2 | 0x80);
                    buf.writeByte(b3 | 0x80);
                    buf.writeByte(b4);
                }

                // 追加二进制载荷
                out.add(buf);
                out.add(data.retain());
                release = false;
            } finally {
                if (release) {
                    buf.release();
                }
            }
        }
    }
}
