/*
 * Copyright 2012 The Netty Project
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
package io.netty.channel.udt.nio;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.SocketChannelUDT;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.FileRegion;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.nio.AbstractNioByteChannel;
import io.netty.channel.nio.NioIoOps;
import io.netty.channel.udt.DefaultUdtChannelConfig;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.UdtChannelConfig;
import io.netty.util.internal.SocketUtils;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Byte Channel Connector for UDT Streams.
 * <p>UDT 字节流（{@link TypeUDT#STREAM}）Connector：基于 {@link AbstractNioByteChannel} 与 {@link SocketChannelUDT}， 支持非阻塞 connect/read/write；由 Acceptor accept 或主动 connect 创建。</p>
 *
 * @deprecated The UDT transport is no longer maintained and will be removed.
 */
@Deprecated
public class NioUdtByteConnectorChannel extends AbstractNioByteChannel implements UdtChannel {

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(NioUdtByteConnectorChannel.class);

    private final UdtChannelConfig config;

    /** 创建默认 STREAM 类型的 UDT 字节 Connector 通道 */
    public NioUdtByteConnectorChannel() {
        this(TypeUDT.STREAM);
    }

    /**
     * 包装已有 UDT 套接字通道（如 Acceptor accept 所得）。
     * <p>设为非阻塞；INIT/OPENED 状态时立即 apply 配置。</p>
     */
    public NioUdtByteConnectorChannel(final Channel parent, final SocketChannelUDT channelUDT) {
        super(parent, channelUDT);
        try {
            channelUDT.configureBlocking(false);
            switch (channelUDT.socketUDT().status()) {
            case INIT:
            case OPENED:
                config = new DefaultUdtChannelConfig(this, channelUDT, true);
                break;
            default:
                config = new DefaultUdtChannelConfig(this, channelUDT, false);
                break;
            }
        } catch (final Exception e) {
            try {
                channelUDT.close();
            } catch (final Exception e2) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to close channel.", e2);
                }
            }
            throw new ChannelException("Failed to configure channel.", e);
        }
    }

    /** 无 parent 包装已有 {@link SocketChannelUDT} */
    public NioUdtByteConnectorChannel(final SocketChannelUDT channelUDT) {
        this(null, channelUDT);
    }

    /** 按 {@link TypeUDT} 打开新 UDT 套接字并构造 Connector */
    public NioUdtByteConnectorChannel(final TypeUDT type) {
        this(NioUdtProvider.newConnectorChannelUDT(type));
    }

    @Override
    public UdtChannelConfig config() {
        return config;
    }

    @Override
    /** 绑定本地地址（通过特权 {@link AccessController} 调用底层 bind） */
    protected void doBind(final SocketAddress localAddress) throws Exception {
        privilegedBind(javaChannel(), localAddress);
    }

    @Override
    /** 关闭底层 {@link SocketChannelUDT} */
    protected void doClose() throws Exception {
        javaChannel().close();
    }

    @Override
    /** 可选 bind 本地地址后发起 connect；未完成时注册 {@code OP_CONNECT} */
    protected boolean doConnect(final SocketAddress remoteAddress,
                                final SocketAddress localAddress) throws Exception {
        doBind(localAddress != null? localAddress : new InetSocketAddress(0));
        boolean success = false;
        try {
            final boolean connected = SocketUtils.connect(javaChannel(), remoteAddress);
            if (!connected) {
                addAndSubmit(NioIoOps.CONNECT);
            }
            success = true;
            return connected;
        } finally {
            if (!success) {
                doClose();
            }
        }
    }

    @Override
    /** UDT Connector 断开即关闭通道 */
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    /** 完成非阻塞 connect 并清除 CONNECT 兴趣集 */
    protected void doFinishConnect() throws Exception {
        if (javaChannel().finishConnect()) {
            removeAndSubmit(NioIoOps.CONNECT);
        } else {
            throw new Error(
                    "Provider error: failed to finish connect. Provider library should be upgraded.");
        }
    }

    @Override
    /** 从 UDT 套接字读入字节到 {@link ByteBuf}，返回实际读取字节数 */
    protected int doReadBytes(final ByteBuf byteBuf) throws Exception {
        final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
        allocHandle.attemptedBytesRead(byteBuf.writableBytes());
        return byteBuf.writeBytes(javaChannel(), allocHandle.attemptedBytesRead());
    }

    @Override
    /** 将 {@link ByteBuf} 可读字节写入 UDT 套接字 */
    protected int doWriteBytes(final ByteBuf byteBuf) throws Exception {
        final int expectedWrittenBytes = byteBuf.readableBytes();
        return byteBuf.readBytes(javaChannel(), expectedWrittenBytes);
    }

    @Override
    /** UDT 字节流不支持半关闭输入 */
    protected ChannelFuture shutdownInput() {
        return newFailedFuture(new UnsupportedOperationException("shutdownInput"));
    }

    @Override
    /** UDT 字节 Connector 不支持 {@link FileRegion} 零拷贝写 */
    protected long doWriteFileRegion(FileRegion region) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    /** 通道已打开且 connect 已完成时为 active */
    public boolean isActive() {
        final SocketChannelUDT channelUDT = javaChannel();
        return channelUDT.isOpen() && channelUDT.isConnectFinished();
    }

    @Override
    /** 返回底层 barchart UDT NIO 套接字通道 */
    protected SocketChannelUDT javaChannel() {
        return (SocketChannelUDT) super.javaChannel();
    }

    @Override
    /** 底层套接字本地地址（未缓存） */
    protected SocketAddress localAddress0() {
        return javaChannel().socket().getLocalSocketAddress();
    }

    @Override
    /** 底层套接字远端地址（未缓存） */
    protected SocketAddress remoteAddress0() {
        return javaChannel().socket().getRemoteSocketAddress();
    }

    @Override
    /** 本地 {@link InetSocketAddress} */
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) super.localAddress();
    }

    @Override
    /** 远端 {@link InetSocketAddress} */
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) super.remoteAddress();
    }

    /** 在特权块内执行 bind，将 {@link PrivilegedActionException} 转为 {@link IOException} */
    private static void privilegedBind(final SocketChannelUDT socketChannel, final SocketAddress localAddress)
            throws IOException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    socketChannel.bind(localAddress);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getCause();
        }
    }

}
