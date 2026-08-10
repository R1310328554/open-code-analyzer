/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.sctp;

import com.sun.nio.sctp.Association;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 * A SCTP/IP  {@link Channel} interface for single SCTP association.
 * <p>面向消息的已连接 SCTP 传输：支持多流（multi-streaming）与多宿主（multi-homing）； 一条关联对应一个 {@link io.netty.channel.sctp.SctpChannel} 实例。</p>
 *
 * <p>
 * The SctpChannel is a message-oriented, connected transport which supports multi-streaming and multi-homing.
 * </p>
 */
public interface SctpChannel extends Channel {
    @Override
    SctpServerChannel parent();

    /**
     * Returns the underlying SCTP association.
     * <p>返回底层 JDK {@link Association}，关联未建立时可能为 {@code null}。</p>
     */
    Association association();

    /**
     * Return the (primary) local address of the SCTP channel.
     *
     * Please note that, this return the first local address in the underlying SCTP Channel's
     * local address iterator to support Netty Channel API. In other words, its the application's
     * responsibility to keep track of it's local primary address.
     *
     * (To set a local address as primary, the application can request by calling local SCTP stack,
     * with SctpStandardSocketOption.SCTP_PRIMARY_ADDR option).
     * <p>返回主本地地址（迭代器首个）；多宿主时需应用自行跟踪主地址， 可用 {@code SCTP_PRIMARY_ADDR} 设置。</p>
     */
    @Override
    InetSocketAddress localAddress();

    /**
     * Return all local addresses of the SCTP  channel.
     * Please note that, it will return more than one address if this channel is using multi-homing
     * <p>返回全部本地传输地址；启用 multi-homing 时集合可含多个 {@link InetSocketAddress}。</p>
     */
    Set<InetSocketAddress> allLocalAddresses();

    /**
     * Returns the {@link SctpChannelConfig} configuration of the channel.
     * <p>返回 SCTP 通道配置（NODELAY、缓冲、INIT 流等）。</p>
     */
    @Override
    SctpChannelConfig config();

    /**
     * Return the (primary) remote address of the SCTP channel.
     *
     * Please note that, this return the first remote address in the underlying SCTP Channel's
     * remote address iterator to support Netty Channel API. In other words, its the application's
     * responsibility to keep track of it's peer's primary address.
     *
     * (The application can request it's remote peer to set a specific address as primary by
     * calling the local SCTP stack with SctpStandardSocketOption.SCTP_SET_PEER_PRIMARY_ADDR option)
     * <p>返回对端主地址；可通过 {@code SCTP_SET_PEER_PRIMARY_ADDR} 请求对端切换主路径。</p>
     */
    @Override
    InetSocketAddress remoteAddress();

    /**
     * Return all remote addresses of the SCTP server channel.
     * Please note that, it will return more than one address if the remote is using multi-homing.
     * <p>返回对端全部传输地址（对端 multi-homing 时不只一个）。</p>
     */
    Set<InetSocketAddress> allRemoteAddresses();

    /**
     * Bind a address to the already bound channel to enable multi-homing.
     * The Channel bust be bound and yet to be connected.
     * <p>向已 bind 且尚未 connect 的通道追加本地地址以启用 multi-homing。</p>
     */
    ChannelFuture bindAddress(InetAddress localAddress);

    /**
     * Bind a address to the already bound channel to enable multi-homing.
     * The Channel bust be bound and yet to be connected.
     *
     * Will notify the given {@link ChannelPromise} and return a {@link ChannelFuture}
     * <p>同上，异步完成时通知 {@link ChannelPromise}。</p>
     */
    ChannelFuture bindAddress(InetAddress localAddress, ChannelPromise promise);

    /**
     *  Unbind the address from channel's multi-homing address list.
     *  The address should be added already in multi-homing address list.
     * <p>从 multi-homing 列表移除指定本地地址（须已 bindAddress 过）。</p>
     */
    ChannelFuture unbindAddress(InetAddress localAddress);

    /**
     *  Unbind the address from channel's multi-homing address list.
     *  The address should be added already in multi-homing address list.
     *
     * Will notify the given {@link ChannelPromise} and return a {@link ChannelFuture}
     * <p>异步 unbind，完成时通知 promise。</p>
     */
    ChannelFuture unbindAddress(InetAddress localAddress, ChannelPromise promise);
}
