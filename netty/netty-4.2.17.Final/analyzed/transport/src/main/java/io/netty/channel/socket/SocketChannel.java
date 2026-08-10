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

import java.net.InetSocketAddress;

/**
 * A TCP/IP socket {@link Channel}.
 * <p>基于 TCP/IP 的双向 socket {@link Channel}，支持半关闭等全双工特性。</p>
 */
public interface SocketChannel extends DuplexChannel {
    /** 返回接受此连接的父 {@link ServerSocketChannel} */
    @Override
    ServerSocketChannel parent();

    /** 返回此 socket channel 的配置 */
    @Override
    SocketChannelConfig config();
    /** 返回本地 socket 地址 */
    @Override
    InetSocketAddress localAddress();
    /** 返回远端 peer 的 socket 地址 */
    @Override
    InetSocketAddress remoteAddress();
}
