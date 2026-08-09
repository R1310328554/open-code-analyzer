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
package io.netty.handler.codec.compression;

import io.netty.handler.codec.EncoderException;

/**
 * 压缩失败时抛出的 {@link EncoderException} 子类。
 */
public class CompressionException extends EncoderException {

    private static final long serialVersionUID = 5603413481274811897L;

    /** 创建无消息的压缩异常。 */
    public CompressionException() {
    }

    /** 创建带消息与原因的压缩异常。 */
    public CompressionException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 创建带消息的压缩异常。 */
    public CompressionException(String message) {
        super(message);
    }

    /** 创建仅带原因的压缩异常。 */
    public CompressionException(Throwable cause) {
        super(cause);
    }
}
