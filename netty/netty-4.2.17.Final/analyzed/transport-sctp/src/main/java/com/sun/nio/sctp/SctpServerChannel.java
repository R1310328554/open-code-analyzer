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
package com.sun.nio.sctp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

/**
 * SCTP 服务端监听通道，接受入站关联并返回 {@link SctpChannel}。
 * <p>支持多地址 bind 与 {@link #bindAddress}/{@link #unbindAddress} 动态地址管理； stub 实现在非 SCTP 平台不可用。</p>
 */
@SuppressWarnings("all")
public abstract class SctpServerChannel extends AbstractSelectableChannel {
    /** SCTP 平台探测 */
    static {
        UnsupportedOperatingSystemException.raise();
    }

    /** 打开 SCTP 服务端通道 */
    public static SctpServerChannel open() throws IOException {
        return null;
    }

    /** 子类构造 */
    protected SctpServerChannel(SelectorProvider provider) {
        super(provider);
    }

    /** 读取监听套接字选项 */
    public abstract <T> T getOption(SctpSocketOption<T> name) throws IOException;
    /** 设置监听套接字选项 */
    public abstract <T> SctpServerChannel setOption(SctpSocketOption<T> name, T value) throws IOException;

    /** 返回监听的全部本地地址 */
    public abstract Set<SocketAddress> getAllLocalAddresses() throws IOException;

    /** 绑定本地地址并开始监听（系统默认 backlog） */
    public abstract SctpServerChannel bind(SocketAddress local) throws IOException;
    /** 绑定并指定 accept 队列 backlog */
    public abstract SctpServerChannel bind(SocketAddress local, int backlog) throws IOException;

    /** 向监听套接字追加本地 IP */
    public abstract SctpServerChannel bindAddress(InetAddress inetAddress) throws IOException;
    /** 从监听套接字移除本地 IP */
    public abstract SctpServerChannel unbindAddress(InetAddress inetAddress) throws IOException;

    /** 接受新的 SCTP 关联，返回已连接的 {@link SctpChannel} */
    public abstract SctpChannel accept() throws IOException;
}
