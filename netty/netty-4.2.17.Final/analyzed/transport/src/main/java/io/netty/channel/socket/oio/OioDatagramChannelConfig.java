/*
 * Copyright 2017 The Netty Project
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
package io.netty.channel.socket.oio;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.DatagramChannelConfig;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * <p>OIO 数据报通道配置：在 {@link DatagramChannelConfig} 基础上增加阻塞读超时 {@link #setSoTimeout}。</p>
 * @deprecated use NIO / EPOLL / KQUEUE transport.
 */
@Deprecated
public interface OioDatagramChannelConfig extends DatagramChannelConfig {
    /**
     * Sets the maximal time a operation on the underlying socket may block.
     * <p>设置底层 socket 单次阻塞 I/O 的最大等待时间（毫秒）。</p>
     */
    OioDatagramChannelConfig setSoTimeout(int timeout);

    /**
     * Returns the maximal time a operation on the underlying socket may block.
     * <p>返回底层 socket 阻塞 I/O 超时（毫秒）。</p>
     */
    int getSoTimeout();

    @Override
    OioDatagramChannelConfig setSendBufferSize(int sendBufferSize);

    @Override
    OioDatagramChannelConfig setReceiveBufferSize(int receiveBufferSize);

    @Override
    OioDatagramChannelConfig setTrafficClass(int trafficClass);

    @Override
    OioDatagramChannelConfig setReuseAddress(boolean reuseAddress);

    @Override
    OioDatagramChannelConfig setBroadcast(boolean broadcast);

    @Override
    OioDatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled);

    @Override
    OioDatagramChannelConfig setTimeToLive(int ttl);

    @Override
    OioDatagramChannelConfig setInterface(InetAddress interfaceAddress);

    @Override
    OioDatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface);

    @Override
    OioDatagramChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    OioDatagramChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    OioDatagramChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    OioDatagramChannelConfig setAllocator(ByteBufAllocator allocator);

    @Override
    OioDatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    @Override
    OioDatagramChannelConfig setAutoRead(boolean autoRead);

    @Override
    OioDatagramChannelConfig setAutoClose(boolean autoClose);

    @Override
    OioDatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);

    @Override
    OioDatagramChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark);

    @Override
    OioDatagramChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    @Override
    OioDatagramChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);
}
