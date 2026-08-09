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
 * HTTP/1.0 及以下消息携带 {@code Transfer-Encoding} 头时由 {@link HttpObjectDecoder} 抛出。
 * <p>
 * 按 RFC 9112，Transfer-Encoding 仅 HTTP/1.1 及更高版本允许。
 */
public final class TransferEncodingNotAllowedException extends DecoderException {
    /**
     * 以指定消息创建异常实例。
     * @param message The exception message.
     */
    public TransferEncodingNotAllowedException(String message) {
        super(message);
    }
}
