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
package io.netty.handler.codec.spdy;

import io.netty.util.AsciiString;

/**
 * {@link SpdyHttpDecoder} 与 {@link SpdyHttpEncoder} 共用的扩展 HTTP 头名常量。
 * <p>这些 {@code x-spdy-*} 头在 HTTP 层携带 SPDY 特有元数据（流 ID、优先级、关联流等），
 * 编解码时与 SPDY 帧字段相互映射，不会直接出现在线上 SPDY 头块中。
 */
public final class SpdyHttpHeaders {

    /**
     * SPDY 扩展 HTTP 头名称（小写 {@link AsciiString}，便于零拷贝比较）。
     */
    public static final class Names {
        /**
         * {@code "x-spdy-stream-id"} — 当前 HTTP 消息对应的 SPDY 流 ID。
         */
        public static final AsciiString STREAM_ID = AsciiString.cached("x-spdy-stream-id");
        /**
         * {@code "x-spdy-associated-to-stream-id"} — 服务器推送资源所关联的客户端请求流 ID。
         */
        public static final AsciiString ASSOCIATED_TO_STREAM_ID = AsciiString.cached("x-spdy-associated-to-stream-id");
        /**
         * {@code "x-spdy-priority"} — 流优先级，0（最高）到 7（最低）。
         */
        public static final AsciiString PRIORITY = AsciiString.cached("x-spdy-priority");
        /**
         * {@code "x-spdy-scheme"} — 请求 scheme；未设置时编码器默认 {@code https}。
         */
        public static final AsciiString SCHEME = AsciiString.cached("x-spdy-scheme");

        private Names() { }
    }

    private SpdyHttpHeaders() { }
}
