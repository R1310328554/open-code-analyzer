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
// (BSD License: https://www.opensource.org/licenses/bsd-license)
//
// Copyright (c) 2011, Joe Walnes and contributors
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or
// without modification, are permitted provided that the
// following conditions are met:
//
// * Redistributions of source code must retain the above
// copyright notice, this list of conditions and the
// following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the
// following disclaimer in the documentation and/or other
// materials provided with the distribution.
//
// * Neither the name of the Webbit nor the names of
// its contributors may be used to endorse or promote products
// derived from this software without specific prior written
// permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
// CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
// GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
// BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.ByteOrder;
import java.util.List;

/**
 * WebSocket 协议版本 8（HyBi-10）帧编码器，将 {@link WebSocketFrame} 写回线格式。
 * <p>实现源自 <a href="https://github.com/joewalnes/webbit">webbit</a> 并做了 Netty 适配。
 */
public class WebSocket08FrameEncoder extends MessageToMessageEncoder<WebSocketFrame> implements WebSocketFrameEncoder {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocket08FrameEncoder.class);

    private static final byte OPCODE_CONT = 0x0;
    private static final byte OPCODE_TEXT = 0x1;
    private static final byte OPCODE_BINARY = 0x2;
    private static final byte OPCODE_CLOSE = 0x8;
    private static final byte OPCODE_PING = 0x9;
    private static final byte OPCODE_PONG = 0xA;

    /**
     * 聚集写阈值：未掩码且超过此大小的消息拆成 header + content 两次写；
     * 小于阈值则合并为单 buffer。掩码帧始终一次写出。
     */
    private static final int GATHERING_WRITE_THRESHOLD = 1024;

    private final WebSocketFrameMaskGenerator maskGenerator;

    /**
     * 构造编码器。
     *
     * @param maskPayload
     *            Web socket clients must set this to true to mask payload. Server implementations must set this to
     *            false.
     */
    public WebSocket08FrameEncoder(boolean maskPayload) {
        this(maskPayload ? RandomWebSocketFrameMaskGenerator.INSTANCE : null);
    }

    /**
     * 使用自定义掩码生成器构造编码器。
     *
     * @param maskGenerator
     *            Web socket clients must set this to {@code non null} to mask payload.
     *            Server implementations must set this to {@code null}.
     */
    public WebSocket08FrameEncoder(WebSocketFrameMaskGenerator maskGenerator) {
        super(WebSocketFrame.class);
        this.maskGenerator = maskGenerator;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
        final ByteBuf data = msg.content();

        byte opcode = getOpCode(msg);

        int length = data.readableBytes();

        if (logger.isTraceEnabled()) {
            logger.trace("Encoding WebSocket Frame opCode={} length={}", opcode, length);
        }

        int b0 = 0;
        if (msg.isFinalFragment()) {
            b0 |= 1 << 7;
        }
        b0 |= (msg.rsv() & 0x07) << 4;
        b0 |= opcode & 0x7F;

        if (opcode == OPCODE_PING && length > 125) {
            throw new TooLongFrameException("invalid payload for PING (payload length must be <= 125, was " + length);
        }

        boolean release = true;
        ByteBuf buf = null;
        try {
            int maskLength = maskGenerator != null ? 4 : 0;
            if (length <= 125) {
                int size = 2 + maskLength + length;
                buf = ctx.alloc().buffer(size);
                buf.writeByte(b0);
                byte b = (byte) (maskGenerator != null ? 0x80 | length : length);
                buf.writeByte(b);
            } else if (length <= 0xFFFF) {
                int size = 4 + maskLength;
                if (maskGenerator != null || length <= GATHERING_WRITE_THRESHOLD) {
                    size += length;
                }
                buf = ctx.alloc().buffer(size);
                buf.writeByte(b0);
                buf.writeByte(maskGenerator != null ? 0xFE : 126);
                buf.writeByte(length >>> 8 & 0xFF);
                buf.writeByte(length & 0xFF);
            } else {
                int size = 10 + maskLength;
                if (maskGenerator != null) {
                    size += length;
                }
                buf = ctx.alloc().buffer(size);
                buf.writeByte(b0);
                buf.writeByte(maskGenerator != null ? 0xFF : 127);
                buf.writeLong(length);
            }

            // 写入载荷（按需掩码）
            if (maskGenerator != null) {
                int mask = maskGenerator.nextMask();
                buf.writeInt(mask);

                // 掩码为 0 时可跳过全部 XOR
                if (mask != 0) {
                    if (length > 0) {
                        ByteOrder srcOrder = data.order();
                        ByteOrder dstOrder = buf.order();

                        int i = data.readerIndex();
                        int end = data.writerIndex();

                        if (srcOrder == dstOrder) {
                            // 字节序一致时使用批量 XOR 优化路径
                            // Avoid sign extension on widening primitive conversion
                            long longMask = mask & 0xFFFFFFFFL;
                            longMask |= longMask << 32;

                            // 小端序时需反转掩码以匹配 getInt/writeInt 的字节序
                            if (srcOrder == ByteOrder.LITTLE_ENDIAN) {
                                longMask = Long.reverseBytes(longMask);
                            }

                            for (int lim = end - 7; i < lim; i += 8) {
                                buf.writeLong(data.getLong(i) ^ longMask);
                            }

                            if (i < end - 3) {
                                buf.writeInt(data.getInt(i) ^ (int) longMask);
                                i += 4;
                            }
                        }
                        int maskOffset = 0;
                        for (; i < end; i++) {
                            byte byteData = data.getByte(i);
                            buf.writeByte(byteData ^ WebSocketUtil.byteAtIndex(mask, maskOffset++ & 3));
                        }
                    }
                    out.add(buf);
                } else {
                    addBuffers(buf, data, out);
                }
            } else {
                addBuffers(buf, data, out);
            }
            release = false;
        } finally {
            if (release && buf != null) {
                buf.release();
            }
        }
    }

    /** 根据帧类型返回 opcode 字节。 */
    private static byte getOpCode(WebSocketFrame msg) {
        if (msg instanceof TextWebSocketFrame) {
            return OPCODE_TEXT;
        }
        if (msg instanceof BinaryWebSocketFrame) {
            return OPCODE_BINARY;
        }
        if (msg instanceof PingWebSocketFrame) {
            return OPCODE_PING;
        }
        if (msg instanceof PongWebSocketFrame) {
            return OPCODE_PONG;
        }
        if (msg instanceof CloseWebSocketFrame) {
            return OPCODE_CLOSE;
        }
        if (msg instanceof ContinuationWebSocketFrame) {
            return OPCODE_CONT;
        }
        throw new UnsupportedOperationException("Cannot encode frame of type: " + msg.getClass().getName());
    }

    /** 将 header 与 data 合并或分两次写出，小载荷合并更省开销。 */
    private static void addBuffers(ByteBuf buf, ByteBuf data, List<Object> out) {
        int readableBytes = data.readableBytes();
        if (buf.writableBytes() >= readableBytes) {
            // 载荷较小，合并到同一 buffer
            buf.writeBytes(data);
            out.add(buf);
        } else {
            out.add(buf);
            if (readableBytes > 0) {
                out.add(data.retain());
            }
        }
    }
}
