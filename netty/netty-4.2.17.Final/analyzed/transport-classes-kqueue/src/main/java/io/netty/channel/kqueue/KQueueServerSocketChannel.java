/*
 * Copyright 2016 The Netty Project
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
package io.netty.channel.kqueue;

import io.netty.channel.Channel;
import io.netty.channel.socket.ServerSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static io.netty.channel.kqueue.BsdSocket.newSocketStream;
import static io.netty.channel.unix.NativeInetAddress.address;

/**
 * 基于 KQueue 的 TCP {@link ServerSocketChannel}（IPv4/IPv6）。
 * <p>bind 时设置 backlog；可选启用 TCP FastOpen； accept 创建 {@link KQueueSocketChannel}。</p>
 */
public final class KQueueServerSocketChannel extends AbstractKQueueServerChannel implements ServerSocketChannel {
    /** 本通道专用配置（SO_REUSEPORT、accept 过滤器等） */
    private final KQueueServerSocketChannelConfig config;

    /** 创建未绑定的 TCP 服务端通道 */
    public KQueueServerSocketChannel() {
        super(newSocketStream(), false);
        config = new KQueueServerSocketChannelConfig(this);
    }

    /** 由已有监听 fd 包装（通常来自 accept 或外部传入） */
    public KQueueServerSocketChannel(int fd) {
        // 须通过 BsdSocket 构造以正确解析本地地址
        this(new BsdSocket(fd));
    }

    KQueueServerSocketChannel(BsdSocket fd) {
        super(fd);
        config = new KQueueServerSocketChannelConfig(this);
    }

    KQueueServerSocketChannel(BsdSocket fd, boolean active) {
        super(fd, active);
        config = new KQueueServerSocketChannelConfig(this);
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        super.doBind(localAddress);
        socket.listen(config.getBacklog());
        // bind 完成后若配置启用则打开服务端 TCP FastOpen
            socket.setTcpFastOpen(true);
        }
        active = true;
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) super.remoteAddress();
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) super.localAddress();
    }

    @Override
    public KQueueServerSocketChannelConfig config() {
        return config;
    }

    @Override
    protected Channel newChildChannel(int fd, byte[] address, int offset, int len) throws Exception {
        // accept 到的子 fd 包装为 KQueueSocketChannel 并解析远端地址
    }
}
