/*
 * Copyright 2012 The Netty Project
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
package io.netty.channel.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.DefaultAddressedEnvelope;

import java.net.InetSocketAddress;

/**
 * The message container that is used for {@link DatagramChannel} to communicate with the remote peer.
 * <p>{@link DatagramChannel} 与远端通信时使用的消息容器，封装负载 {@link ByteBuf} 与收/发地址。</p>
 */
public class DatagramPacket
        extends DefaultAddressedEnvelope<ByteBuf, InetSocketAddress> implements ByteBufHolder {

    /**
     * Create a new instance with the specified packet {@code data} and {@code recipient} address.
     * <p>使用指定数据与接收方地址创建 datagram 包。</p>
     */
    public DatagramPacket(ByteBuf data, InetSocketAddress recipient) {
        super(data, recipient);
    }

    /**
     * Create a new instance with the specified packet {@code data}, {@code recipient} address, and {@code sender}
     * address.
     * <p>使用指定数据、接收方与发送方地址创建 datagram 包（常用于已接收报文的表示）。</p>
     */
    public DatagramPacket(ByteBuf data, InetSocketAddress recipient, InetSocketAddress sender) {
        super(data, recipient, sender);
    }

    @Override
    /** 深拷贝此 datagram 包（含内容缓冲区） */
    public DatagramPacket copy() {
        return replace(content().copy());
    }

    @Override
    /** 返回共享底层存储的浅拷贝视图 */
    public DatagramPacket duplicate() {
        return replace(content().duplicate());
    }

    @Override
    /** 返回保留引用计数的 duplicate 视图 */
    public DatagramPacket retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    @Override
    /** 替换负载内容，保留原收/发地址 */
    public DatagramPacket replace(ByteBuf content) {
        return new DatagramPacket(content, recipient(), sender());
    }

    @Override
    /** 增加引用计数并返回自身 */
    public DatagramPacket retain() {
        super.retain();
        return this;
    }

    @Override
    /** 按增量增加引用计数并返回自身 */
    public DatagramPacket retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    /** 记录最近访问位置（调试用）并返回自身 */
    public DatagramPacket touch() {
        super.touch();
        return this;
    }

    @Override
    /** 带 hint 记录最近访问位置并返回自身 */
    public DatagramPacket touch(Object hint) {
        super.touch(hint);
        return this;
    }
}
