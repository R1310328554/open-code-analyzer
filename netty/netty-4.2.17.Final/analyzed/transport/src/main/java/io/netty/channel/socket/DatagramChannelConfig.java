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

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelOption;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;

/**
 * A {@link ChannelConfig} for a {@link DatagramChannel}.
 * <p>{@link DatagramChannel} 的 {@link ChannelConfig}，扩展组播、广播等 UDP 专用 socket 选项。</p>
 *
 * <h3>Available options</h3>
 *
 * In addition to the options provided by {@link ChannelConfig},
 * {@link DatagramChannelConfig} allows the following options in the option map:
 *
 * <table border="1" cellspacing="0" cellpadding="6">
 * <tr>
 * <th>Name</th><th>Associated setter method</th>
 * </tr><tr>
 * <td>{@link ChannelOption#SO_BROADCAST}</td><td>{@link #setBroadcast(boolean)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#IP_MULTICAST_ADDR}</td><td>{@link #setInterface(InetAddress)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#IP_MULTICAST_LOOP_DISABLED}</td>
 * <td>{@link #setLoopbackModeDisabled(boolean)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#IP_MULTICAST_IF}</td>
 * <td>{@link #setNetworkInterface(NetworkInterface)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#SO_REUSEADDR}</td><td>{@link #setReuseAddress(boolean)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#SO_RCVBUF}</td><td>{@link #setReceiveBufferSize(int)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#SO_SNDBUF}</td><td>{@link #setSendBufferSize(int)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#IP_MULTICAST_TTL}</td><td>{@link #setTimeToLive(int)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#IP_TOS}</td><td>{@link #setTrafficClass(int)}</td>
 * </tr>
 * </table>
 */
public interface DatagramChannelConfig extends ChannelConfig {

    /**
     * Gets the {@link StandardSocketOptions#SO_SNDBUF} option.
     * <p>获取发送缓冲区大小（{@link StandardSocketOptions#SO_SNDBUF}）。</p>
     */
    int getSendBufferSize();

    /**
     * Sets the {@link StandardSocketOptions#SO_SNDBUF} option.
     * <p>设置发送缓冲区大小。</p>
     */
    DatagramChannelConfig setSendBufferSize(int sendBufferSize);

    /**
     * Gets the {@link StandardSocketOptions#SO_RCVBUF} option.
     * <p>获取接收缓冲区大小（{@link StandardSocketOptions#SO_RCVBUF}）。</p>
     */
    int getReceiveBufferSize();

    /**
     * Sets the {@link StandardSocketOptions#SO_RCVBUF} option.
     * <p>设置接收缓冲区大小。</p>
     */
    DatagramChannelConfig setReceiveBufferSize(int receiveBufferSize);

    /**
     * Gets the {@link StandardSocketOptions#IP_TOS} option.
     * <p>获取 IP 服务类型 / 流量类别（{@link StandardSocketOptions#IP_TOS}）。</p>
     */
    int getTrafficClass();

    /**
     * Sets the {@link StandardSocketOptions#IP_TOS} option.
     * <p>设置 IP 服务类型 / 流量类别。</p>
     */
    DatagramChannelConfig setTrafficClass(int trafficClass);

    /**
     * Gets the {@link StandardSocketOptions#SO_REUSEADDR} option.
     * <p>是否启用地址复用（{@link StandardSocketOptions#SO_REUSEADDR}）。</p>
     */
    boolean isReuseAddress();

    /**
     * Gets the {@link StandardSocketOptions#SO_REUSEADDR} option.
     * <p>设置是否允许地址复用。</p>
     */
    DatagramChannelConfig setReuseAddress(boolean reuseAddress);

    /**
     * Gets the {@link StandardSocketOptions#SO_BROADCAST} option.
     * <p>是否允许发送广播 datagram。</p>
     */
    boolean isBroadcast();

    /**
     * Sets the {@link StandardSocketOptions#SO_BROADCAST} option.
     * <p>设置是否允许广播。</p>
     */
    DatagramChannelConfig setBroadcast(boolean broadcast);

    /**
     * Gets the {@link StandardSocketOptions#IP_MULTICAST_LOOP} option.
     *
     * @return {@code true} if and only if the loopback mode has been disabled
     * <p>组播回环是否已禁用；{@code true} 表示已禁用本地回环接收。</p>
     */
    boolean isLoopbackModeDisabled();

    /**
     * Sets the {@link StandardSocketOptions#IP_MULTICAST_LOOP} option.
     *
     * @param loopbackModeDisabled
     *        {@code true} if and only if the loopback mode has been disabled
     * <p>设置组播回环模式；{@code true} 禁用回环。</p>
     */
    DatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled);

    /**
     * Gets the {@link StandardSocketOptions#IP_MULTICAST_TTL} option.
     * <p>获取组播 TTL（跳数限制）。</p>
     */
    int getTimeToLive();

    /**
     * Sets the {@link StandardSocketOptions#IP_MULTICAST_TTL} option.
     * <p>设置组播 TTL。</p>
     */
    DatagramChannelConfig setTimeToLive(int ttl);

    /**
     * Gets the address of the network interface used for multicast packets.
     * <p>获取发送组播包时使用的网络接口地址。</p>
     */
    InetAddress getInterface();

    /**
     * Sets the address of the network interface used for multicast packets.
     * <p>设置组播出口接口地址。</p>
     */
    DatagramChannelConfig setInterface(InetAddress interfaceAddress);

    /**
     * Gets the {@link StandardSocketOptions#IP_MULTICAST_IF} option.
     * <p>获取组播网络接口（{@link StandardSocketOptions#IP_MULTICAST_IF}）。</p>
     */
    NetworkInterface getNetworkInterface();

    /**
     * Sets the {@link StandardSocketOptions#IP_MULTICAST_IF} option.
     * <p>设置组播绑定的网络接口。</p>
     */
    DatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface);

    @Override
    @Deprecated
    DatagramChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    DatagramChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    DatagramChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    DatagramChannelConfig setAllocator(ByteBufAllocator allocator);

    @Override
    DatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    @Override
    DatagramChannelConfig setAutoRead(boolean autoRead);

    @Override
    DatagramChannelConfig setAutoClose(boolean autoClose);

    @Override
    DatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);

    @Override
    DatagramChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark);

}
