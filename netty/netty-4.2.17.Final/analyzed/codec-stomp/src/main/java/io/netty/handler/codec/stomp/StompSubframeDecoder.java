/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.codec.stomp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.stomp.StompSubframeDecoder.State;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.AppendableCharSequence;
import io.netty.util.internal.StringUtil;

import java.util.List;

import static io.netty.buffer.ByteBufUtil.*;
import static io.netty.util.internal.ObjectUtil.*;

/**
 * Decodes {@link ByteBuf}s into {@link StompHeadersSubframe}s and {@link StompContentSubframe}s.
 *
 * <h3>Parameters to control memory consumption: </h3>
 * {@code maxLineLength} the maximum length of line - restricts length of command and header lines If the length of the
 * initial line exceeds this value, a {@link TooLongFrameException} will be raised.
 * <br>
 * {@code maxChunkSize} The maximum length of the content or each chunk.  If the content length (or the length of each
 * chunk) exceeds this value, the content or chunk ill be split into multiple {@link StompContentSubframe}s whose length
 * is {@code maxChunkSize} at maximum.
 * <br>
 * {@code maxNumHeaders} The maximum number of headers per frame.
 * If this limit exceeded a {@link TooLongFrameException} will be raised.
 *
 * <h3>Chunked Content</h3>
 * <p>
 * If the content of a stomp message is greater than {@code maxChunkSize} the transfer encoding of the HTTP message is
 * 'chunked', this decoder generates multiple {@link StompContentSubframe} instances to avoid excessive memory
 * consumption. Note, that every message, even with no content decodes with {@link LastStompContentSubframe} at the end
 * to simplify upstream message parsing.
 * <p>将入站 {@link ByteBuf} 解码为 {@link StompHeadersSubframe} 与 {@link StompContentSubframe} 序列。
 * {@code maxLineLength} 限制命令行与头部行长度；{@code maxChunkSize} 限制单块正文大小，超出则拆成多块；
 * {@code maxNumHeaders} 限制每帧头部条数。即使无正文，也会输出 {@link LastStompContentSubframe} 以统一上游处理逻辑。</p>
 */
public class StompSubframeDecoder extends ReplayingDecoder<State> {

    /** 默认单块正文上限（字节）。 */
    private static final int DEFAULT_CHUNK_SIZE = 8132;
    /** 默认单行（命令/头部）最大长度。 */
    private static final int DEFAULT_MAX_LINE_LENGTH = 1024;
    /** 默认每帧最大头部数量。 */
    private static final int DEFAULT_MAX_NUMBER_HEADERS = 128;

    /**
     * @deprecated this should never be used by an user!
     * <p>解码状态机；仅供框架内部使用，应用代码不应依赖。</p>
     */
    @Deprecated
    public enum State {
        /** 跳过帧间多余的 CR/LF。 */
        SKIP_CONTROL_CHARACTERS,
        /** 读取命令行与头部行。 */
        READ_HEADERS,
        /** 按 content-length 或 NUL 终止符读取正文。 */
        READ_CONTENT,
        /** 跳过帧尾 NUL 并输出末内容子帧。 */
        FINALIZE_FRAME_READ,
        /** 坏帧：丢弃剩余可读字节直至连接复位。 */
        BAD_FRAME,
        /** 无效分块（保留状态）。 */
        INVALID_CHUNK
    }

    /** 解析 STOMP 命令首行（UTF-8 行）。 */
    private final Utf8LineParser commandParser;
    /** 解析 name:value 头部行。 */
    private final HeaderParser headerParser;
    /** 单块正文最大长度。 */
    private final int maxChunkSize;
    /** 当前帧已读正文累计长度。 */
    private int alreadyReadChunkSize;
    /** 待输出的末内容子帧（可能尚未加入 out）。 */
    private LastStompContentSubframe lastContent;
    /** 来自 content-length 头部的定长正文长度；-1 表示按 NUL 终止。 */
    private long contentLength = -1;

    public StompSubframeDecoder() {
        this(DEFAULT_MAX_LINE_LENGTH, DEFAULT_CHUNK_SIZE);
    }

    public StompSubframeDecoder(boolean validateHeaders) {
        this(DEFAULT_MAX_LINE_LENGTH, DEFAULT_CHUNK_SIZE, DEFAULT_MAX_NUMBER_HEADERS, validateHeaders);
    }

    public StompSubframeDecoder(int maxLineLength, int maxChunkSize) {
        this(maxLineLength, maxChunkSize, false);
    }

    public StompSubframeDecoder(int maxLineLength, int maxChunkSize, boolean validateHeaders) {
        this(maxLineLength, maxChunkSize, DEFAULT_MAX_NUMBER_HEADERS, validateHeaders);
    }

    public StompSubframeDecoder(int maxLineLength, int maxChunkSize, int maxNumHeaders, boolean validateHeaders) {
        super(State.SKIP_CONTROL_CHARACTERS);
        checkPositive(maxLineLength, "maxLineLength");
        checkPositive(maxChunkSize, "maxChunkSize");
        checkPositive(maxNumHeaders, "maxNumHeaders");

        this.maxChunkSize = maxChunkSize;
        commandParser = new Utf8LineParser(new AppendableCharSequence(16), maxLineLength);
        headerParser = new HeaderParser(new AppendableCharSequence(128), maxLineLength, maxNumHeaders, validateHeaders);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case SKIP_CONTROL_CHARACTERS:
                skipControlCharacters(in);
                checkpoint(State.READ_HEADERS);
                // Fall through.
            case READ_HEADERS:
                StompCommand command = StompCommand.UNKNOWN;
                StompHeadersSubframe frame = null;
                try {
                    command = readCommand(in);
                    frame = new DefaultStompHeadersSubframe(command);
                    checkpoint(readHeaders(in, frame));
                    out.add(frame);
                } catch (Exception e) {
                    // 解析失败仍输出带失败结果的头部子帧，并进入 BAD_FRAME
                    if (frame == null) {
                        frame = new DefaultStompHeadersSubframe(command);
                    }
                    frame.setDecoderResult(DecoderResult.failure(e));
                    out.add(frame);
                    checkpoint(State.BAD_FRAME);
                    return;
                }
                break;
            case BAD_FRAME:
                in.skipBytes(actualReadableBytes());
                return;
        }
        try {
            switch (state()) {
                case READ_CONTENT:
                    int toRead = in.readableBytes();
                    if (toRead == 0) {
                        return;
                    }
                    if (toRead > maxChunkSize) {
                        toRead = maxChunkSize;
                    }
                    if (contentLength >= 0) {
                        // 定长正文：按 content-length 切分，读满后进入 FINALIZE
                        int remainingLength = (int) (contentLength - alreadyReadChunkSize);
                        if (toRead > remainingLength) {
                            toRead = remainingLength;
                        }
                        ByteBuf chunkBuffer = readBytes(ctx.alloc(), in, toRead);
                        if ((alreadyReadChunkSize += toRead) >= contentLength) {
                            lastContent = new DefaultLastStompContentSubframe(chunkBuffer);
                            checkpoint(State.FINALIZE_FRAME_READ);
                        } else {
                            out.add(new DefaultStompContentSubframe(chunkBuffer));
                            return;
                        }
                    } else {
                        // 无 content-length：正文以 NUL 结束
                        int nulIndex = indexOf(in, in.readerIndex(), in.writerIndex(), StompConstants.NUL);
                        if (nulIndex == in.readerIndex()) {
                            checkpoint(State.FINALIZE_FRAME_READ);
                        } else {
                            if (nulIndex > 0) {
                                toRead = nulIndex - in.readerIndex();
                            } else {
                                toRead = in.writerIndex() - in.readerIndex();
                            }
                            ByteBuf chunkBuffer = readBytes(ctx.alloc(), in, toRead);
                            alreadyReadChunkSize += toRead;
                            if (nulIndex > 0) {
                                lastContent = new DefaultLastStompContentSubframe(chunkBuffer);
                                checkpoint(State.FINALIZE_FRAME_READ);
                            } else {
                                out.add(new DefaultStompContentSubframe(chunkBuffer));
                                return;
                            }
                        }
                    }
                    // Fall through.
                case FINALIZE_FRAME_READ:
                    skipNullCharacter(in);
                    if (lastContent == null) {
                        lastContent = LastStompContentSubframe.EMPTY_LAST_CONTENT;
                    }
                    out.add(lastContent);
                    resetDecoder();
            }
        } catch (Exception e) {
            if (lastContent != null) {
                lastContent.release();
                lastContent = null;
            }

            StompContentSubframe errorContent = new DefaultLastStompContentSubframe(Unpooled.EMPTY_BUFFER);
            errorContent.setDecoderResult(DecoderResult.failure(e));
            out.add(errorContent);
            checkpoint(State.BAD_FRAME);
        }
    }

    /** 读取并解析命令行，映射为 {@link StompCommand}。 */
    private StompCommand readCommand(ByteBuf in) {
        CharSequence commandSequence = commandParser.parse(in);
        if (commandSequence == null) {
            throw new DecoderException("Failed to read command from channel");
        }
        String commandStr = commandSequence.toString();
        try {
            return StompCommand.valueOf(commandStr);
        } catch (IllegalArgumentException iae) {
            throw new DecoderException("Cannot to parse command " + commandStr);
        }
    }

    /** 逐行解析头部，直至空行；返回下一解码状态。 */
    private State readHeaders(ByteBuf buffer, StompHeadersSubframe headersSubframe) {
        StompHeaders headers = headersSubframe.headers();
        for (;;) {
            boolean headerRead = headerParser.parseHeader(headersSubframe, buffer);
            if (!headerRead) {
                if (headers.contains(StompHeaders.CONTENT_LENGTH)) {
                    contentLength = getContentLength(headers);
                    if (contentLength == 0) {
                        return State.FINALIZE_FRAME_READ;
                    }
                }
                return State.READ_CONTENT;
            }
        }
    }

    /** 校验并读取 content-length 头部值。 */
    private static long getContentLength(StompHeaders headers) {
        long contentLength = headers.getLong(StompHeaders.CONTENT_LENGTH, 0L);
        if (contentLength < 0) {
            throw new DecoderException(StompHeaders.CONTENT_LENGTH + " must be non-negative");
        }
        return contentLength;
    }

    /** 帧尾必须紧跟一个 NUL 字节。 */
    private static void skipNullCharacter(ByteBuf buffer) {
        byte b = buffer.readByte();
        if (b != StompConstants.NUL) {
            throw new IllegalStateException("unexpected byte in buffer " + b + " while expecting NULL byte");
        }
    }

    /** 跳过帧开始处可能存在的 CR/LF 分隔符。 */
    private static void skipControlCharacters(ByteBuf buffer) {
        byte b;
        for (;;) {
            if (!buffer.isReadable()) {
                return;
            }
            b = buffer.readByte();
            if (b != StompConstants.CR && b != StompConstants.LF) {
                buffer.readerIndex(buffer.readerIndex() - 1);
                break;
            }
        }
    }

    /** 重置状态机，准备解析下一帧。 */
    private void resetDecoder() {
        checkpoint(State.SKIP_CONTROL_CHARACTERS);
        contentLength = -1;
        alreadyReadChunkSize = 0;
        lastContent = null;
    }

    /** 按 CRLF 终止的 UTF-8 行解析器，供命令行与头部行复用。 */
    private static class Utf8LineParser implements ByteProcessor {

        private final AppendableCharSequence charSeq;
        private final int maxLineLength;

        private int lineLength;
        private char interim;
        private boolean nextRead;

        Utf8LineParser(AppendableCharSequence charSeq, int maxLineLength) {
            this.charSeq = checkNotNull(charSeq, "charSeq");
            this.maxLineLength = maxLineLength;
        }

        /** 扫描至 LF 或缓冲区不足；成功则推进 readerIndex 并返回已解析字符序列。 */
        AppendableCharSequence parse(ByteBuf byteBuf) {
            reset();
            int offset = byteBuf.forEachByte(this);
            if (offset == -1) {
                return null;
            }

            byteBuf.readerIndex(offset + 1);
            return charSeq;
        }

        AppendableCharSequence charSequence() {
            return charSeq;
        }

        @Override
        public boolean process(byte nextByte) throws Exception {
            if (nextByte == StompConstants.CR) {
                interim = 0;
                nextRead = false;
                ++lineLength;
                return true;
            }

            if (nextByte == StompConstants.LF) {
                return false;
            }

            if (++lineLength > maxLineLength) {
                throw new TooLongFrameException("An STOMP line is larger than " + maxLineLength + " bytes.");
            }

            // 1 byte   -   0xxxxxxx                    -  7 bits
            // 2 byte   -   110xxxxx 10xxxxxx           -  11 bits
            // 3 byte   -   1110xxxx 10xxxxxx 10xxxxxx  -  16 bits
            // 手工 UTF-8 解码：处理多字节字符的续字节
            if (nextRead) {
                interim |= (nextByte & 0x3F) << 6;
                nextRead = false;
            } else if (interim != 0) { // flush 2 or 3 byte
                appendTo(charSeq, (char) (interim | (nextByte & 0x3F)));
                interim = 0;
            } else if (nextByte >= 0) { // INITIAL BRANCH
                // The first 128 characters (US-ASCII) need one byte.
                appendTo(charSeq, (char) nextByte);
            } else if ((nextByte & 0xE0) == 0xC0) {
                // The next 1920 characters need two bytes and we can define
                // a first byte by mask 110xxxxx.
                interim = (char) ((nextByte & 0x1F) << 6);
            } else {
                // The rest of characters need three bytes.
                interim = (char) ((nextByte & 0x0F) << 12);
                nextRead = true;
            }

            return true;
        }

        protected void appendTo(AppendableCharSequence charSeq, char chr) {
            charSeq.append(chr);
        }

        protected void reset() {
            charSeq.reset();
            lineLength = 0;
            interim = 0;
            nextRead = false;
        }
    }

    /** 头部行解析：识别 name:value，支持 STOMP 1.1+ 转义序列。 */
    private static final class HeaderParser extends Utf8LineParser {

        private final boolean validateHeaders;
        private final int maxNumHeaders;
        private int numHeaders;
        private String name;
        private boolean valid;

        private boolean shouldUnescape;
        private boolean unescapeInProgress;

        HeaderParser(AppendableCharSequence charSeq, int maxLineLength, int maxNumHeaders, boolean validateHeaders) {
            super(charSeq, maxLineLength);
            this.validateHeaders = validateHeaders;
            this.maxNumHeaders = maxNumHeaders;
        }

        /**
         * 解析一行头部；空行表示头部结束，返回 {@code false}。
         * @return {@code true} 若本行是有效头部并已加入 frame
         */
        boolean parseHeader(StompHeadersSubframe headersSubframe, ByteBuf buf) {
            shouldUnescape = shouldUnescape(headersSubframe.command());
            AppendableCharSequence value = super.parse(buf);
            if (value == null || (name == null && value.length() == 0)) {
                numHeaders = 0;
                return false;
            }

            numHeaders++;
            if (maxNumHeaders < numHeaders) {
                throw new TooLongFrameException("maximum number of headers exceeded: " + maxNumHeaders);
            }
            if (valid) {
                headersSubframe.headers().add(name, value.toString());
            } else if (validateHeaders) {
                if (StringUtil.isNullOrEmpty(name)) {
                    throw new IllegalArgumentException("received an invalid header line '" + value + '\'');
                }
                String line = name + ':' + value;
                throw new IllegalArgumentException("a header value or name contains a prohibited character ':'"
                                                   + ", " + line);
            }
            return true;
        }

        @Override
        public boolean process(byte nextByte) throws Exception {
            if (nextByte == StompConstants.COLON) {
                if (name == null) {
                    AppendableCharSequence charSeq = charSequence();
                    if (charSeq.length() != 0) {
                        name = charSeq.substring(0, charSeq.length());
                        charSeq.reset();
                        valid = true;
                        return true;
                    } else {
                        name = StringUtil.EMPTY_STRING;
                    }
                } else {
                    // 值部分再次出现冒号，标记为无效头部
                    valid = false;
                }
            }

            return super.process(nextByte);
        }

        @Override
        protected void appendTo(AppendableCharSequence charSeq, char chr) {
            if (!shouldUnescape) {
                super.appendTo(charSeq, chr);
                return;
            }

            // CONNECT/CONNECTED 之外的头部分支持 \c \r \n 转义
            if (chr == '\\') {
                if (unescapeInProgress) {
                    super.appendTo(charSeq, chr);
                    unescapeInProgress = false;
                } else {
                    unescapeInProgress = true;
                }
                return;
            }

            if (unescapeInProgress) {
                if (chr == 'c') {
                    charSeq.append(':');
                } else if (chr == 'r') {
                    charSeq.append('\r');
                } else if (chr == 'n') {
                    charSeq.append('\n');
                } else {
                    charSeq.append('\\').append(chr);
                    throw new IllegalArgumentException("received an invalid escape header sequence '" + charSeq + '\'');
                }

                unescapeInProgress = false;
                return;
            }

            super.appendTo(charSeq, chr);
        }

        @Override
        protected void reset() {
            name = null;
            valid = false;
            unescapeInProgress = false;
            super.reset();
        }

        /** CONNECT 与 CONNECTED 帧头部不做转义处理。 */
        private static boolean shouldUnescape(StompCommand command) {
            return command != StompCommand.CONNECT && command != StompCommand.CONNECTED;
        }
    }
}
