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
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

/**
 * SCTP 客户端/已连接通道，对应 JDK {@code com.sun.nio.sctp.SctpChannel} API。
 * <p>支持多流收发、多地址 bind/connect 及 {@link NotificationHandler} 通知； 本文件为 Netty 在非 SCTP 平台的编译占位 stub。</p>
 */
@SuppressWarnings("all")
public abstract class SctpChannel extends AbstractSelectableChannel {
    /** 加载时校验 SCTP 支持 */
    static {
        UnsupportedOperatingSystemException.raise();
    }

    /** 打开新的 SCTP 通道（stub 返回 {@code null}） */
    public static SctpChannel open() throws IOException {
        return null;
    }
    
    /** 子类构造：绑定 {@link SelectorProvider} */
    protected SctpChannel(SelectorProvider provider) {
        super(provider);
    }

    /** 读取 SCTP/套接字选项 */
    public abstract <T> T getOption(SctpSocketOption<T> name) throws IOException;
    /** 设置通道选项 */
    public abstract <T> SctpChannel setOption(SctpSocketOption<T> name, T value) throws IOException;

    /** 返回本地绑定的全部传输地址（多宿主） */
    public abstract Set<SocketAddress> getAllLocalAddresses() throws IOException;
    /** 返回对端全部传输地址 */
    public abstract Set<SocketAddress> getRemoteAddresses() throws IOException;

    /** 返回当前 SCTP 关联对象 */
    public abstract Association association() throws IOException;
    /** 绑定本地地址 */
    public abstract SctpChannel bind(SocketAddress local) throws IOException;
    /** 发起关联（可能非阻塞，需 {@link #finishConnect}） */
    public abstract boolean connect(SocketAddress remote) throws IOException;
    /** 完成非阻塞 connect */
    public abstract boolean finishConnect() throws IOException;

    /** 向已有关联追加本地 IP 地址 */
    public abstract SctpChannel bindAddress(InetAddress inetAddress) throws IOException;
    /** 从关联移除本地 IP 地址 */
    public abstract SctpChannel unbindAddress(InetAddress inetAddress) throws IOException;

    /** 接收数据并同步分发 SCTP 通知给 handler */
    public abstract <T> MessageInfo receive(ByteBuffer dst, T attachment, NotificationHandler<T> handler) throws IOException;
    /** 按 {@link MessageInfo} 元数据发送缓冲区数据 */
    public abstract int send(ByteBuffer src, MessageInfo messageInfo) throws IOException;
    
    /** 返回本通道支持的 {@link SctpSocketOption} 集合 */
    public abstract Set<SctpSocketOption<?>> supportedOptions();
}
