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
package io.netty.handler.codec.http2;

import io.netty.channel.ChannelOption;

/**
 * {@link Http2StreamChannel} 专用 {@link ChannelOption}。
 *
 * @param <T>   the type of the value which is valid for the {@link ChannelOption}
 */
public final class Http2StreamChannelOption<T> extends ChannelOption<T> {
    private Http2StreamChannelOption(String name) {
        super(name);
    }

    /**
     * 为 {@code true} 时，数据经 {@link io.netty.channel.ChannelPipeline#fireChannelRead(Object)} 交付用户后，
     * 自动发送 {@link Http2WindowUpdateFrame} 以恢复流级接收窗口；为 {@code false} 时由用户自行控制 WINDOW_UPDATE 时机。
     * <p>
     * 详见 <a href="https://datatracker.ietf.org/doc/html/rfc9113#section-5.2">RFC9113 5.2 流控</a>。
     */
    public static final ChannelOption<Boolean> AUTO_STREAM_FLOW_CONTROL =
            valueOf("AUTO_STREAM_FLOW_CONTROL");
}
