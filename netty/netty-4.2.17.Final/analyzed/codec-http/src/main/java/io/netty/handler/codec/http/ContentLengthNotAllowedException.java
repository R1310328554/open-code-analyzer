/*
 * Copyright 2026 The Netty Project
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
package io.netty.handler.codec.http;

import io.netty.handler.codec.DecoderException;

/**
 * 同一消息同时含 {@code Transfer-Encoding} 与 {@code Content-Length} 时抛出。
 * <p>
 * RFC 9112 禁止发送方在同一报文中同时携带两者，服务器可拒绝此类请求。
 * 默认由 {@link HttpObjectDecoder#handleTransferEncodingChunkedWithContentLength(HttpMessage)} 抛出。
 */
public final class ContentLengthNotAllowedException extends DecoderException {
    /**
     * 使用指定消息创建异常实例。
     * @param message The exception message.
     */
    public ContentLengthNotAllowedException(String message) {
        super(message);
    }
}
