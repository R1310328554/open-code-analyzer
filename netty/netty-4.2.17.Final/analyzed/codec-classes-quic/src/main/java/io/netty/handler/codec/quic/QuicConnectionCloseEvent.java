/*
 * Copyright 2023 The Netty Project
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

import java.util.Arrays;

/**
 * 远端发送
 * <a href="https://www.rfc-editor.org/rfc/rfc9000#name-connection_close-frames">CONNECTION_CLOSE 帧</a>
 * 时产生的事件，便于检查连接关闭原因与错误码。
 */
public final class QuicConnectionCloseEvent implements QuicEvent {

    final boolean applicationClose;
    final int error;
    final byte[] reason;

    QuicConnectionCloseEvent(boolean applicationClose, int error, byte[] reason) {
        this.applicationClose = applicationClose;
        this.error = error;
        this.reason = reason;
    }

    /**
     * 若为应用层主动关闭则返回 {@code true}，否则为传输层关闭。
     *
     * @return  if this is an application close.
     */
    public boolean isApplicationClose() {
        return applicationClose;
    }

    /**
     * 返回关闭帧携带的错误码。
     *
     * @return the error.
     */
    public int error() {
        return error;
    }

    /**
     * 若错误码表示 <a href="https://www.rfc-editor.org/rfc/rfc9001#section-4.8">TLS 错误</a> 则返回 {@code true}。
     * @return {@code true} if this is an {@code TLS error}, {@code false} otherwise.
     */
    public boolean isTlsError() {
        return !applicationClose && error >= 0x0100;
    }

    /**
     * 返回关闭原因字节数组；远端未提供时可能为空数组。
     *
     * @return  the reason.
     */
    public byte[] reason() {
        return reason.clone();
    }

    @Override
    public String toString() {
        return "QuicConnectionCloseEvent{" +
                "applicationClose=" + applicationClose +
                ", error=" + error +
                ", reason=" + Arrays.toString(reason) +
                '}';
    }

    /**
     * 从 QUIC 错误码中提取内嵌的 TLS 错误码；若无 TLS 错误则返回 {@code -1}。
     *
     * @param error the {@code QUIC error}
     * @return      the {@code TLS error} or {@code -1} if there was no {@code TLS error} contained.
     */
    public static int extractTlsError(int error) {
        int tlsError = error - 0x0100;
        if (tlsError < 0) {
            return -1;
        }
        return tlsError;
    }
}
