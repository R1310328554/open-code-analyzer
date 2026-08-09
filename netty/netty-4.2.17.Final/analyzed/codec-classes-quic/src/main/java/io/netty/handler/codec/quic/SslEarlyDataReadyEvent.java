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


/**
 * 客户端 TLS 1.3 0-RTT 早期数据可发送时触发的用户事件（单例）。
 * 参见 <a href="https://www.rfc-editor.org/rfc/rfc8446#section-4.2.10">RFC8446 4.2.10 Early Data Indication</a>。
 * <p>
 * 可通过 {@link io.netty.channel.Channel#writeAndFlush(Object)} 或
 * {@link io.netty.channel.ChannelHandlerContext#writeAndFlush(Object)} 发送早期数据。
 * 注意：早期数据可能被重放，安全语义与普通数据不同。
 */
public final class SslEarlyDataReadyEvent {

    /** 全局单例事件实例。 */
    static final SslEarlyDataReadyEvent INSTANCE = new SslEarlyDataReadyEvent();

    private SslEarlyDataReadyEvent() { }
}
