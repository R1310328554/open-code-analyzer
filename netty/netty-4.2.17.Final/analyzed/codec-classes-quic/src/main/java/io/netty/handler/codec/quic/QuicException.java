/*
 * Copyright 2020 The Netty Project
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

import org.jetbrains.annotations.Nullable;

/**
 * QUIC 协议处理过程中抛出的异常，可携带 {@link QuicTransportError} 传输错误码。
 */
public class QuicException extends Exception {

    private final QuicTransportError error;

    QuicException(String message) {
        super(message);
        this.error = null;
    }

    public QuicException(QuicTransportError error) {
        super(error.name());
        this.error = error;
    }

    public QuicException(String message, QuicTransportError error) {
        super(message);
        this.error = error;
    }

    public QuicException(Throwable cause, QuicTransportError error) {
        super(cause);
        this.error = error;
    }

    public QuicException(String message, Throwable cause, QuicTransportError error) {
        super(message, cause);
        this.error = error;
    }

    /**
     * 返回导致此 {@link QuicException} 的 {@link QuicTransportError}；
     * 若由其他原因引起则为 {@code null}。
     *
     * @return  the {@link QuicTransportError} that caused this {@link QuicException} or {@code null} if
     *          it was caused by something different.
     */
    @Nullable
    public QuicTransportError error() {
        return error;
    }
}
