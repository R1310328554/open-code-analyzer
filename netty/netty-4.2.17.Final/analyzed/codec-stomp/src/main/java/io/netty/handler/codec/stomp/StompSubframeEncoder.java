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
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.AppendableCharSequence;
import io.netty.util.internal.PlatformDependent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import static io.netty.handler.codec.stomp.StompConstants.NUL;
import static io.netty.handler.codec.stomp.StompHeaders.ACCEPT_VERSION;
import static io.netty.handler.codec.stomp.StompHeaders.ACK;
import static io.netty.handler.codec.stomp.StompHeaders.CONTENT_LENGTH;
import static io.netty.handler.codec.stomp.StompHeaders.CONTENT_TYPE;
import static io.netty.handler.codec.stomp.StompHeaders.DESTINATION;
import static io.netty.handler.codec.stomp.StompHeaders.HEART_BEAT;
import static io.netty.handler.codec.stomp.StompHeaders.HOST;
import static io.netty.handler.codec.stomp.StompHeaders.ID;
import static io.netty.handler.codec.stomp.StompHeaders.LOGIN;
import static io.netty.handler.codec.stomp.StompHeaders.MESSAGE;
import static io.netty.handler.codec.stomp.StompHeaders.MESSAGE_ID;
import static io.netty.handler.codec.stomp.StompHeaders.PASSCODE;
import static io.netty.handler.codec.stomp.StompHeaders.RECEIPT;
import static io.netty.handler.codec.stomp.StompHeaders.RECEIPT_ID;
import static io.netty.handler.codec.stomp.StompHeaders.SERVER;
import static io.netty.handler.codec.stomp.StompHeaders.SESSION;
import static io.netty.handler.codec.stomp.StompHeaders.SUBSCRIPTION;
import static io.netty.handler.codec.stomp.StompHeaders.TRANSACTION;
import static io.netty.handler.codec.stomp.StompHeaders.VERSION;

/**
 * Encodes a {@link StompFrame} or a {@link StompSubframe} into a {@link ByteBuf}.
 *
 * <p>STOMP 子帧出站编码器：将 {@link StompFrame}、{@link StompHeadersSubframe}
 * 或 {@link StompContentSubframe} 序列化为 STOMP 1.x 线格式（命令行 + 头 + 空行 + 正文 + NUL）。
 * CONNECT/CONNECTED 命令禁止转义但校验非法字符；其余命令对头名/头值做反斜杠转义。</p>
 */
public class StompSubframeEncoder extends MessageToMessageEncoder<StompSubframe> {

    /** 线程本地缓存中已转义头名的最大条目数。 */
    private static final int ESCAPE_HEADER_KEY_CACHE_LIMIT = 32;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /** 预置常用 STOMP 头名的转义结果，避免重复分配。 */
    private static final FastThreadLocal<LinkedHashMap<CharSequence, CharSequence>> ESCAPE_HEADER_KEY_CACHE =
            new FastThreadLocal<LinkedHashMap<CharSequence, CharSequence>>() {
                @Override
                protected LinkedHashMap<CharSequence, CharSequence> initialValue() throws Exception {
                    LinkedHashMap<CharSequence, CharSequence> cache = new LinkedHashMap<CharSequence, CharSequence>(
                            ESCAPE_HEADER_KEY_CACHE_LIMIT, DEFAULT_LOAD_FACTOR, true) {

                        @Override
                        protected boolean removeEldestEntry(Entry eldest) {
                            return size() > ESCAPE_HEADER_KEY_CACHE_LIMIT;
                        }
                    };

                    // 预填 RFC 常用头名（无需转义时键值相同）
                    cache.put(ACCEPT_VERSION, ACCEPT_VERSION);
                    cache.put(HOST, HOST);
                    cache.put(LOGIN, LOGIN);
                    cache.put(PASSCODE, PASSCODE);
                    cache.put(HEART_BEAT, HEART_BEAT);
                    cache.put(VERSION, VERSION);
                    cache.put(SESSION, SESSION);
                    cache.put(SERVER, SERVER);
                    cache.put(DESTINATION, DESTINATION);
                    cache.put(ID, ID);
                    cache.put(ACK, ACK);
                    cache.put(TRANSACTION, TRANSACTION);
                    cache.put(RECEIPT, RECEIPT);
                    cache.put(MESSAGE_ID, MESSAGE_ID);
                    cache.put(SUBSCRIPTION, SUBSCRIPTION);
                    cache.put(RECEIPT_ID, RECEIPT_ID);
                    cache.put(MESSAGE, MESSAGE);
                    cache.put(CONTENT_LENGTH, CONTENT_LENGTH);
                    cache.put(CONTENT_TYPE, CONTENT_TYPE);

                    return cache;
                }
            };

    public StompSubframeEncoder() {
        super(StompSubframe.class);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, StompSubframe msg, List<Object> out) throws Exception {
        if (msg instanceof StompFrame) {
            // 完整帧：头 + 正文 + 帧尾 NUL
            StompFrame stompFrame = (StompFrame) msg;
            ByteBuf buf = encodeFullFrame(stompFrame, ctx);

            out.add(convertFullFrame(stompFrame, buf));
        } else if (msg instanceof StompHeadersSubframe) {
            // 仅头子帧（流式发送的第一段）
            StompHeadersSubframe stompHeadersSubframe = (StompHeadersSubframe) msg;
            ByteBuf buf = ctx.alloc().buffer(headersSubFrameSize(stompHeadersSubframe));
            try {
                encodeHeaders(stompHeadersSubframe, buf);
            } catch (Exception e) {
                buf.release();
                PlatformDependent.throwException(e);
            }

            out.add(convertHeadersSubFrame(stompHeadersSubframe, buf));
        } else if (msg instanceof StompContentSubframe) {
            // 正文子帧；最后一帧追加 NUL
            StompContentSubframe stompContentSubframe = (StompContentSubframe) msg;
            ByteBuf buf = encodeContent(stompContentSubframe, ctx);

            out.add(convertContentSubFrame(stompContentSubframe, buf));
        }
    }

    /**
     * An extension method to convert a STOMP encoded buffer to a different message type
     * based on an original {@link StompFrame} full frame.
     *
     * <p>By default an encoded buffer is returned as is.
     *
     * <p>扩展点：子类可将编码后的 {@link ByteBuf} 包装为其他出站消息类型；默认原样返回。</p>
     */
    protected Object convertFullFrame(StompFrame original, ByteBuf encoded) {
        return encoded;
    }

    /**
     * An extension method to convert a STOMP encoded buffer to a different message type
     * based on an original {@link StompHeadersSubframe} headers sub frame.
     *
     * <p>By default an encoded buffer is returned as is.
     *
     * <p>扩展点：头子帧编码完成后的类型转换钩子。</p>
     */
    protected Object convertHeadersSubFrame(StompHeadersSubframe original, ByteBuf encoded) {
        return encoded;
    }

    /**
     * An extension method to convert a STOMP encoded buffer to a different message type
     * based on an original {@link StompHeadersSubframe} content sub frame.
     *
     * <p>By default an encoded buffer is returned as is.
     *
     * <p>扩展点：正文子帧编码完成后的类型转换钩子。</p>
     */
    protected Object convertContentSubFrame(StompContentSubframe original, ByteBuf encoded) {
        return encoded;
    }

    /**
     * Returns a heuristic size for headers (32 bytes per header line) + (2 bytes for colon and eol) + (additional
     * command buffer).
     *
     * <p>按每行头约 34 字节估算缓冲区容量，下限 128、较大帧至少 256。</p>
     */
    protected int headersSubFrameSize(StompHeadersSubframe headersSubframe) {
        int estimatedSize = headersSubframe.headers().size() * 34 + 48;
        if (estimatedSize < 128) {
            return 128;
        }

        return Math.max(estimatedSize, 256);
    }

    /** 编码完整 {@link StompFrame}：头、可选正文、结尾 NUL。 */
    private ByteBuf encodeFullFrame(StompFrame frame, ChannelHandlerContext ctx) {
        int contentReadableBytes = frame.content().readableBytes();
        ByteBuf buf = ctx.alloc().buffer(headersSubFrameSize(frame) + contentReadableBytes);
        try {
            encodeHeaders(frame, buf);
        } catch (Exception e) {
            buf.release();
            PlatformDependent.throwException(e);
        }

        if (contentReadableBytes > 0) {
            buf.writeBytes(frame.content());
        }

        return buf.writeByte(NUL);
    }

    /** 写入命令行、各 header 行及头与正文之间的空行。 */
    private static void encodeHeaders(StompHeadersSubframe frame, ByteBuf buf) {
        StompCommand command = frame.command();
        ByteBufUtil.writeUtf8(buf, command.toString());
        buf.writeByte(StompConstants.LF);

        boolean shouldEscape = shouldEscape(command);
        LinkedHashMap<CharSequence, CharSequence> cache = ESCAPE_HEADER_KEY_CACHE.get();
        for (Entry<CharSequence, CharSequence> entry : frame.headers()) {
            CharSequence headerKey = entry.getKey();
            CharSequence headerValue = entry.getValue();
            if (headerKey.length() == 0) {
                throw new IllegalArgumentException("STOMP " + command + " contains empty header name");
            }
            if (shouldEscape) {
                // 非 CONNECT/CONNECTED：对头名/头值做 STOMP 转义
                CharSequence cachedHeaderKey = cache.get(headerKey);
                if (cachedHeaderKey == null) {
                    cachedHeaderKey = escape(command, "header name", headerKey);
                    cache.put(headerKey, cachedHeaderKey);
                }
                headerKey = cachedHeaderKey;
                headerValue = escape(command, "header value", headerValue);
            } else {
                // For CONNECT/CONNECTED: don't escape but REJECT illegal characters
                // CONNECT/CONNECTED：不转义，但拒绝含换行、冒号、NUL 等非法字符
                validateNoIllegalCharacters(command, headerKey, "header name");
                validateNoIllegalCharacters(command, headerValue, "header value");
            }

            ByteBufUtil.writeUtf8(buf, headerKey);
            buf.writeByte(StompConstants.COLON);

            ByteBufUtil.writeUtf8(buf, headerValue);
            buf.writeByte(StompConstants.LF);
        }

        buf.writeByte(StompConstants.LF);
    }

    /** 最后一帧正文后追加 NUL；中间帧仅 retain 原内容。 */
    private static ByteBuf encodeContent(StompContentSubframe content, ChannelHandlerContext ctx) {
        if (content instanceof LastStompContentSubframe) {
            ByteBuf buf = ctx.alloc().buffer(content.content().readableBytes() + 1);
            buf.writeBytes(content.content());
            buf.writeByte(StompConstants.NUL);
            return buf;
        }

        return content.content().retain();
    }

    /** CONNECT 与 CONNECTED 握手命令不使用 STOMP 转义规则。 */
    private static boolean shouldEscape(StompCommand command) {
        return command != StompCommand.CONNECT && command != StompCommand.CONNECTED;
    }

    /** 校验 CharSequence 中是否含 STOMP 禁止的未转义字符。 */
    private static void validateNoIllegalCharacters(StompCommand command, CharSequence value, String type) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\n' || c == '\r' || c == ':' || c == '\0') {
                throw newIllegalCharacterException(command, type, i);
            }
        }
    }

    private static IllegalArgumentException newIllegalCharacterException(StompCommand command, String type, int index) {
        return new IllegalArgumentException(
                "STOMP " + command + " " + type + " contains illegal character at index " + index);
    }

    /** 将 \、:、\n、\r 转为 STOMP 转义序列；NUL 无转义形式，直接抛错。 */
    private static CharSequence escape(StompCommand command, String type, CharSequence input) {
        AppendableCharSequence builder = null;
        for (int i = 0; i < input.length(); i++) {
            char chr = input.charAt(i);
            if (chr == '\\') {
                builder = escapeBuilder(builder, input, i);
                builder.append("\\\\");
            } else if (chr == ':') {
                builder = escapeBuilder(builder, input, i);
                builder.append("\\c");
            } else if (chr == '\n') {
                builder = escapeBuilder(builder, input, i);
                builder.append("\\n");
            } else if (chr == '\r') {
                builder = escapeBuilder(builder, input, i);
                builder.append("\\r");
            } else if (chr == '\0') {
                // The NUL character has no escape and is always illegal.
                // NUL 在 STOMP 帧内非法，且无对应转义序列
                throw newIllegalCharacterException(command, type, i);
            } else if (builder != null) {
                builder.append(chr);
            }
        }

        return builder != null? builder : input;
    }

    /** 首次需要转义时惰性创建 {@link AppendableCharSequence}，预留 8 字节避免扩容。 */
    private static AppendableCharSequence escapeBuilder(AppendableCharSequence builder, CharSequence input,
                                                        int offset) {
        if (builder != null) {
            return builder;
        }

        // Add extra overhead to the input char sequence to avoid resizing during escaping.
        return new AppendableCharSequence(input.length() + 8).append(input, 0, offset);
    }
}
