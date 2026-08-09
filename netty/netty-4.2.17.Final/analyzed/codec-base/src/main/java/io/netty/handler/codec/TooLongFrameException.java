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
 * 解码帧长度超过允许上限时抛出的 {@link DecoderException}。
 */
public class TooLongFrameException extends DecoderException {

    private static final long serialVersionUID = -1995801950698951640L;

    /** 创建无详细消息的新实例。 */
    public TooLongFrameException() {
    }

    /** 创建带消息与原因的新实例。 */
    public TooLongFrameException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 创建带消息的新实例。 */
    public TooLongFrameException(String message) {
        super(message);
    }

    /** 创建带原因的新实例。 */
    public TooLongFrameException(Throwable cause) {
        super(cause);
    }
}
