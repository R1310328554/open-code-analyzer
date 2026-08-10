/*
 * Copyright 2014 The Netty Project
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

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketProtocolFamily;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;

import static io.netty.channel.epoll.LinuxSocket.newSocketStream;
import static io.netty.channel.epoll.Native.IS_SUPPORTING_TCP_FASTOPEN_CLIENT;

/**
 * {@link SocketChannel} implementation that uses linux EPOLL.
 * <p>基于 Linux epoll 的 TCP 客户端 {@link SocketChannel}，支持 TFO 连接与 TCP_INFO。</p>
 */
public final class EpollSocketChannel extends AbstractEpollStreamChannel implements SocketChannel {

    private final EpollSocketChannelConfig config;

    /** 继承自父 Server 或本地配置的 TCP_MD5SIG 地址 */
    private volatile Collection<InetAddress> tcpMd5SigAddresses = Collections.emptyList();

    public EpollSocketChannel() {
        super(newSocketStream(), false);
        config = new EpollSocketChannelConfig(this);
    }

    /**
     *
     * @deprecated use {@link EpollServerSocketChannel#EpollServerSocketChannel(SocketProtocolFamily)}.
      * <p>Netty epoll 传输实现；详见上方英文说明。</p>
     */
    @Deprecated
    public EpollSocketChannel(InternetProtocolFamily protocol) {
        super(newSocketStream(protocol), false);
        config = new EpollSocketChannelConfig(this);
    }

    public EpollSocketChannel(SocketProtocolFamily protocol) {
        super(newSocketStream(protocol), false);
        config = new EpollSocketChannelConfig(this);
    }

    public EpollSocketChannel(int fd) {
        super(fd);
        config = new EpollSocketChannelConfig(this);
    }

    EpollSocketChannel(LinuxSocket fd, boolean active) {
        super(fd, active);
        config = new EpollSocketChannelConfig(this);
    }

    EpollSocketChannel(Channel parent, LinuxSocket fd, InetSocketAddress remoteAddress) {
        super(parent, fd, remoteAddress);
        config = new EpollSocketChannelConfig(this);

        if (parent instanceof EpollServerSocketChannel) {
            tcpMd5SigAddresses = ((EpollServerSocketChannel) parent).tcpMd5SigAddresses();
        }
    }

    /**
     * Returns the {@code TCP_INFO} for the current socket.
     * See <a href="https://linux.die.net//man/7/tcp">man 7 tcp</a>.
     * <p>读取当前套接字的 Linux {@code TCP_INFO} 统计。</p>
     */
    public EpollTcpInfo tcpInfo() {
        return tcpInfo(new EpollTcpInfo());
    }

    /**
     * Updates and returns the {@code TCP_INFO} for the current socket.
     * See <a href="https://linux.die.net//man/7/tcp">man 7 tcp</a>.
     * <p>填充并返回传入的 {@link EpollTcpInfo} 对象。</p>
     */
    public EpollTcpInfo tcpInfo(EpollTcpInfo info) {
        try {
            socket.getTcpInfo(info);
            return info;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
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
    public EpollSocketChannelConfig config() {
        return config;
    }

    @Override
    public ServerSocketChannel parent() {
        return (ServerSocketChannel) super.parent();
    }

    @Override
    protected AbstractEpollUnsafe newUnsafe() {
        return new EpollSocketChannelUnsafe();
    }

    @Override
    boolean doConnect0(SocketAddress remote) throws Exception {
        if (IS_SUPPORTING_TCP_FASTOPEN_CLIENT && config.isTcpFastOpenConnect()) {
            ChannelOutboundBuffer outbound = unsafe().outboundBuffer();
            outbound.addFlush();
            Object curr;
            if ((curr = outbound.current()) instanceof ByteBuf) {
                ByteBuf initialData = (ByteBuf) curr;
                // 无 TFO cookie 时写失败为 EINPROGRESS，退化为普通异步 connect
                long localFlushedAmount = doWriteOrSendBytes(
                        initialData, (InetSocketAddress) remote, true);
                if (localFlushedAmount > 0) {
                    // TFO 成功：移除已发送的 SYN 数据，后续走正常 TCP
                    outbound.removeBytes(localFlushedAmount);
                    return true;
                }
            }
        }
        return super.doConnect0(remote);
    }

    private final class EpollSocketChannelUnsafe extends EpollStreamUnsafe {
        @Override
        protected Executor prepareToClose() {
            try {
                // 先检查 isOpen，避免 fd 已关闭时 getSoLinger 抛异常
                if (isOpen() && config().getSoLinger() > 0) {
                    // SO_LINGER>0 时取消 epoll 注册，避免 close 延迟导致 event loop 空转
                    // See https://github.com/netty/netty/issues/4449
                    registration().cancel();
                    return GlobalEventExecutor.INSTANCE;
                }
            } catch (Throwable ignore) {
                // Ignore the error as the underlying channel may be closed in the meantime and so
                // getSoLinger() may produce an exception. In this case we just return null.
                // See https://github.com/netty/netty/issues/4449
            }
            return null;
        }
    }

    void setTcpMd5Sig(Map<InetAddress, byte[]> keys) throws IOException {
        // 更新 MD5 密钥需同步，因底层可能多次 socket 操作
        synchronized (this) {
            tcpMd5SigAddresses = TcpMd5Util.newTcpMd5Sigs(this, tcpMd5SigAddresses, keys);
        }
    }
}
