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

import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.net.SocketAddress;

/**
 * The default {@link AddressedEnvelope} implementation.
 * <p>{@link AddressedEnvelope} 的默认实现：封装消息体、收件人地址与可选发件人地址，
 * 若消息为 {@link ReferenceCounted} 则委托引用计数操作。</p>
 *
 * @param <M> the type of the wrapped message
 * @param <A> the type of the recipient address
 */
public class DefaultAddressedEnvelope<M, A extends SocketAddress> implements AddressedEnvelope<M, A> {

    /** 封装的消息体 */
    private final M message;
    /** 发件人地址，可为 {@code null} */
    private final A sender;
    /** 收件人地址 */
    private final A recipient;

    /**
     * Creates a new instance with the specified {@code message}, {@code recipient} address, and
     * {@code sender} address.
     * <p>创建带消息、收件人与发件人地址的实例；recipient 与 sender 不能同时为 {@code null}。</p>
     */
    public DefaultAddressedEnvelope(M message, A recipient, A sender) {
        ObjectUtil.checkNotNull(message, "message");
        if (recipient == null && sender == null) {
            throw new NullPointerException("recipient and sender");
        }

        this.message = message;
        this.sender = sender;
        this.recipient = recipient;
    }

    /**
     * Creates a new instance with the specified {@code message} and {@code recipient} address.
     * The sender address becomes {@code null}.
     * <p>仅指定消息与收件人地址，发件人为 {@code null}。</p>
     */
    public DefaultAddressedEnvelope(M message, A recipient) {
        this(message, recipient, null);
    }

    /** 返回封装的消息体。 */
    @Override
    public M content() {
        return message;
    }

    /** 返回发件人地址。 */
    @Override
    public A sender() {
        return sender;
    }

    /** 返回收件人地址。 */
    @Override
    public A recipient() {
        return recipient;
    }

    @Override
    public int refCnt() {
        if (message instanceof ReferenceCounted) {
            return ((ReferenceCounted) message).refCnt();
        } else {
            return 1;
        }
    }

    @Override
    public AddressedEnvelope<M, A> retain() {
        ReferenceCountUtil.retain(message);
        return this;
    }

    @Override
    public AddressedEnvelope<M, A> retain(int increment) {
        ReferenceCountUtil.retain(message, increment);
        return this;
    }

    @Override
    public boolean release() {
        return ReferenceCountUtil.release(message);
    }

    @Override
    public boolean release(int decrement) {
        return ReferenceCountUtil.release(message, decrement);
    }

    @Override
    public AddressedEnvelope<M, A> touch() {
        ReferenceCountUtil.touch(message);
        return this;
    }

    @Override
    public AddressedEnvelope<M, A> touch(Object hint) {
        ReferenceCountUtil.touch(message, hint);
        return this;
    }

    @Override
    public String toString() {
        if (sender != null) {
            return StringUtil.simpleClassName(this) +
                    '(' + sender + " => " + recipient + ", " + message + ')';
        } else {
            return StringUtil.simpleClassName(this) +
                    "(=> " + recipient + ", " + message + ')';
        }
    }
}
