/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.spdy;

import io.netty.handler.codec.Headers;
import io.netty.util.AsciiString;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * SPDY 协议中 HTTP 语义头的标准名称常量，以及访问 {@link SpdyHeadersFrame} 的常用工具方法。
 * <p>SPDY 将请求行/状态行拆成以冒号开头的伪头（如 {@code :method}、{@code :status}），
 * 与普通 HTTP 头域共存于同一 {@link Headers} 容器。
 */
public interface SpdyHeaders extends Headers<CharSequence, CharSequence, SpdyHeaders> {

    /**
     * SPDY 伪 HTTP 头名称常量（均以 {@code :} 开头，区别于普通头域）。
     */
    final class HttpNames {
        /**
         * {@code ":host"} — 目标主机，对应 HTTP {@code Host}。
         */
        public static final AsciiString HOST = AsciiString.cached(":host");
        /**
         * {@code ":method"} — HTTP 方法（GET、POST 等）。
         */
        public static final AsciiString METHOD = AsciiString.cached(":method");
        /**
         * {@code ":path"} — 请求路径与 query，不含 scheme/host。
         */
        public static final AsciiString PATH = AsciiString.cached(":path");
        /**
         * {@code ":scheme"} — 协议方案，通常为 {@code https}。
         */
        public static final AsciiString SCHEME = AsciiString.cached(":scheme");
        /**
         * {@code ":status"} — 响应状态码（如 {@code 200}）。
         */
        public static final AsciiString STATUS = AsciiString.cached(":status");
        /**
         * {@code ":version"} — HTTP 版本字符串（如 {@code HTTP/1.1}）。
         */
        public static final AsciiString VERSION = AsciiString.cached(":version");

        private HttpNames() { }
    }

    /**
     * 等价于 {@link Headers#get(Object)}，并将首个值转为 {@link String}。
     * @param name the name of the header to retrieve
     * @return the first header value if the header is found. {@code null} if there's no such header.
     */
    String getAsString(CharSequence name);

    /**
     * 等价于 {@link Headers#getAll(Object)}，并将列表中每个元素转为 {@link String}。
     * @param name the name of the header to retrieve
     * @return a {@link List} of header values or an empty {@link List} if no values are found.
     */
    List<String> getAllAsString(CharSequence name);

    /**
     * 等价于 {@link #iterator()}，但将每条 {@link Entry} 的键值均转为 {@link String}。
     */
    Iterator<Entry<String, String>> iteratorAsString();

    /**
     * 判断是否存在指定名称与值的头部。
     * <p>
     * 若 {@code ignoreCase} 为 {@code true}，则对值做大小写不敏感比较。
     * @param name the name of the header to find
     * @param value the value of the header to find
     * @param ignoreCase {@code true} then a case insensitive compare is run to compare values.
     * otherwise a case sensitive compare is run to compare values.
     */
    boolean contains(CharSequence name, CharSequence value, boolean ignoreCase);
}
