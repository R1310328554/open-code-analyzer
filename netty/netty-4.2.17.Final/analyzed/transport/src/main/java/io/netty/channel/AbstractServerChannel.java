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
package io.netty.channel;

import java.net.SocketAddress;

/**
 * A skeletal server-side {@link Channel} implementation.  A server-side
 * {@link Channel} does not allow the following operations:
 * <p>服务端 {@link Channel} 的骨架实现。监听套接字本身不接受客户端语义的操作，
 * 以下出站能力均会抛出 {@link UnsupportedOperationException}：</p>
 * <ul>
 * <li>{@link #connect(SocketAddress, ChannelPromise)} — 服务端通道不主动连接远端</li>
 * <li>{@link #disconnect(ChannelPromise)} — 无对等连接可断开</li>
 * <li>{@link #write(Object, ChannelPromise)} / {@link #flush()} — 数据经接受的子通道写出</li>
 * <li>以及调用上述方法的便捷重载</li>
 * </ul>
 */
public abstract class AbstractServerChannel extends AbstractChannel implements ServerChannel {
    /** 服务端通道元数据：无连接语义，默认最大报文数 16。 */
    private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);

    /**
     * Creates a new instance.
     * <p>创建无父通道的服务端 {@link Channel} 实例。</p>
     */
    protected AbstractServerChannel() {
        super(null);
    }

    /** 返回服务端通道固定元数据。 */
    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    /** 服务端监听通道无固定远端地址，恒为 {@code null}。 */
    @Override
    public SocketAddress remoteAddress() {
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;
    }

    /** 服务端通道不支持断开操作。 */
    @Override
    protected void doDisconnect() throws Exception {
        throw new UnsupportedOperationException();
    }

    /** 创建服务端专用的 {@link AbstractUnsafe} 实现。 */
    @Override
    protected AbstractUnsafe newUnsafe() {
        return new DefaultServerUnsafe();
    }

    /** 服务端通道不支持直接写出消息。 */
    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        throw new UnsupportedOperationException();
    }

    /** 出站消息过滤在服务端通道上不可用。 */
    @Override
    protected final Object filterOutboundMessage(Object msg) {
        throw new UnsupportedOperationException();
    }

    /** 拦截 connect 请求并以失败完成对应的 {@link ChannelPromise}。 */
    private final class DefaultServerUnsafe extends AbstractUnsafe {
        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            safeSetFailure(promise, new UnsupportedOperationException());
        }
    }
}
