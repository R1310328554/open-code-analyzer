/*
 * Copyright 2022 The Netty Project
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
package io.netty.handler.ssl.ocsp;

import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * {@link IoTransport} 封装用于 DNS 与 OCSP 查询的 {@link EventLoop}、
 * {@link SocketChannel} 与 {@link DatagramChannel}。
 */
public final class IoTransport {
    /** 执行 I/O 的事件循环 */
    private final EventLoop eventLoop;
    /** TCP 通道工厂（DNS TCP 查询与 OCSP HTTP） */
    private final ChannelFactory<SocketChannel> socketChannel;
    /** UDP 通道工厂（DNS UDP 查询） */
    private final ChannelFactory<DatagramChannel> datagramChannel;

    /**
     * 默认 {@link IoTransport}：使用 {@link NioIoHandler}、{@link NioSocketChannel}
     * 与 {@link NioDatagramChannel}。
     */
    public static final IoTransport DEFAULT = new IoTransport(
            new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory()).next(),
            new ChannelFactory<SocketChannel>() {
                @Override
                public SocketChannel newChannel() {
                    return new NioSocketChannel();
                }
            },
            new ChannelFactory<DatagramChannel>() {
                @Override
                public DatagramChannel newChannel() {
                    return new NioDatagramChannel();
                }
            });

    /**
     * 创建新的 {@link IoTransport} 实例。
     *
     * @param eventLoop       用于 I/O 的 {@link EventLoop}
     * @param socketChannel   用于 TCP DNS 查询与 OCSP 请求的 {@link SocketChannel} 工厂
     * @param datagramChannel 用于 UDP DNS 查询的 {@link DatagramChannel} 工厂
     * @return 任一参数为 {@code null} 时抛出 {@link NullPointerException}
     */
    public static IoTransport create(EventLoop eventLoop, ChannelFactory<SocketChannel> socketChannel,
                                     ChannelFactory<DatagramChannel> datagramChannel) {
        return new IoTransport(eventLoop, socketChannel, datagramChannel);
    }

    private IoTransport(EventLoop eventLoop, ChannelFactory<SocketChannel> socketChannel,
                        ChannelFactory<DatagramChannel> datagramChannel) {
        this.eventLoop = checkNotNull(eventLoop, "EventLoop");
        this.socketChannel = checkNotNull(socketChannel, "SocketChannel");
        this.datagramChannel = checkNotNull(datagramChannel, "DatagramChannel");
    }

    /** 返回关联的事件循环 */
    public EventLoop eventLoop() {
        return eventLoop;
    }

    /** 返回 TCP 套接字通道工厂 */
    public ChannelFactory<SocketChannel> socketChannel() {
        return socketChannel;
    }

    /** 返回 UDP 数据报通道工厂 */
    public ChannelFactory<DatagramChannel> datagramChannel() {
        return datagramChannel;
    }
}
