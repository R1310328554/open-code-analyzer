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
package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.internal.ObjectUtil;

import java.util.List;

import static io.netty.buffer.ByteBufUtil.readBytes;

/**
 * HyBi 00（草案）WebSocket 帧解码器，将 {@link ByteBuf} 解析为 {@link WebSocketFrame}。
 * <p>支持 0x00…0xFF 文本帧与 0x80 前缀二进制帧；示例见 {@code io.netty.example.http.websocket}。
 */
public class WebSocket00FrameDecoder extends ReplayingDecoder<Void> implements WebSocketFrameDecoder {

    static final int DEFAULT_MAX_FRAME_SIZE = 16384;

    private final long maxFrameSize;
    private boolean receivedClosingHandshake;

    public WebSocket00FrameDecoder() {
        this(DEFAULT_MAX_FRAME_SIZE);
    }

    /**
     * 指定最大帧长度，超限抛出 {@link TooLongFrameException}。
     *
     * @param maxFrameSize
     *            the maximum frame size to decode
     */
    public WebSocket00FrameDecoder(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    /**
     * 从 {@link WebSocketDecoderConfig} 读取最大载荷长度等参数。
     *
     * @param decoderConfig
     *            Frames decoder configuration.
     */
    public WebSocket00FrameDecoder(WebSocketDecoderConfig decoderConfig) {
        this.maxFrameSize = ObjectUtil.checkNotNull(decoderConfig, "decoderConfig").maxFramePayloadLength();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 已收到关闭握手后丢弃后续字节
        if (receivedClosingHandshake) {
            in.skipBytes(actualReadableBytes());
            return;
        }

        // 按 HyBi-00 格式解码帧
        byte type = in.readByte();
        WebSocketFrame frame;
        if ((type & 0x80) == 0x80) {
            // 最高位为 1：二进制帧，后跟变长长度字段
            frame = decodeBinaryFrame(ctx, type, in);
        } else {
            // 最高位为 0：以 0xFF 结尾的 UTF-8 文本帧
            frame = decodeTextFrame(ctx, in);
        }

        if (frame != null) {
            out.add(frame);
        }
    }

    private WebSocketFrame decodeBinaryFrame(ChannelHandlerContext ctx, byte type, ByteBuf buffer) {
        long frameSize = 0;
        int lengthFieldSize = 0;
        byte b;
        do {
            b = buffer.readByte();
            frameSize <<= 7;
            frameSize |= b & 0x7f;
            if (frameSize > maxFrameSize) {
                throw new TooLongFrameException("frame length exceeds " + maxFrameSize + ": " + frameSize);
            }
            lengthFieldSize++;
            if (lengthFieldSize > 8) {
                // 长度字段超过 8 字节，疑似恶意对端
                throw new TooLongFrameException("frame length field size exceeds 8: " + lengthFieldSize);
            }
        } while ((b & 0x80) == 0x80);

        if (type == (byte) 0xFF && frameSize == 0) {
            receivedClosingHandshake = true;
            return new CloseWebSocketFrame(true, 0, ctx.alloc().buffer(0));
        }
        ByteBuf payload = readBytes(ctx.alloc(), buffer, (int) frameSize);
        return new BinaryWebSocketFrame(payload);
    }

    private WebSocketFrame decodeTextFrame(ChannelHandlerContext ctx, ByteBuf buffer) {
        int ridx = buffer.readerIndex();
        int rbytes = actualReadableBytes();
        int delimPos = buffer.indexOf(ridx, ridx + rbytes, (byte) 0xFF);
        if (delimPos == -1) {
            // 未找到 0xFF 分隔符，等待更多数据
            if (rbytes > maxFrameSize) {
                // 累积长度超过 maxFrameSize
                throw new TooLongFrameException("frame length exceeds " + maxFrameSize + ": " + rbytes);
            } else {
                // ReplayingDecoder 等待更多输入
                return null;
            }
        }

        int frameSize = delimPos - ridx;
        if (frameSize > maxFrameSize) {
            throw new TooLongFrameException("frame length exceeds " + maxFrameSize + ": " + frameSize);
        }

        ByteBuf binaryData = readBytes(ctx.alloc(), buffer, frameSize);
        buffer.skipBytes(1);

        int ffDelimPos = binaryData.indexOf(binaryData.readerIndex(), binaryData.writerIndex(), (byte) 0xFF);
        if (ffDelimPos >= 0) {
            binaryData.release();
            // 文本载荷内不允许出现 0xFF
            throw new IllegalArgumentException("a text frame should not contain 0xFF.");
        }

        return new TextWebSocketFrame(binaryData);
    }
}
