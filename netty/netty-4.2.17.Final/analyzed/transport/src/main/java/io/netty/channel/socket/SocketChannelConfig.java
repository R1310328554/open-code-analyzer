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

import java.net.Socket;
import java.net.StandardSocketOptions;

/**
 * A {@link ChannelConfig} for a {@link SocketChannel}.
 * <p>{@link SocketChannel} 的配置，在 {@link DuplexChannelConfig} 基础上提供 TCP 常用 socket 选项。</p>
 *
 * <h3>Available options</h3>
 *
 * In addition to the options provided by {@link DuplexChannelConfig},
 * {@link SocketChannelConfig} allows the following options in the option map:
 *
 * <table border="1" cellspacing="0" cellpadding="6">
 * <tr>
 * <th>Name</th><th>Associated setter method</th>
 * </tr><tr>
 * <td>{@link ChannelOption#SO_KEEPALIVE}</td><td>{@link #setKeepAlive(boolean)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#SO_REUSEADDR}</td><td>{@link #setReuseAddress(boolean)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#SO_LINGER}</td><td>{@link #setSoLinger(int)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#TCP_NODELAY}</td><td>{@link #setTcpNoDelay(boolean)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#SO_RCVBUF}</td><td>{@link #setReceiveBufferSize(int)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#SO_SNDBUF}</td><td>{@link #setSendBufferSize(int)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#IP_TOS}</td><td>{@link #setTrafficClass(int)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#ALLOW_HALF_CLOSURE}</td><td>{@link #setAllowHalfClosure(boolean)}</td>
 * </tr>
 * </table>
 */
public interface SocketChannelConfig extends DuplexChannelConfig {

    /**
     * Gets the {@link StandardSocketOptions#TCP_NODELAY} option.  Please note that the default value of this option
     * is {@code true} unlike the operating system default ({@code false}). However, for some buggy platforms, such as
     * Android, that shows erratic behavior with Nagle's algorithm disabled, the default value remains to be
     * {@code false}.
     * <p>是否禁用 Nagle 算法（TCP_NODELAY）；Netty 默认多为 {@code true}，部分平台仍为 {@code false}。</p>
     */
    boolean isTcpNoDelay();

    /**
     * Sets the {@link StandardSocketOptions#TCP_NODELAY} option.  Please note that the default value of this option
     * is {@code true} unlike the operating system default ({@code false}). However, for some buggy platforms, such as
     * Android, that shows erratic behavior with Nagle's algorithm disabled, the default value remains to be
     * {@code false}.
     * <p>设置 TCP_NODELAY。</p>
     */
    SocketChannelConfig setTcpNoDelay(boolean tcpNoDelay);

    /**
     * Gets the {@link StandardSocketOptions#SO_LINGER} option.
     * <p>获取 SO_LINGER 秒数（{@code -1} 表示禁用）。</p>
     */
    int getSoLinger();

    /**
     * Sets the {@link StandardSocketOptions#SO_LINGER} option.
     * <p>设置 SO_LINGER。</p>
     */
    SocketChannelConfig setSoLinger(int soLinger);

    /**
     * Gets the {@link StandardSocketOptions#SO_SNDBUF} option.
     * <p>获取发送缓冲区大小。</p>
     */
    int getSendBufferSize();

    /**
     * Sets the {@link StandardSocketOptions#SO_SNDBUF} option.
     * <p>设置发送缓冲区大小。</p>
     */
    SocketChannelConfig setSendBufferSize(int sendBufferSize);

    /**
     * Gets the {@link StandardSocketOptions#SO_RCVBUF} option.
     * <p>获取接收缓冲区大小。</p>
     */
    int getReceiveBufferSize();

    /**
     * Sets the {@link StandardSocketOptions#SO_RCVBUF} option.
     * <p>设置接收缓冲区大小。</p>
     */
    SocketChannelConfig setReceiveBufferSize(int receiveBufferSize);

    /**
     * Gets the {@link StandardSocketOptions#SO_KEEPALIVE} option.
     * <p>是否启用 TCP keepalive。</p>
     */
    boolean isKeepAlive();

    /**
     * Sets the {@link StandardSocketOptions#SO_KEEPALIVE} option.
     * <p>设置 keepalive。</p>
     */
    SocketChannelConfig setKeepAlive(boolean keepAlive);

    /**
     * Gets the {@link StandardSocketOptions#IP_TOS} option.
     * <p>获取 IP 流量类别。</p>
     */
    int getTrafficClass();

    /**
     * Sets the {@link StandardSocketOptions#IP_TOS} option.
     * <p>设置 IP 流量类别。</p>
     */
    SocketChannelConfig setTrafficClass(int trafficClass);

    /**
     * Gets the {@link StandardSocketOptions#SO_REUSEADDR} option.
     * <p>是否启用地址复用。</p>
     */
    boolean isReuseAddress();

    /**
     * Sets the {@link StandardSocketOptions#SO_REUSEADDR} option.
     * <p>设置地址复用。</p>
     */
    SocketChannelConfig setReuseAddress(boolean reuseAddress);

    /**
     * Sets the performance preferences as specified in
     * {@link Socket#setPerformancePreferences(int, int, int)}.
     * <p>设置 socket 性能偏好。</p>
     */
    SocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth);

    @Override
    SocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure);

    @Override
    SocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    @Deprecated
    SocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    SocketChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    SocketChannelConfig setAllocator(ByteBufAllocator allocator);

    @Override
    SocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    @Override
    SocketChannelConfig setAutoRead(boolean autoRead);

    @Override
    SocketChannelConfig setAutoClose(boolean autoClose);

    @Override
    SocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);

    @Override
    SocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark);
}
