/*
 * Copyright 2013 The Netty Project
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

package io.netty.channel;

import io.netty.util.ReferenceCounted;

import java.net.SocketAddress;

/**
 * A message that wraps another message with a sender address and a recipient address.
 * <p>带发送方与接收方地址的消息封装，常用于无连接传输（如 {@link io.netty.channel.socket.DatagramChannel}）
 * 中携带 {@link #content()} 载荷及 {@link #sender()}/{@link #recipient()} 端点信息。
 * 继承 {@link ReferenceCounted} 以支持引用计数生命周期管理。</p>
 *
 * @param <M> 被封装的消息类型
 * @param <A> 地址类型，须为 {@link SocketAddress} 子类
 */
public interface AddressedEnvelope<M, A extends SocketAddress> extends ReferenceCounted {
    /**
     * Returns the message wrapped by this envelope message.
     * <p>返回本信封消息所封装的有效载荷。</p>
     */
    M content();

    /**
     * Returns the address of the sender of this message.
     * <p>返回消息发送方地址；接收端据此识别数据来源。</p>
     */
    A sender();

    /**
     * Returns the address of the recipient of this message.
     * <p>返回消息预期接收方地址；发送端指定目标端点。</p>
     */
    A recipient();

    /** 增加引用计数并返回自身。 */
    @Override
    AddressedEnvelope<M, A> retain();

    /** 按指定增量增加引用计数并返回自身。 */
    @Override
    AddressedEnvelope<M, A> retain(int increment);

    /** 记录最近访问位置（调试用）并返回自身。 */
    @Override
    AddressedEnvelope<M, A> touch();

    /** 在指定提示下记录访问位置并返回自身。 */
    @Override
    AddressedEnvelope<M, A> touch(Object hint);
}
