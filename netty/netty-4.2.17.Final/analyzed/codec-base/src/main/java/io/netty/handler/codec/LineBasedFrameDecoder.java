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
package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ByteProcessor;

import java.util.List;

/**
 * 按行尾切分入站 {@link ByteBuf} 的解码器。
 * <p>
 * 同时支持 {@code "\n"} 与 {@code "\r\n"}。
 * <p>
 * The byte stream is expected to be in UTF-8 character encoding or ASCII. The current implementation
 * uses direct {@code byte} to {@code char} cast and then compares that {@code char} to a few low range
 * ASCII characters like {@code '\n'} or {@code '\r'}. UTF-8 is not using low range [0..0x7F]
 * byte values for multibyte codepoint representations therefore fully supported by this implementation.
 * <p>
 * 更通用的分隔符解码见 {@link DelimiterBasedFrameDecoder}。
 * <p>
 * Users should be aware that used as is, the lenient approach on lone {@code '\n} might result on a parser
 * diffenrencial on line based protocols requiring the use of {@code "\r\n"} delimiters like SMTP and can
 * result in attacks similar to
 * <a href="https://sec-consult.com/blog/detail/smtp-smuggling-spoofing-e-mails-worldwide/">SMTP smuggling</a>.
 * Validating afterward the end of line pattern can be a possible mitigation.
 */
/**
 * 按行尾（{@code \n} 或 {@code \r\n}）切分 {@link ByteBuf} 的解码器。
 * <p>
 * 假定 UTF-8 或 ASCII；对单独 {@code \n} 较宽松，严格协议（如 SMTP）需额外校验行尾。
 * @see DelimiterBasedFrameDecoder
 */
public class LineBasedFrameDecoder extends ByteToMessageDecoder {

    /** 允许解码的最大帧长度。*/
    /** 允许解码的最大帧长度。*/
    private final int maxLength;
    /** 超出 maxLength 时是否立即抛异常。*/
    /** 超出 maxLength 时是否立即抛异常。*/
    private final boolean failFast;
    /** 解码帧是否剥离行分隔符。 */
    /** 解码帧是否剥离行分隔符。 */
    private final boolean stripDelimiter;

    /** 因已超出 maxLength 而丢弃输入时为 true。*/
    /** 因已超出 maxLength 而丢弃输入时为 true。*/
    private boolean discarding;
    private int discardedBytes;

    /** 上次扫描位置。 */
    /** 上次扫描位置。 */
    private int offset;

    /**
     * 创建新的解码器。
      * @param maxLength  解码帧允许的最大长度。
     *                   若帧长度超过此值则抛出 {@link TooLongFrameException}。
     */
    public LineBasedFrameDecoder(final int maxLength) {
        this(maxLength, true, false);
    }

    /**
     * 创建新的解码器。
      * @param maxLength  解码帧允许的最大长度。
     *                   若帧长度超过此值则抛出 {@link TooLongFrameException}。
      * @param stripDelimiter  解码后的帧是否剥离分隔符
      * @param failFast  为 {@code true} 时，一旦检测到帧将超出 {@code maxFrameLength} 即抛出 {@link TooLongFrameException}；
     * 为 {@code false} 时，读完整个超长帧后再抛出。
     */
    public LineBasedFrameDecoder(final int maxLength, final boolean stripDelimiter, final boolean failFast) {
        this.maxLength = maxLength;
        this.failFast = failFast;
        this.stripDelimiter = stripDelimiter;
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Object decoded = decode(ctx, in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    /**
     * 从 {@link ByteBuf} 提取一帧并返回。
     *
      * @param   ctx             本 {@link ByteToMessageDecoder} 所属的 {@link ChannelHandlerContext}
      * @param   buffer          待读取的 {@link ByteBuf}
     * @return frame 表示帧的 {@link ByteBuf}；数据不足时 {@code null}
     */
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        final int eol = findEndOfLine(buffer);
        if (!discarding) {
            if (eol >= 0) {
                final ByteBuf frame;
                final int length = eol - buffer.readerIndex();
                final int delimLength = buffer.getByte(eol) == '\r'? 2 : 1;

                if (length > maxLength) {
                    buffer.readerIndex(eol + delimLength);
                    fail(ctx, length);
                    return null;
                }

                if (stripDelimiter) {
                    frame = buffer.readRetainedSlice(length);
                    buffer.skipBytes(delimLength);
                } else {
                    frame = buffer.readRetainedSlice(length + delimLength);
                }

                return frame;
            } else {
                final int length = buffer.readableBytes();
                if (length > maxLength) {
                    discardedBytes = length;
                    buffer.readerIndex(buffer.writerIndex());
                    discarding = true;
                    offset = 0;
                    if (failFast) {
                        fail(ctx, "over " + discardedBytes);
                    }
                }
                return null;
            }
        } else {
            if (eol >= 0) {
                final int length = discardedBytes + eol - buffer.readerIndex();
                final int delimLength = buffer.getByte(eol) == '\r'? 2 : 1;
                buffer.readerIndex(eol + delimLength);
                discardedBytes = 0;
                discarding = false;
                if (!failFast) {
                    fail(ctx, length);
                }
            } else {
                discardedBytes += buffer.readableBytes();
                buffer.readerIndex(buffer.writerIndex());
                // 跳过整个缓冲区，重置 offset 为 0
                offset = 0;
            }
            return null;
        }
    }

    private void fail(final ChannelHandlerContext ctx, int length) {
        fail(ctx, String.valueOf(length));
    }

    private void fail(final ChannelHandlerContext ctx, String length) {
        ctx.fireExceptionCaught(
                new TooLongFrameException(
                        "frame length (" + length + ") exceeds the allowed maximum (" + maxLength + ')'));
    }

    /**
     * 返回缓冲区中换行符位置；未找到返回 -1。
     */
    private int findEndOfLine(final ByteBuf buffer) {
        int totalLength = buffer.readableBytes();
        int i = buffer.indexOf(buffer.readerIndex() + offset,
                               buffer.readerIndex() + totalLength, (byte) '\n');
        if (i >= 0) {
            offset = 0;
            if (i > 0 && buffer.getByte(i - 1) == '\r') {
                i--;
            }
        } else {
            offset = totalLength;
        }
        return i;
    }
}
