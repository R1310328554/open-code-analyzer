/*
 * Copyright 2021 The Netty Project
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
package io.netty.channel.unix;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.DefaultAddressedEnvelope;

/**
 * The message container that is used for {@link DomainDatagramChannel} to communicate with the remote peer.
 * <p>Unix 域数据报消息容器：承载 {@link ByteBuf} 负载、目标 {@link DomainSocketAddress}  及可选的发送方地址；实现 {@link ByteBufHolder} 以支持引用计数。</p>
 */
public final class DomainDatagramPacket
        extends DefaultAddressedEnvelope<ByteBuf, DomainSocketAddress> implements ByteBufHolder {

    /**
     * Create a new instance with the specified packet {@code data} and {@code recipient} address.
     * <p>构造仅含目标地址的数据报（发送路径常用）。</p>
     */
    public DomainDatagramPacket(ByteBuf data, DomainSocketAddress recipient) {
        super(data, recipient);
    }

    /**
     * Create a new instance with the specified packet {@code data}, {@code recipient} address, and {@code sender}
     * address.
     * <p>构造含发送方与接收方地址的数据报（接收路径由 JNI 填充 sender）。</p>
     */
    public DomainDatagramPacket(ByteBuf data, DomainSocketAddress recipient, DomainSocketAddress sender) {
        super(data, recipient, sender);
    }

    /** 深拷贝 {@link ByteBuf} 内容并保留地址信息 */
    @Override
    public DomainDatagramPacket copy() {
        return replace(content().copy());
    }

    /** 共享底层存储的浅拷贝视图 */
    @Override
    public DomainDatagramPacket duplicate() {
        return replace(content().duplicate());
    }

    /** 替换负载 {@link ByteBuf}，地址字段不变 */
    @Override
    public DomainDatagramPacket replace(ByteBuf content) {
        return new DomainDatagramPacket(content, recipient(), sender());
    }

    @Override
    public DomainDatagramPacket retain() {
        super.retain();
        return this;
    }

    @Override
    public DomainDatagramPacket retain(int increment) {
        super.retain(increment);
        return this;
    }

    /** 返回引用计数 +1 的 duplicate 视图 */
    @Override
    public DomainDatagramPacket retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    @Override
    public DomainDatagramPacket touch() {
        super.touch();
        return this;
    }

    @Override
    public DomainDatagramPacket touch(Object hint) {
        super.touch(hint);
        return this;
    }
}
