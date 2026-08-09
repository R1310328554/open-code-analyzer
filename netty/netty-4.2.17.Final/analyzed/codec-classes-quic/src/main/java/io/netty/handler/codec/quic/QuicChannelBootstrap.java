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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 引导 {@link QuicChannel} 建立客户端 QUIC 连接的 Bootstrap。
 */
public final class QuicChannelBootstrap {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(QuicChannelBootstrap.class);

    /** 作为 UDP 传输的父 Channel。 */
    private final Channel parent;
    // ChannelOption 应用顺序可能影响相互校验，故使用 LinkedHashMap 保持插入顺序
    private final Map<ChannelOption<?>, Object> options = new LinkedHashMap<>();
    private final Map<AttributeKey<?>, Object> attrs = new HashMap<>();
    private final Map<ChannelOption<?>, Object> streamOptions = new LinkedHashMap<>();
    private final Map<AttributeKey<?>, Object> streamAttrs = new HashMap<>();

    private SocketAddress local;
    private SocketAddress remote;
    private QuicConnectionAddress localAddress = QuicConnectionAddress.EPHEMERAL;
    private QuicConnectionAddress remoteAddress = QuicConnectionAddress.EPHEMERAL;
    private ChannelHandler handler;
    private ChannelHandler streamHandler;

    /**
     * 使用给定传输 {@link Channel} 创建 bootstrap；其 pipeline 须已包含 QUIC codec。
     *
     * @param parent    传输层 {@link Channel}。
     * @deprecated 请改用 {@link QuicChannel#newBootstrap(Channel)}。
     */
    @Deprecated
    public QuicChannelBootstrap(Channel parent) {
        Quic.ensureAvailability();
        this.parent = ObjectUtil.checkNotNull(parent, "parent");
    }

    /**
     * 为新建 {@link QuicChannel} 设置 {@link ChannelOption}；{@code null} 表示移除先前选项。
     *
     * @param option    要应用的 {@link ChannelOption}。
     * @param value     选项值。
     * @param <T>       值类型。
     * @return          本实例（链式调用）。
     */
    public <T> QuicChannelBootstrap option(ChannelOption<T> option, @Nullable T value) {
        Quic.updateOptions(options, option, value);
        return this;
    }

    /**
     * 为新建 {@link QuicChannel} 设置初始 {@link AttributeKey}；{@code null} 表示移除。
     *
     * @param key       属性键。
     * @param value     属性值。
     * @param <T>       值类型。
     * @return          本实例。
     */
    public <T> QuicChannelBootstrap attr(AttributeKey<T> key, @Nullable T value) {
        Quic.updateAttributes(attrs, key, value);
        return this;
    }

    /**
     * 设置创建后自动加入 {@link QuicChannel} pipeline 的 {@link ChannelHandler}。
     *
     * @param handler   连接级 handler。
     * @return          本实例。
     */
    public QuicChannelBootstrap handler(ChannelHandler handler) {
        this.handler = ObjectUtil.checkNotNull(handler, "handler");
        return this;
    }

    /**
     * 为新建 {@link QuicStreamChannel} 设置 {@link ChannelOption}。
     *
     * @param option    流级选项。
     * @param value     选项值。
     * @param <T>       值类型。
     * @return          本实例。
     */
    public <T> QuicChannelBootstrap streamOption(ChannelOption<T> option, @Nullable T value) {
        Quic.updateOptions(streamOptions, option, value);
        return this;
    }

    /**
     * 为新建 {@link QuicStreamChannel} 设置初始属性。
     *
     * @param key       属性键。
     * @param value     属性值。
     * @param <T>       值类型。
     * @return          本实例。
     */
    public <T> QuicChannelBootstrap streamAttr(AttributeKey<T> key, @Nullable T value) {
        Quic.updateAttributes(streamAttrs, key, value);
        return this;
    }

    /**
     * 设置创建后自动加入 {@link QuicStreamChannel} pipeline 的 handler。
     *
     * @param streamHandler     流级 handler。
     * @return                  本实例。
     */
    public QuicChannelBootstrap streamHandler(ChannelHandler streamHandler) {
        this.streamHandler = ObjectUtil.checkNotNull(streamHandler, "streamHandler");
        return this;
    }

    /**
     * 设置本地 UDP {@link SocketAddress}。
     *
     * @param local    本地地址。
     * @return          本实例。
     */
    public QuicChannelBootstrap localAddress(SocketAddress local) {
        this.local = ObjectUtil.checkNotNull(local, "local");
        return this;
    }

    /**
     * 设置要连接的远端 {@link SocketAddress}。
     *
     * @param remote    远端地址。
     * @return          本实例。
     */
    public QuicChannelBootstrap remoteAddress(SocketAddress remote) {
        this.remote = ObjectUtil.checkNotNull(remote, "remote");
        return this;
    }

    /**
     * 设置本地 {@link QuicConnectionAddress}；未指定时在连接时随机生成。
     *
     * @param connectionAddress     本地 QUIC 连接地址。
     * @return                      本实例。
     * @deprecated                  请使用 {@link #localConnectionAddress(QuicConnectionAddress)}。
     */
    @Deprecated
    public QuicChannelBootstrap connectionAddress(QuicConnectionAddress connectionAddress) {
        this.localAddress = ObjectUtil.checkNotNull(connectionAddress, "connectionAddress");
        return this;
    }

    /**
     * 设置本地 {@link QuicConnectionAddress}；未指定时在连接时随机生成。
     *
     * @param localConnectionAddress     本地 QUIC 连接地址。
     * @return                      本实例。
     */
    public QuicChannelBootstrap localConnectionAddress(QuicConnectionAddress localConnectionAddress) {
        this.localAddress = ObjectUtil.checkNotNull(localConnectionAddress, "localConnectionAddress");
        return this;
    }

    /**
     * 设置远端 {@link QuicConnectionAddress}；须不可预测以保证连接安全，详见
     * <a href="https://datatracker.ietf.org/doc/html/rfc9000#section-7.2">RFC9000 §7.2</a>。
     *
     * @param remoteConnectionAddress     远端 QUIC 连接地址。
     * @return                            本实例。
     */
    public QuicChannelBootstrap remoteConnectionAddress(QuicConnectionAddress remoteConnectionAddress) {
        this.remoteAddress = ObjectUtil.checkNotNull(remoteConnectionAddress, "remoteConnectionAddress");
        return this;
    }

    /**
     * 连接远端并建立 {@link QuicChannel}，完成后通知 future。
     *
     * @return 连接完成时通知的 {@link Future}。
     */
    public Future<QuicChannel> connect() {
        return connect(parent.eventLoop().newPromise());
    }

    /**
     * 连接远端并建立 {@link QuicChannel}，完成后通知 promise。
     *
     * @param promise   完成时通知的 {@link Promise}。
     * @return          同上的 {@link Future}。
     */
    public Future<QuicChannel> connect(Promise<QuicChannel> promise) {
        if (handler == null && streamHandler == null) {
            throw new IllegalStateException("handler and streamHandler not set");
        }
        SocketAddress local = this.local;
        if (local == null) {
            local = parent.localAddress();
        }
        if (local == null) {
            local = new InetSocketAddress(0);
        }

        SocketAddress remote = this.remote;
        if (remote == null) {
            remote = parent.remoteAddress();
        }
        if (remote == null) {
            throw new IllegalStateException("remote not set");
        }

        final QuicConnectionAddress localaddress = localAddress;
        final QuicConnectionAddress remoteaddress = remoteAddress;
        QuicChannel channel = QuicheQuicChannel.forClient(parent, (InetSocketAddress)  local,
                (InetSocketAddress) remote,
                streamHandler, Quic.toOptionsArray(streamOptions), Quic.toAttributesArray(streamAttrs));

        Quic.setupChannel(channel, Quic.toOptionsArray(options), Quic.toAttributesArray(attrs), handler, logger);
        EventLoop eventLoop = parent.eventLoop();
        eventLoop.register(channel).addListener((ChannelFuture future) -> {
            Throwable cause = future.cause();
            if (cause != null) {
                promise.setFailure(cause);
            } else {
                channel.connect(remoteaddress, localaddress).addListener(f -> {
                    Throwable error = f.cause();
                    if (error != null) {
                        promise.setFailure(error);
                    } else {
                        promise.setSuccess(channel);
                    }
                });
            }
        });
        return promise;
    }
}
