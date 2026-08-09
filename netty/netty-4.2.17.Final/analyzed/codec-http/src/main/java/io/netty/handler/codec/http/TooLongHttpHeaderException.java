/*
 * Copyright 2022 The Netty Project
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

import io.netty.handler.codec.TooLongFrameException;

/**
 * 解码头部长度超过允许上限时抛出的 {@link TooLongFrameException}。
 * <p>
 * 由 {@link HttpObjectDecoder} 等在超出 maxHeaderSize 时触发。
 */
public final class TooLongHttpHeaderException extends TooLongFrameException {

    private static final long serialVersionUID = -8295159138628369730L;

    /** 创建无消息异常实例。 */
    public TooLongHttpHeaderException() {
    }

    /** 创建带消息与原因的异常实例。 */
    public TooLongHttpHeaderException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 创建带消息的异常实例。 */
    public TooLongHttpHeaderException(String message) {
        super(message);
    }

    /** 创建带原因的异常实例。 */
    public TooLongHttpHeaderException(Throwable cause) {
        super(cause);
    }
}
