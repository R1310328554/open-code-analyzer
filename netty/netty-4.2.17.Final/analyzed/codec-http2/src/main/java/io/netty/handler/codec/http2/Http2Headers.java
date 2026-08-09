/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.http2;

import io.netty.handler.codec.DefaultHeaders;
import io.netty.handler.codec.Headers;
import io.netty.util.AsciiString;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * HTTP/2 头集合，扩展通用 {@link Headers} 接口并支持伪头（pseudo-header）语义。
 * <p>请求头含 {@code :method/:scheme/:authority/:path}，响应头含 {@code :status}；
 * RFC 8441 扩展 {@code :protocol} 用于 WebSocket over HTTP/2。
 */
public interface Http2Headers extends Headers<CharSequence, CharSequence, Http2Headers> {

    /**
     * HTTP/2 伪头名称枚举；均以 {@code ':'} 开头，区别于普通头字段。
     */
    enum PseudoHeaderName {
        /**
         * {@code :method}.
         */
        METHOD(":method", true),

        /**
         * {@code :scheme}.
         */
        SCHEME(":scheme", true),

        /**
         * {@code :authority}.
         */
        AUTHORITY(":authority", true),

        /**
         * {@code :path}.
         */
        PATH(":path", true),

        /**
         * {@code :status}.
         */
        STATUS(":status", false),

        /**
         * {@code :protocol}, as defined in <a href="https://datatracker.ietf.org/doc/rfc8441/">RFC 8441,
         * Bootstrapping WebSockets with HTTP/2</a>.
         */
        PROTOCOL(":protocol", true);

        /** 伪头前缀字符 {@code ':'}。 */
        private static final char PSEUDO_HEADER_PREFIX = ':';
        private static final byte PSEUDO_HEADER_PREFIX_BYTE = (byte) PSEUDO_HEADER_PREFIX;

        private final AsciiString value;
        /** {@code true} 表示仅用于请求上下文（如 :method），{@code :status} 为响应专用。 */
        private final boolean requestOnly;

        PseudoHeaderName(String value, boolean requestOnly) {
            this.value = AsciiString.cached(value);
            this.requestOnly = requestOnly;
        }

        public AsciiString value() {
            // Return a slice so that the buffer gets its own reader index.
            return value;
        }

        /**
         * 判断给定名称是否符合伪头格式（以 {@code ':'} 开头）。
         *
         * @return {@code true} if the header follow the pseudo-header format
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
         * 判断给定名称是否为 RFC 7540 定义的合法伪头。
         */
        public static boolean isPseudoHeader(CharSequence header) {
            return getPseudoHeader(header) != null;
        }

        /**
         * 判断给定名称是否为 RFC 7540 定义的合法伪头。
         */
        public static boolean isPseudoHeader(AsciiString header) {
            return getPseudoHeader(header) != null;
        }

        /**
         * 判断给定名称是否为 RFC 7540 定义的合法伪头。
         */
        public static boolean isPseudoHeader(String header) {
            return getPseudoHeader(header) != null;
        }

        /**
         * 根据头名称返回对应 {@link PseudoHeaderName}，无法识别时返回 {@code null}。
         *
         * @return corresponding {@link PseudoHeaderName} if any, {@code null} otherwise.
         */
        public static PseudoHeaderName getPseudoHeader(CharSequence header) {
            if (header instanceof AsciiString) {
                return getPseudoHeader((AsciiString) header);
            }
            return getPseudoHeaderName(header);
        }

        // 按名称长度分支匹配，避免对所有伪头做完整字符串比较
        private static PseudoHeaderName getPseudoHeaderName(CharSequence header) {
            int length = header.length();
            if (length > 0 && header.charAt(0) == PSEUDO_HEADER_PREFIX) {
                switch (length) {
                case 5:
                    // :path
                    return ":path".contentEquals(header)? PATH : null;
                case 7:
                    // :method, :scheme, :status
                    if (":method" == header) {
                        return METHOD;
                    }
                    if (":scheme" == header) {
                        return SCHEME;
                    }
                    if (":status" == header) {
                        return STATUS;
                    }
                    if (":method".contentEquals(header)) {
                        return METHOD;
                    }
                    if (":scheme".contentEquals(header)) {
                        return SCHEME;
                    }
                    return ":status".contentEquals(header)? STATUS : null;
                case 9:
                    // :protocol
                    return ":protocol".contentEquals(header)? PROTOCOL : null;
                case 10:
                    // :authority
                    return ":authority".contentEquals(header)? AUTHORITY : null;
                }
            }
            return null;
        }

        /**
         * 根据 {@link AsciiString} 头名称返回对应 {@link PseudoHeaderName}。
         *
         * @return corresponding {@link PseudoHeaderName} if any, {@code null} otherwise.
         */
        public static PseudoHeaderName getPseudoHeader(AsciiString header) {
            int length = header.length();
            if (length > 0 && header.charAt(0) == PSEUDO_HEADER_PREFIX) {
                switch (length) {
                case 5:
                    // :path
                    return PATH.value().equals(header) ? PATH : null;
                case 7:
                    if (header == METHOD.value()) {
                        return METHOD;
                    }
                    if (header == SCHEME.value()) {
                        return SCHEME;
                    }
                    if (header == STATUS.value()) {
                        return STATUS;
                    }
                    // :method, :scheme, :status
                    if (METHOD.value().equals(header)) {
                        return METHOD;
                    }
                    if (SCHEME.value().equals(header)) {
                        return SCHEME;
                    }
                    return STATUS.value().equals(header)? STATUS : null;
                case 9:
                    // :protocol
                    return PROTOCOL.value().equals(header)? PROTOCOL : null;
                case 10:
                    // :authority
                    return AUTHORITY.value().equals(header)? AUTHORITY : null;
                }
            }
            return null;
        }

        /**
         * 该伪头是否仅用于请求上下文。
         *
         * @return {@code true} if the pseudo-header is to be used in a request context
         */
        public boolean isRequestOnly() {
            return requestOnly;
        }
    }

    /**
     * 迭代所有 HTTP/2 头，顺序为：先全部伪头（顺序未规定），再普通头（插入顺序）。
     */
    @Override
    Iterator<Entry<CharSequence, CharSequence>> iterator();

    /**
     * 等价于 {@link #getAll(Object)} 但不生成中间 List。
     * @param name the name of the header to retrieve
     * @return an {@link Iterator} of header values corresponding to {@code name}.
     */
    Iterator<CharSequence> valueIterator(CharSequence name);

    /**
     * 设置 {@link PseudoHeaderName#METHOD} 伪头。
     */
    Http2Headers method(CharSequence value);

    /**
     * 设置 {@link PseudoHeaderName#SCHEME} 伪头。
     */
    Http2Headers scheme(CharSequence value);

    /**
     * 设置 {@link PseudoHeaderName#AUTHORITY} 伪头。
     */
    Http2Headers authority(CharSequence value);

    /**
     * 设置 {@link PseudoHeaderName#PATH} 伪头。
     */
    Http2Headers path(CharSequence value);

    /**
     * 设置 {@link PseudoHeaderName#STATUS} 伪头。
     */
    Http2Headers status(CharSequence value);

    /**
     * 获取 {@link PseudoHeaderName#METHOD}，不存在时返回 {@code null}。
     */
    CharSequence method();

    /**
     * 获取 {@link PseudoHeaderName#SCHEME}，不存在时返回 {@code null}。
     */
    CharSequence scheme();

    /**
     * 获取 {@link PseudoHeaderName#AUTHORITY}，不存在时返回 {@code null}。
     */
    CharSequence authority();

    /**
     * 获取 {@link PseudoHeaderName#PATH}，不存在时返回 {@code null}。
     */
    CharSequence path();

    /**
     * 获取 {@link PseudoHeaderName#STATUS}，不存在时返回 {@code null}。
     */
    CharSequence status();

    /**
     * 判断是否存在指定 {@code name}/{@code value} 的头对。
     * <p>
     * If {@code caseInsensitive} is {@code true} then a case insensitive compare is done on the value.
     *
     * @param name the name of the header to find
     * @param value the value of the header to find
     * @param caseInsensitive {@code true} then a case insensitive compare is run to compare values.
     * otherwise a case sensitive compare is run to compare values.
     */
    boolean contains(CharSequence name, CharSequence value, boolean caseInsensitive);
}
