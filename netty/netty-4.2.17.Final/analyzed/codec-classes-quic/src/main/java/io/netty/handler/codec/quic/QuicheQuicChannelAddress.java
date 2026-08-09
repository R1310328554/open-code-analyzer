/*
 * Copyright 2024 The Netty Project
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

import java.net.SocketAddress;

/**
 * 将 {@link QuicheQuicChannel} 包装为 {@link SocketAddress}，供 {@link QuicheQuicClientCodec#connect} 识别。
 * 客户端 connect 时通过此地址关联已创建的 QUIC 连接通道。
 */
final class QuicheQuicChannelAddress extends SocketAddress {

    final QuicheQuicChannel channel;

    /** 绑定目标 QUIC 连接通道。 */
    QuicheQuicChannelAddress(QuicheQuicChannel channel) {
        this.channel = channel;
    }
}
