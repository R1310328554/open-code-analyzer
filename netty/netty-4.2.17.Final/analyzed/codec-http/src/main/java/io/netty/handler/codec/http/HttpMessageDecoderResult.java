/*
 * Copyright 2021 The Netty Project
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

import io.netty.handler.codec.DecoderResult;

/**
 * {@link HttpObjectDecoder} 成功解码 {@link HttpMessage} 时附带的 {@link DecoderResult}。
 * <p>
 * 不保证解码器一定返回本类型，也可能返回普通 {@link DecoderResult}；
 * 本类额外记录起始行与头部占用的字节数，便于监控与限流。
 */
public final class HttpMessageDecoderResult extends DecoderResult {

    /** 起始行字节长度（受 maxInitialLineLength 约束）。 */
    private final int initialLineLength;
    /** 全部头部字节长度（受 maxHeaderSize 约束）。 */
    private final int headerSize;

    HttpMessageDecoderResult(int initialLineLength, int headerSize) {
        super(SIGNAL_SUCCESS);
        this.initialLineLength = initialLineLength;
        this.headerSize = headerSize;
    }

    /**
     * 已解码起始行的字节长度（受 {@code maxInitialLineLength} 限制）。
     */
    public int initialLineLength() {
        return initialLineLength;
    }

    /**
     * 已解码头部的总字节长度（受 {@code maxHeaderSize} 限制）。
     */
    public int headerSize() {
        return headerSize;
    }

    /**
     * 起始行与头部字节长度之和。
     */
    public int totalSize() {
        return initialLineLength + headerSize;
    }
}
