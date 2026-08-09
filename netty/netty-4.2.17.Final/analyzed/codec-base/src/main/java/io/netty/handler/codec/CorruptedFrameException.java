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
package io.netty.handler.codec;

/**
 * 入站帧数据无法被解码时抛出的 {@link DecoderException}，表示帧格式损坏或非法。
 */
public class CorruptedFrameException extends DecoderException {

    private static final long serialVersionUID = 3918052232492988408L;

    /** 创建无参实例。 */
    public CorruptedFrameException() {
    }

    /** 创建新实例。 */
    public CorruptedFrameException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 创建新实例。 */
    public CorruptedFrameException(String message) {
        super(message);
    }

    /** 创建新实例。 */
    public CorruptedFrameException(Throwable cause) {
        super(cause);
    }
}
