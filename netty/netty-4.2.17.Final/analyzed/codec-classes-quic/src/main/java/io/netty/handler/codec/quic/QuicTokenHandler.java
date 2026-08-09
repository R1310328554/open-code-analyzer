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

import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;

/**
 * QUIC 地址验证 token 的生成与校验接口，用于服务端防放大攻击。
 * 实现类负责在 Initial 报文中写入 token，并在客户端重连时验证。
 */
public interface QuicTokenHandler {

    /**
     * 为给定目的连接 ID 与客户端地址生成 token 并写入 {@code out}。
     * 若不需要 token 验证（不写入 token），应返回 {@code false}。
     *
     * @param out       {@link ByteBuf} into which the token will be written.
     * @param dcid      the destination connection id. The {@link ByteBuf#readableBytes()} will be at most
     *                  {@link Quic#MAX_CONN_ID_LEN}.
     * @param address   the {@link InetSocketAddress} of the sender.
     * @return          {@code true} if a token was written and so validation should happen, {@code false} otherwise.
     */
    boolean writeToken(ByteBuf out, ByteBuf dcid, InetSocketAddress address);

    /**
     * 校验 token 有效性；有效时返回 token 之后的数据起始偏移，无效时返回 {@code -1}。
     *
     * @param token     the {@link ByteBuf} that contains the token. The ownership is not transferred.
     * @param address   the {@link InetSocketAddress} of the sender.
     * @return          the start index after the token or {@code -1} if the token was not valid.
     */
    int validateToken(ByteBuf token, InetSocketAddress address);

    /**
     * 返回本实现支持的最大 token 长度。
     *
     * @return the maximal supported token length.
     */
    int maxTokenLength();
}
