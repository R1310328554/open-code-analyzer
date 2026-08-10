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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ServerChannel;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.util.Set;

/**
 * A SCTP/IP {@link ServerChannel} which accepts incoming SCTP/IP associations.
 * <p>接受入站 SCTP 关联并产出 {@link SctpChannel}； 支持 multi-homing 的 bindAddress/unbindAddress。</p>
 *
 * <p>
 * Multi-homing address binding/unbinding can done through bindAddress/unbindAddress methods.
 * </p>
 */
public interface SctpServerChannel extends ServerChannel {

    /**
     * Returns the {@link SctpServerChannelConfig} configuration of the channel.
     * <p>返回监听通道配置（backlog、缓冲、INIT 流等）。</p>
     */
    @Override
    SctpServerChannelConfig config();

    /**
     * Return the (primary) local address of the SCTP server channel.
     *
     * Please note that, this return the first local address in the underlying SCTP ServerChannel's
     * local address iterator to support Netty Channel API. In other words, its the application's
     * responsibility to keep track of it's local primary address.
     *
     * (To set a local address as primary, the application can request by calling local SCTP stack,
     * with SctpStandardSocketOption.SCTP_PRIMARY_ADDR option).
     * <p>主本地地址（迭代器首个）；多宿主时应用需自行维护主地址。</p>
     */
    @Override
    InetSocketAddress localAddress();

    /**
     * Return all local addresses of the SCTP server channel.
     * Please note that, it will return more than one address if this channel is using multi-homing
     * <p>全部本地监听地址集合。</p>
     */
    Set<InetSocketAddress> allLocalAddresses();

    /**
     * Bind a address to the already bound channel to enable multi-homing.
     * The Channel must be bound and yet to be connected.
     * <p>向已 bind 的监听通道追加本地 IP（multi-homing）。</p>
     */
    ChannelFuture bindAddress(InetAddress localAddress);

    /**
     * Bind a address to the already bound channel to enable multi-homing.
     * The Channel must be bound and yet to be connected.
     *
     * Will notify the given {@link ChannelPromise} and return a {@link ChannelFuture}
     * <p>异步 bindAddress，完成时通知 promise。</p>
     */
    ChannelFuture bindAddress(InetAddress localAddress, ChannelPromise promise);

    /**
     *  Unbind the address from channel's multi-homing address list.
     *  The address should be added already in multi-homing address list.
     * <p>从 multi-homing 列表移除本地地址。</p>
     */
    ChannelFuture unbindAddress(InetAddress localAddress);

    /**
     *  Unbind the address from channel's multi-homing address list.
     *  The address should be added already in multi-homing address list.
     *
     * Will notify the given {@link ChannelPromise} and return a {@link ChannelFuture}
     * <p>异步 unbindAddress。</p>
     */
    ChannelFuture unbindAddress(InetAddress localAddress, ChannelPromise promise);
}
