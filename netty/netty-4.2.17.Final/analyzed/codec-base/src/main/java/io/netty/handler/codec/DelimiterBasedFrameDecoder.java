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

import static io.netty.util.internal.ObjectUtil.checkPositive;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ObjectUtil;

import java.util.List;

/**
 * 按一个或多个分隔符切分入站 {@link ByteBuf} 的解码器。  It is particularly useful for decoding the frames which ends
 * with a delimiter such as {@link Delimiters#nulDelimiter() NUL} or
 * {@linkplain Delimiters#lineDelimiter() newline characters}.
 *
 * <h3>Predefined delimiters</h3>
 * <p>
 * {@link Delimiters} defines frequently used delimiters for convenience' sake.
 *
 * <h3>Specifying more than one delimiter</h3>
 * <p>
 * {@link DelimiterBasedFrameDecoder} allows you to specify more than one
 * delimiter.  If more than one delimiter is found in the buffer, it chooses
 * 分隔符 which produces the shortest frame.  For example, if you have
 * the following data in the buffer:
 * <pre>
 * +--------------+
 * | ABC\nDEF\r\n |
 * +--------------+
 * </pre>
 * a {@link DelimiterBasedFrameDecoder}({@link Delimiters#lineDelimiter() Delimiters.lineDelimiter()})
 * will choose {@code '\n'} as the first delimiter and produce two frames:
 * <pre>
 * +-----+-----+
 * | ABC | DEF |
 * +-----+-----+
 * </pre>
 * rather than incorrectly choosing {@code '\r\n'} as the first delimiter:
 * <pre>
 * +----------+
 * | ABC\nDEF |
 * +----------+
 * </pre>
 */
public class DelimiterBasedFrameDecoder extends ByteToMessageDecoder {

    /** 分隔符数组（切片引用，不持有原缓冲所有权）。 */
    /** 分隔符数组（切片引用，不持有原缓冲所有权）。 */
    private final ByteBuf[] delimiters;
    private final int maxFrameLength;
    private final boolean stripDelimiter;
    private final boolean failFast;
    /** 是否正在丢弃超长帧数据。 */
    /** 是否正在丢弃超长帧数据。 */
    private boolean discardingTooLongFrame;
    private int tooLongFrameLength;
    /** 仅当分隔符为 {@code "\n"} 与 {@code "\r\n"} 时设置。*/
    /** 仅当分隔符为 {@code "\n"} 与 {@code "\r\n"} 时设置。*/
    private final LineBasedFrameDecoder lineBasedDecoder;

    /**
     * 创建新实例。
     *
      * @param maxFrameLength  解码帧允许的最大长度。
     *                        A {@link TooLongFrameException} is thrown if
     *                        帧长度 exceeds this value.
      * @param delimiter  分隔符
     */
    public DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf delimiter) {
        this(maxFrameLength, true, delimiter);
    }

    /**
     * 创建新实例。
     *
      * @param maxFrameLength  解码帧允许的最大长度。
     *                        A {@link TooLongFrameException} is thrown if
     *                        帧长度 exceeds this value.
      * @param stripDelimiter  解码后的帧是否剥离分隔符
      * @param delimiter  分隔符
     */
    public DelimiterBasedFrameDecoder(
            int maxFrameLength, boolean stripDelimiter, ByteBuf delimiter) {
        this(maxFrameLength, stripDelimiter, true, delimiter);
    }

    /**
     * 创建新实例。
     *
      * @param maxFrameLength  解码帧允许的最大长度。
     *                        A {@link TooLongFrameException} is thrown if
     *                        帧长度 exceeds this value.
      * @param stripDelimiter  解码后的帧是否剥离分隔符
      * @param failFast  为 {@code true} 时，一旦检测到帧将超出 {@code maxFrameLength} 即抛出 {@link TooLongFrameException}；
     * 为 {@code false} 时，读完整个超长帧后再抛出。
      * @param delimiter  分隔符
     */
    public DelimiterBasedFrameDecoder(
            int maxFrameLength, boolean stripDelimiter, boolean failFast,
            ByteBuf delimiter) {
        this(maxFrameLength, stripDelimiter, failFast, new ByteBuf[] {
                delimiter.slice(delimiter.readerIndex(), delimiter.readableBytes())});
    }

    /**
     * 创建新实例。
     *
      * @param maxFrameLength  解码帧允许的最大长度。
     *                        A {@link TooLongFrameException} is thrown if
     *                        帧长度 exceeds this value.
      * @param delimiters  分隔符数组
     */
    public DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf... delimiters) {
        this(maxFrameLength, true, delimiters);
    }

    /**
     * 创建新实例。
     *
      * @param maxFrameLength  解码帧允许的最大长度。
     *                        A {@link TooLongFrameException} is thrown if
     *                        帧长度 exceeds this value.
      * @param stripDelimiter  解码后的帧是否剥离分隔符
      * @param delimiters  分隔符数组
     */
    public DelimiterBasedFrameDecoder(
            int maxFrameLength, boolean stripDelimiter, ByteBuf... delimiters) {
        this(maxFrameLength, stripDelimiter, true, delimiters);
    }

    /**
     * 创建新实例。
     *
      * @param maxFrameLength  解码帧允许的最大长度。
     *                        A {@link TooLongFrameException} is thrown if
     *                        帧长度 exceeds this value.
      * @param stripDelimiter  解码后的帧是否剥离分隔符
      * @param failFast  为 {@code true} 时，一旦检测到帧将超出 {@code maxFrameLength} 即抛出 {@link TooLongFrameException}；
     * 为 {@code false} 时，读完整个超长帧后再抛出。
      * @param delimiters  分隔符数组
     */
    public DelimiterBasedFrameDecoder(
            int maxFrameLength, boolean stripDelimiter, boolean failFast, ByteBuf... delimiters) {
        validateMaxFrameLength(maxFrameLength);
        ObjectUtil.checkNonEmpty(delimiters, "delimiters");

        if (isLineBased(delimiters) && !isSubclass()) {
            lineBasedDecoder = new LineBasedFrameDecoder(maxFrameLength, stripDelimiter, failFast);
            this.delimiters = null;
        } else {
            this.delimiters = new ByteBuf[delimiters.length];
            for (int i = 0; i < delimiters.length; i ++) {
                ByteBuf d = delimiters[i];
                validateDelimiter(d);
                this.delimiters[i] = d.slice(d.readerIndex(), d.readableBytes());
            }
            lineBasedDecoder = null;
        }
        this.maxFrameLength = maxFrameLength;
        this.stripDelimiter = stripDelimiter;
        this.failFast = failFast;
    }

    /** 分隔符是否为 {@code "\n"} 与 {@code "\r\n"}。*/
    /** 分隔符是否为 {@code "\n"} 与 {@code "\r\n"}。*/
    private static boolean isLineBased(final ByteBuf[] delimiters) {
        if (delimiters.length != 2) {
            return false;
        }
        ByteBuf a = delimiters[0];
        ByteBuf b = delimiters[1];
        if (a.capacity() < b.capacity()) {
            a = delimiters[1];
            b = delimiters[0];
        }
        return a.capacity() == 2 && b.capacity() == 1
                && a.getByte(0) == '\r' && a.getByte(1) == '\n'
                && b.getByte(0) == '\n';
    }

    /**
     * 当前实例是否为 DelimiterBasedFrameDecoder 子类
     */
    private boolean isSubclass() {
        return getClass() != DelimiterBasedFrameDecoder.class;
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
        if (lineBasedDecoder != null) {
            return lineBasedDecoder.decode(ctx, buffer);
        }
        // 尝试所有分隔符，选择产生最短帧的分隔符
        int minFrameLength = Integer.MAX_VALUE;
        ByteBuf minDelim = null;
        for (ByteBuf delim: delimiters) {
            int frameLength = indexOf(buffer, delim);
            if (frameLength >= 0 && frameLength < minFrameLength) {
                minFrameLength = frameLength;
                minDelim = delim;
            }
        }

        if (minDelim != null) {
            int minDelimLength = minDelim.capacity();
            ByteBuf frame;

            if (discardingTooLongFrame) {
                // 刚丢弃完超长帧，恢复初始状态
                discardingTooLongFrame = false;
                buffer.skipBytes(minFrameLength + minDelimLength);

                int tooLongFrameLength = this.tooLongFrameLength;
                this.tooLongFrameLength = 0;
                if (!failFast) {
                    fail(tooLongFrameLength);
                }
                return null;
            }

            if (minFrameLength > maxFrameLength) {
                // 丢弃已读超长帧
                buffer.skipBytes(minFrameLength + minDelimLength);
                fail(minFrameLength);
                return null;
            }

            if (stripDelimiter) {
                frame = buffer.readRetainedSlice(minFrameLength);
                buffer.skipBytes(minDelimLength);
            } else {
                frame = buffer.readRetainedSlice(minFrameLength + minDelimLength);
            }

            return frame;
        } else {
            if (!discardingTooLongFrame) {
                if (buffer.readableBytes() > maxFrameLength) {
                    // 丢弃缓冲区内容直至找到分隔符
                    tooLongFrameLength = buffer.readableBytes();
                    buffer.skipBytes(buffer.readableBytes());
                    discardingTooLongFrame = true;
                    if (failFast) {
                        fail(tooLongFrameLength);
                    }
                }
            } else {
                // 仍未找到分隔符，继续丢弃
                tooLongFrameLength += buffer.readableBytes();
                buffer.skipBytes(buffer.readableBytes());
            }
            return null;
        }
    }

    private void fail(long frameLength) {
        if (frameLength > 0) {
            throw new TooLongFrameException(
                            "frame length exceeds " + maxFrameLength +
                            ": " + frameLength + " - discarded");
        } else {
            throw new TooLongFrameException(
                            "frame length exceeds " + maxFrameLength +
                            " - discarding");
        }
    }

    /**
     * 返回 haystack 读索引到首个 needle 的字节数；未找到返回 -1。
     */
    private static int indexOf(ByteBuf haystack, ByteBuf needle) {
        int index = ByteBufUtil.indexOf(needle, haystack);
        if (index == -1) {
            return -1;
        }
        return index - haystack.readerIndex();
    }

    private static void validateDelimiter(ByteBuf delimiter) {
        ObjectUtil.checkNotNull(delimiter, "delimiter");
        if (!delimiter.isReadable()) {
            throw new IllegalArgumentException("empty delimiter");
        }
    }

    private static void validateMaxFrameLength(int maxFrameLength) {
        checkPositive(maxFrameLength, "maxFrameLength");
    }
}
