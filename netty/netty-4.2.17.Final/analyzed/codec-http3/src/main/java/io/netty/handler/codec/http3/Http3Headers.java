/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.http3;

import io.netty.handler.codec.Headers;
import io.netty.util.AsciiString;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * HTTP/3 头部容器接口，扩展通用 {@link Headers} 并提供伪头部（pseudo-header）的便捷读写。
 * <p>伪头部以 {@code :} 开头，承载请求/响应元数据（如 {@code :method}、{@code :status}），
 * 在 QPACK 解码顺序上必须位于所有普通头部之前。
 */
public interface Http3Headers extends Headers<CharSequence, CharSequence, Http3Headers> {

    /**
     * HTTP/2 (and HTTP/3) pseudo-headers names.
     * <p>HTTP/3 沿用 HTTP/2 伪头部命名；{@link #flag} 位掩码用于 {@link Http3HeadersSink} 校验必填项与去重。
     */
    enum PseudoHeaderName {
        /**
         * {@code :method}.
         * <p>请求方法，如 GET、POST、CONNECT。
         */
        METHOD(":method", true, 0x1),

        /**
         * {@code :scheme}.
         * <p>URI 方案，通常为 {@code https}。
         */
        SCHEME(":scheme", true, 0x2),

        /**
         * {@code :authority}.
         * <p>目标主机与端口；缺失时可用 {@code Host} 普通头部替代（RFC 9110 §7.2）。
         */
        AUTHORITY(":authority", true, 0x4),

        /**
         * {@code :path}.
         * <p>请求路径与查询串；OPTIONS 对 {@code *} 有特殊规则。
         */
        PATH(":path", true, 0x8),

        /**
         * {@code :status}.
         * <p>三位 HTTP 状态码，仅出现在响应侧。
         */
        STATUS(":status", false, 0x10),

        /**
         * {@code :protocol}.
         * <p>
         * Used for Extended CONNECT requests as defined in RFC 9220.
         * This pseudo-header is only valid for CONNECT requests and indicates
         * the desired protocol for the connection (e.g., "webtransport", "websocket").
         * <p>Extended CONNECT 专用：声明隧道内要协商的上层协议（WebTransport、WebSocket 等）。
         *
         * @see <a href="https://www.rfc-editor.org/rfc/rfc9220.html">RFC 9220: Bootstrapping WebSockets with HTTP/3</a>
         */
        PROTOCOL(":protocol", true, 0x20);

        private static final char PSEUDO_HEADER_PREFIX = ':';
        private static final byte PSEUDO_HEADER_PREFIX_BYTE = (byte) PSEUDO_HEADER_PREFIX;

        private final AsciiString value;
        /** {@code true} 表示该伪头部仅用于请求上下文（如 {@code :method}），{@code false} 表示响应专用（如 {@code :status}）。 */
        private final boolean requestOnly;
        // The position of the bit in the flag indicates the type of the header field
        // 每个枚举常量对应 flag 中的一位，便于用位或/位与统计已收到的伪头部集合
        private final int flag;
        private static final CharSequenceMap<PseudoHeaderName> PSEUDO_HEADERS = new CharSequenceMap<PseudoHeaderName>();

        static {
            for (PseudoHeaderName pseudoHeader : PseudoHeaderName.values()) {
                PSEUDO_HEADERS.add(pseudoHeader.value(), pseudoHeader);
            }
        }

        PseudoHeaderName(String value, boolean requestOnly, int flag) {
            this.value = AsciiString.cached(value);
            this.requestOnly = requestOnly;
            this.flag = flag;
        }

        public AsciiString value() {
            // Return a slice so that the buffer gets its own reader index.
            return value;
        }

        /**
         * Indicates whether the specified header follows the pseudo-header format (begins with ':' character)
         * <p>仅检查首字符是否为 {@code :}，不保证是 RFC 定义的合法伪头部名。
         *
         * @param headerName    the header name to check.
         * @return              {@code true} if the header follow the pseudo-header format
         */
        public static boolean hasPseudoHeaderFormat(CharSequence headerName) {
            if (headerName instanceof AsciiString) {
                final AsciiString asciiHeaderName = (AsciiString) headerName;
                return asciiHeaderName.length() > 0 && asciiHeaderName.byteAt(0) == PSEUDO_HEADER_PREFIX_BYTE;
            } else {
                return headerName.length() > 0 && headerName.charAt(0) == PSEUDO_HEADER_PREFIX;
            }
        }

        /**
         * Indicates whether the given header name is a valid HTTP/3 pseudo header.
         *
         * @param name  the header name.
         * @return      {@code true} if the given header name is a valid HTTP/3 pseudo header, {@code false} otherwise.
         */
        public static boolean isPseudoHeader(CharSequence name) {
            return PSEUDO_HEADERS.contains(name);
        }

        /**
         * Returns the {@link PseudoHeaderName} corresponding to the specified header name.
         *
         * @param name  the header name.
         * @return corresponding {@link PseudoHeaderName} if any, {@code null} otherwise.
         */
        @Nullable
        public static PseudoHeaderName getPseudoHeader(CharSequence name) {
            return PSEUDO_HEADERS.get(name);
        }

        /**
         * Indicates whether the pseudo-header is to be used in a request context.
         * <p>{@code true} 时该字段只能出现在请求头块中，用于区分请求/响应伪头部混用。
         *
         * @return {@code true} if the pseudo-header is to be used in a request context
         */
        public boolean isRequestOnly() {
            return requestOnly;
        }

        /** 返回该伪头部在 {@code receivedPseudoHeaders} 位掩码中对应的标志位。 */
        public int getFlag() {
             return flag;
        }
    }

    /**
     * Returns an iterator over all HTTP/3 headers. The iteration order is as follows:
     *   1. All pseudo headers (order not specified).
     *   2. All non-pseudo headers (in insertion order).
     * <p>迭代顺序保证伪头部先于普通头部，符合 HTTP/3 头部块语义。
     */
    @Override
    Iterator<Entry<CharSequence, CharSequence>> iterator();

    /**
     * Equivalent to {@link #getAll(Object)} but no intermediate list is generated.
     * <p>按名遍历多值头部，避免 {@code getAll} 分配中间 {@code List}。
     * @param name the name of the header to retrieve
     * @return an {@link Iterator} of header values corresponding to {@code name}.
     */
    Iterator<CharSequence> valueIterator(CharSequence name);

    /**
     * Sets the {@link PseudoHeaderName#METHOD} header
     *
     * @param value the value for the header.
     * @return      this instance itself.
     */
    Http3Headers method(CharSequence value);

    /**
     * Sets the {@link PseudoHeaderName#SCHEME} header
     *
     * @param value the value for the header.
     * @return      this instance itself.
     */
    Http3Headers scheme(CharSequence value);

    /**
     * Sets the {@link PseudoHeaderName#AUTHORITY} header
     *
     * @param value the value for the header.
     * @return      this instance itself.
     */
    Http3Headers authority(CharSequence value);

    /**
     * Sets the {@link PseudoHeaderName#PATH} header
     *
     * @param value the value for the header.
     * @return      this instance itself.
     */
    Http3Headers path(CharSequence value);

    /**
     * Sets the {@link PseudoHeaderName#STATUS} header
     *
     * @param value the value for the header.
     * @return      this instance itself.
     */
    Http3Headers status(CharSequence value);

    /**
     * Sets the {@link PseudoHeaderName#PROTOCOL} header
     * <p>
     * This pseudo-header is used for Extended CONNECT requests as defined in RFC 9220.
     * Common values include "webtransport" and "websocket".
     * <p>为 Extended CONNECT 设置目标协议标识。
     *
     * @param value the value for the header.
     * @return      this instance itself.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9220.html">RFC 9220: Bootstrapping WebSockets with HTTP/3</a>
     */
    default Http3Headers protocol(CharSequence value) {
        set(PseudoHeaderName.PROTOCOL.value(), value);
        return this;
    }

    /**
     * Gets the {@link PseudoHeaderName#METHOD} header or {@code null} if there is no such header
     *
     * @return the value of the header.
     */
    @Nullable
    CharSequence method();

    /**
     * Gets the {@link PseudoHeaderName#SCHEME} header or {@code null} if there is no such header
     *
     * @return the value of the header.
     */
    @Nullable
    CharSequence scheme();

    /**
     * Gets the {@link PseudoHeaderName#AUTHORITY} header or {@code null} if there is no such header
     *
     * @return the value of the header.
     */
    @Nullable
    CharSequence authority();

    /**
     * Gets the {@link PseudoHeaderName#PATH} header or {@code null} if there is no such header
     *
     * @return the value of the header.
     */
    @Nullable
    CharSequence path();

    /**
     * Gets the {@link PseudoHeaderName#STATUS} header or {@code null} if there is no such header
     *
     * @return the value of the header.
     */
    @Nullable
    CharSequence status();

    /**
     * Gets the {@link PseudoHeaderName#PROTOCOL} header or {@code null} if there is no such header
     * <p>
     * This pseudo-header is used for Extended CONNECT requests as defined in RFC 9220.
     *
     * @return the value of the header.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9220.html">RFC 9220: Bootstrapping WebSockets with HTTP/3</a>
     */
    @Nullable
    default CharSequence protocol() {
        return get(PseudoHeaderName.PROTOCOL.value());
    }

    /**
     * Returns {@code true} if a header with the {@code name} and {@code value} exists, {@code false} otherwise.
     * <p>
     * If {@code caseInsensitive} is {@code true} then a case insensitive compare is done on the value.
     * <p>值比较可选大小写不敏感，便于匹配 {@code Content-Type} 等头部。
     *
     * @param name              the name of the header to find
     * @param value             the value of the header to find
     * @param caseInsensitive   {@code true} then a case insensitive compare is run to compare values.
     * otherwise a case sensitive compare is run to compare values.
     * @return                  {@code true} if its contained, {@code false} otherwise.
     */
    boolean contains(CharSequence name, CharSequence value, boolean caseInsensitive);
}
