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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

/**
 * A UDP/IP {@link Channel}.
 * <p>UDP/IP {@link Channel}，支持无连接 datagram 收发及可选的 connect、组播等操作。</p>
 */
public interface DatagramChannel extends Channel {
    /** 返回 datagram channel 配置 */
    @Override
    DatagramChannelConfig config();
    /** 返回本地绑定地址 */
    @Override
    InetSocketAddress localAddress();
    /** 返回远端地址（未 connect 时可能为 {@code null}） */
    @Override
    InetSocketAddress remoteAddress();

    /**
     * Return {@code true} if the {@link DatagramChannel} is connected to the remote peer.
     * <p>若此 {@link DatagramChannel} 已通过 connect 关联远端 peer，则返回 {@code true}。</p>
     */
    boolean isConnected();

    /**
     * Joins a multicast group and notifies the {@link ChannelFuture} once the operation completes.
     * <p>加入组播组，操作完成后通知 {@link ChannelFuture}。</p>
     */
    ChannelFuture joinGroup(InetAddress multicastAddress);

    /**
     * Joins a multicast group and notifies the {@link ChannelFuture} once the operation completes.
     *
     * The given {@link ChannelFuture} will be notified and also returned.
     * <p>加入组播组；指定的 {@link ChannelFuture} 会在完成时被通知并作为返回值。</p>
     */
    ChannelFuture joinGroup(InetAddress multicastAddress, ChannelPromise future);

    /**
     * Joins the specified multicast group at the specified interface and notifies the {@link ChannelFuture}
     * once the operation completes.
     * <p>在指定网络接口上加入组播组，完成后通知 {@link ChannelFuture}。</p>
     */
    ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface);

    /**
     * Joins the specified multicast group at the specified interface and notifies the {@link ChannelFuture}
     * once the operation completes.
     *
     * The given {@link ChannelFuture} will be notified and also returned.
     * <p>在指定接口上加入组播组；传入的 {@link ChannelFuture} 会被通知并返回。</p>
     */
    ChannelFuture joinGroup(
            InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise future);

    /**
     * Joins the specified multicast group at the specified interface and notifies the {@link ChannelFuture}
     * once the operation completes.
     * <p>在指定接口上加入组播组（可指定源过滤地址 {@code source}）。</p>
     */
    ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source);

    /**
     * Joins the specified multicast group at the specified interface and notifies the {@link ChannelFuture}
     * once the operation completes.
     *
     * The given {@link ChannelFuture} will be notified and also returned.
     * <p>在指定接口上加入组播组（含源地址）；传入的 {@link ChannelFuture} 会被通知并返回。</p>
     */
    ChannelFuture joinGroup(
            InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise future);

    /**
     * Leaves a multicast group and notifies the {@link ChannelFuture} once the operation completes.
     * <p>离开组播组，完成后通知 {@link ChannelFuture}。</p>
     */
    ChannelFuture leaveGroup(InetAddress multicastAddress);

    /**
     * Leaves a multicast group and notifies the {@link ChannelFuture} once the operation completes.
     *
     * The given {@link ChannelFuture} will be notified and also returned.
     * <p>离开组播组；传入的 {@link ChannelFuture} 会被通知并返回。</p>
     */
    ChannelFuture leaveGroup(InetAddress multicastAddress, ChannelPromise future);

    /**
     * Leaves a multicast group on a specified local interface and notifies the {@link ChannelFuture} once the
     * operation completes.
     * <p>在指定本地接口上离开组播组。</p>
     */
    ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface);

    /**
     * Leaves a multicast group on a specified local interface and notifies the {@link ChannelFuture} once the
     * operation completes.
     *
     * The given {@link ChannelFuture} will be notified and also returned.
     * <p>在指定本地接口上离开组播组；传入的 {@link ChannelFuture} 会被通知并返回。</p>
     */
    ChannelFuture leaveGroup(
            InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise future);

    /**
     * Leave the specified multicast group at the specified interface using the specified source and notifies
     * the {@link ChannelFuture} once the operation completes.
     * <p>在指定接口上离开组播组（含源地址过滤）。</p>
     */
    ChannelFuture leaveGroup(
            InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source);

    /**
     * Leave the specified multicast group at the specified interface using the specified source and notifies
     * the {@link ChannelFuture} once the operation completes.
     *
     * The given {@link ChannelFuture} will be notified and also returned.
     * <p>在指定接口上离开组播组（含源地址）；传入的 {@link ChannelFuture} 会被通知并返回。</p>
     */
    ChannelFuture leaveGroup(
            InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source,
            ChannelPromise future);

    /**
     * Block the given sourceToBlock address for the given multicastAddress on the given networkInterface and notifies
     * the {@link ChannelFuture} once the operation completes.
     *
     * The given {@link ChannelFuture} will be notified and also returned.
     * <p>在指定接口上屏蔽来自 {@code sourceToBlock} 的组播源。</p>
     */
    ChannelFuture block(
            InetAddress multicastAddress, NetworkInterface networkInterface,
            InetAddress sourceToBlock);

    /**
     * Block the given sourceToBlock address for the given multicastAddress on the given networkInterface and notifies
     * the {@link ChannelFuture} once the operation completes.
     *
     * The given {@link ChannelFuture} will be notified and also returned.
     * <p>在指定接口上屏蔽组播源；传入的 {@link ChannelFuture} 会被通知并返回。</p>
     */
    ChannelFuture block(
            InetAddress multicastAddress, NetworkInterface networkInterface,
            InetAddress sourceToBlock, ChannelPromise future);

    /**
     * Block the given sourceToBlock address for the given multicastAddress and notifies the {@link ChannelFuture} once
     * the operation completes.
     *
     * The given {@link ChannelFuture} will be notified and also returned.
     * <p>屏蔽指定组播地址上的 {@code sourceToBlock} 源。</p>
     */
    ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock);

    /**
     * Block the given sourceToBlock address for the given multicastAddress and notifies the {@link ChannelFuture} once
     * the operation completes.
     *
     * The given {@link ChannelFuture} will be notified and also returned.
     * <p>屏蔽组播源；传入的 {@link ChannelFuture} 会被通知并返回。</p>
     */
    ChannelFuture block(
            InetAddress multicastAddress, InetAddress sourceToBlock, ChannelPromise future);
}
