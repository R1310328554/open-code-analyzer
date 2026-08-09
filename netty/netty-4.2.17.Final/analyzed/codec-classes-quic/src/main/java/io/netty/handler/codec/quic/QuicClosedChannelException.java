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

import org.jetbrains.annotations.Nullable;

import java.nio.channels.ClosedChannelException;

/**
 * QUIC 通道关闭异常，当远端触发 {@link QuicConnectionCloseEvent} 导致关闭时，
 * 可通过 {@link #event()} 获取额外关闭详情。
 */
public final class QuicClosedChannelException extends ClosedChannelException {

    private final QuicConnectionCloseEvent event;

    /** 构造异常，可选携带引发关闭的 {@link QuicConnectionCloseEvent}。 */
    QuicClosedChannelException(@Nullable QuicConnectionCloseEvent event) {
        this.event = event;
    }

    /**
     * 返回导致通道关闭的 {@link QuicConnectionCloseEvent}；若未收到则为 {@code null}。
     *
     * @return the event.
     */
    @Nullable
    public QuicConnectionCloseEvent event() {
        return event;
    }
}
