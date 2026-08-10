/*
 * Copyright 2015 The Netty Project
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
package io.netty.channel.epoll;

import io.netty.channel.Channel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.ServerDomainSocketChannel;
import io.netty.channel.unix.Socket;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.File;
import java.net.SocketAddress;

import static io.netty.channel.epoll.LinuxSocket.newSocketDomain;

/** epoll UNIX 域套接字服务端通道：bind 路径、listen、accept 子连接 */
public final class EpollServerDomainSocketChannel extends AbstractEpollServerChannel
        implements ServerDomainSocketChannel {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(
            EpollServerDomainSocketChannel.class);

    /** 服务端通道配置 */
    private final EpollServerChannelConfig config = new EpollServerChannelConfig(this);
    /** 绑定后的本地域套接字地址 */
    private volatile DomainSocketAddress local;

    public EpollServerDomainSocketChannel() {
        super(newSocketDomain(), false);
    }

    public EpollServerDomainSocketChannel(int fd) {
        super(fd);
    }

    public EpollServerDomainSocketChannel(int fd, boolean active) {
        super(new LinuxSocket(fd), active);
    }

    EpollServerDomainSocketChannel(LinuxSocket fd) {
        super(fd);
    }

    EpollServerDomainSocketChannel(LinuxSocket fd, boolean active) {
        super(fd, active);
    }

    @Override
    protected Channel newChildChannel(int fd, byte[] addr, int offset, int len) throws Exception {
        return new EpollDomainSocketChannel(this, new Socket(fd));
    }

    @Override
    protected DomainSocketAddress localAddress0() {
        return local;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        socket.bind(localAddress);
        socket.listen(config.getBacklog());
        local = (DomainSocketAddress) localAddress;
        active = true;
        // listen 完成后提交 epoll 监听以接收新连接
        submitCurrentOps();
    }

    @Override
    protected void doClose() throws Exception {
        try {
            super.doClose();
        } finally {
            DomainSocketAddress local = this.local;
            if (local != null) {
                // 关闭后尽量删除域套接字文件
                File socketFile = new File(local.path());
                boolean success = socketFile.delete();
                if (!success && logger.isDebugEnabled()) {
                    logger.debug("Failed to delete a domain socket file: {}", local.path());
                }
            }
        }
    }

    @Override
    public EpollServerChannelConfig config() {
        return config;
    }

    @Override
    public DomainSocketAddress remoteAddress() {
        return (DomainSocketAddress) super.remoteAddress();
    }

    @Override
    public DomainSocketAddress localAddress() {
        return (DomainSocketAddress) super.localAddress();
    }
}
