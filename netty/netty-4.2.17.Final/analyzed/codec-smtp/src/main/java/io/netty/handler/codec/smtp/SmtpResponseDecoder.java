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
package io.netty.handler.codec.smtp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.UnstableApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Decoder for SMTP responses.
 * <p>基于 {@link LineBasedFrameDecoder} 按行解析服务端应答。格式 {@code NNN[ -| ]detail}：
 * {@code -} 表示多行中间行，{@code 空格} 表示最后一行并产出完整 {@link SmtpResponse}；
 * 中间行暂存于 {@link #details}，末行合并后重置。</p>
 */
@UnstableApi
public final class SmtpResponseDecoder extends LineBasedFrameDecoder {

    /** 多行应答尚未结束时的累积 detail 行；末行 {@code NNN } 产出后置 {@code null}。 */
    private List<CharSequence> details;

    /**
     * Creates a new instance that enforces the given {@code maxLineLength}.
     * @param maxLineLength 单行最大字节数，超限抛 {@link TooLongFrameException}。
     */
    public SmtpResponseDecoder(int maxLineLength) {
        super(maxLineLength);
    }

    @Override
    protected SmtpResponse decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, buffer);
        if (frame == null) {
            // 尚未收到完整 CRLF 行
            return null;
        }
        try {
            final int readable = frame.readableBytes();
            final int readerIndex = frame.readerIndex();
            if (readable < 3) {
                throw newDecoderException(buffer, readerIndex, readable);
            }
            final int code = parseCode(frame);
            final int separator = frame.readByte();
            final CharSequence detail = frame.isReadable() ? frame.toString(CharsetUtil.US_ASCII) : null;

            List<CharSequence> details = this.details;

            switch (separator) {
            case ' ':
                // 空格：多行应答的最后一行，或单行应答
                this.details = null;
                if (details != null) {
                    if (detail != null) {
                        details.add(detail);
                    }
                } else {
                    if (detail == null) {
                        details = Collections.emptyList();
                    } else {
                        details = Collections.singletonList(detail);
                    }
                }
                return new DefaultSmtpResponse(code, details);
            case '-':
                // 连字符：多行应答的中间行（如 250-PIPELINING）
                if (detail != null) {
                    if (details == null) {
                        // 初始容量 4：实际多行应答很少超过 3 行附加文本
                        this.details = details = new ArrayList<CharSequence>(4);
                    }
                    details.add(detail);
                }
                break;
            default:
                throw newDecoderException(buffer, readerIndex, readable);
            }
        } finally {
            frame.release();
        }
        return null;
    }

    private static DecoderException newDecoderException(ByteBuf buffer, int readerIndex, int readable) {
        return new DecoderException(
                "Received invalid line: '" + buffer.toString(readerIndex, readable, CharsetUtil.US_ASCII) + '\'');
    }

    /**
     * Parses the io.netty.handler.codec.smtp code without any allocation, which is three digits.
     * <p>从缓冲区连续读取三个 ASCII 数字字节，无字符串分配。</p>
     */
    private static int parseCode(ByteBuf buffer) {
        final int first = parseNumber(buffer.readByte()) * 100;
        final int second = parseNumber(buffer.readByte()) * 10;
        final int third = parseNumber(buffer.readByte());
        return first + second + third;
    }

    private static int parseNumber(byte b) {
        return Character.digit((char) b, 10);
    }
}
