/*
 * Copyright 2019 The Netty Project
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
package io.netty.handler.address;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.net.NetworkInterface;
import java.net.SocketAddress;

/**
 * {@link ChannelOutboundHandler} implementation which allows to dynamically replace the used
 * {@code remoteAddress} and / or {@code localAddress} when making a connection attempt.
 * <p>
 * This can be useful to for example bind to a specific {@link NetworkInterface} based on
 * the {@code remoteAddress}.
 *
 * <p>出站 {@link ChannelOutboundHandler}，在发起连接前动态替换远端和/或本地 {@link SocketAddress}。
 * 典型场景：根据目标地址选择绑定的 {@link NetworkInterface}。连接成功后自动从 pipeline 移除。</p>
 */
public abstract class DynamicAddressConnectHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public final void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                              SocketAddress localAddress, ChannelPromise promise) {
        final SocketAddress remote;
        final SocketAddress local;
        try {
            // 子类可重写以动态决定实际使用的远端/本地地址
            remote = remoteAddress(remoteAddress, localAddress);
            local = localAddress(remoteAddress, localAddress);
        } catch (Exception e) {
            promise.setFailure(e);
            return;
        }
        ctx.connect(remote, local, promise).addListener(future -> {
            if (future.isSuccess()) {
                // We only remove this handler from the pipeline once the connect was successful as otherwise
                // the user may try to connect again.
                // 仅在连接成功后才移除 handler，以便用户重试 connect 时仍能动态解析地址。
                ctx.pipeline().remove(DynamicAddressConnectHandler.this);
            }
        });
    }

    /**
     * Returns the local {@link SocketAddress} to use for
     * {@link ChannelHandlerContext#connect(SocketAddress, SocketAddress)} based on the original {@code remoteAddress}
     * and {@code localAddress}.
     * By default, this method returns the given {@code localAddress}.
     *
     * <p>根据原始远端/本地地址返回实际用于连接的本地 {@link SocketAddress}；默认原样返回 {@code localAddress}。</p>
     */
    protected SocketAddress localAddress(
            @SuppressWarnings("unused") SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        return localAddress;
    }

    /**
     * Returns the remote {@link SocketAddress} to use for
     * {@link ChannelHandlerContext#connect(SocketAddress, SocketAddress)} based on the original {@code remoteAddress}
     * and {@code localAddress}.
     * By default, this method returns the given {@code remoteAddress}.
     *
     * <p>根据原始远端/本地地址返回实际用于连接的远端 {@link SocketAddress}；默认原样返回 {@code remoteAddress}。</p>
     */
    protected SocketAddress remoteAddress(
            SocketAddress remoteAddress, @SuppressWarnings("unused") SocketAddress localAddress) throws Exception {
        return remoteAddress;
    }
}
