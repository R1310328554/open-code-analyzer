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
 * 解码起始行长度超过允许上限时抛出的 {@link TooLongFrameException}。
 * <p>
 * 由 {@link HttpObjectDecoder} 等在超出 maxInitialLineLength 时触发。
 */
public final class TooLongHttpLineException extends TooLongFrameException {

    private static final long serialVersionUID = 1614751125592211890L;

    /** 创建无消息异常实例。 */
    public TooLongHttpLineException() {
    }

    /** 创建带消息与原因的异常实例。 */
    public TooLongHttpLineException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 创建带消息的异常实例。 */
    public TooLongHttpLineException(String message) {
        super(message);
    }

    /** 创建带原因的异常实例。 */
    public TooLongHttpLineException(Throwable cause) {
        super(cause);
    }
}
