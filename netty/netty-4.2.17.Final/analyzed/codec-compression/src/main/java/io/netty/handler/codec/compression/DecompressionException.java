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

import io.netty.handler.codec.DecoderException;

/**
 * 解压失败时抛出的 {@link DecoderException} 子类，表示数据损坏或格式非法。
 */
public class DecompressionException extends DecoderException {

    private static final long serialVersionUID = 3546272712208105199L;

    /** 构造无消息的解压异常。 */
    public DecompressionException() {
    }

    /** 构造带消息与原因的解压异常。 */
    public DecompressionException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 构造带消息的解压异常。 */
    public DecompressionException(String message) {
        super(message);
    }

    /** 构造仅含原因的解压异常。 */
    public DecompressionException(Throwable cause) {
        super(cause);
    }
}
