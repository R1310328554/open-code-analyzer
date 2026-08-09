/*
 * Copyright 2025 The Netty Project
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
package io.netty.handler.codec.quic;

/**
 * 表示 QUIC 流被 RESET_STREAM 或等效操作重置时抛出的异常。
 */
public final class QuicStreamResetException extends QuicException {

    private final long applicationProtocolCode;

    /** 构造流重置异常，携带可选的应用层协议错误码。 */
    public QuicStreamResetException(String message, long applicationProtocolCode) {
        super(message);

        this.applicationProtocolCode = applicationProtocolCode;
    }

    /**
     * 返回 {@code RESET_STREAM} 帧中携带的应用层协议错误码；未提供时为 {@code -1}。
     *
     * 注意：{@code STOP_SENDING} 帧尚未实现此字段。
     *
     * @return the optional application protocol error code or {@code -1} when no such code is provided.
     */
    public long applicationProtocolCode() {
        return applicationProtocolCode;
    }
}
