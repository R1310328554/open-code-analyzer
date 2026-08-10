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
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.IoRegistration;
import io.netty.channel.nio.NioIoOps;
import io.netty.util.internal.SocketUtils;
import io.netty.channel.nio.AbstractNioMessageChannel;
import io.netty.channel.udt.DefaultUdtChannelConfig;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.UdtChannelConfig;
import io.netty.channel.udt.UdtMessage;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;

/**
 * Message Connector for UDT Datagrams.
 * <p>
 * Note: send/receive must use {@link UdtMessage} in the pipeline
 * <p>UDT DATAGRAM 消息 Connector：基于 {@link AbstractNioMessageChannel}， 每次 read 产出单个 {@link UdtMessage}，write 须传入 {@link UdtMessage}。 接收缓冲大小同时限制单消息最大尺寸。</p>
 *
 * @deprecated The UDT transport is no longer maintained and will be removed.
 */
@Deprecated
public class NioUdtMessageConnectorChannel extends AbstractNioMessageChannel implements UdtChannel {

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(NioUdtMessageConnectorChannel.class);

    private static final ChannelMetadata METADATA = new ChannelMetadata(false);

    private final UdtChannelConfig config;

    /** 创建默认 DATAGRAM 类型的 UDT 消息 Connector */
    public NioUdtMessageConnectorChannel() {
        this(TypeUDT.DATAGRAM);
    }

    /**
     * 包装已有 UDT 套接字（Acceptor accept 或外部传入）。
     * <p>注册 READ 兴趣；INIT/OPENED 时 apply 通道配置。</p>
     */
    public NioUdtMessageConnectorChannel(final Channel parent, final SocketChannelUDT channelUDT) {
        super(parent, channelUDT, NioIoOps.READ);
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
                logger.warn("Failed to close channel.", e2);
            }
            throw new ChannelException("Failed to configure channel.", e);
        }
    }

    /** 无 parent 包装已有 {@link SocketChannelUDT} */
    public NioUdtMessageConnectorChannel(final SocketChannelUDT channelUDT) {
        this(null, channelUDT);
    }

    /** 按 {@link TypeUDT} 打开新套接字并构造消息 Connector */
    public NioUdtMessageConnectorChannel(final TypeUDT type) {
        this(NioUdtProvider.newConnectorChannelUDT(type));
    }

    @Override
    public UdtChannelConfig config() {
        return config;
    }

    @Override
    /** 特权 bind 本地地址 */
    protected void doBind(final SocketAddress localAddress) throws Exception {
        privilegedBind(javaChannel(), localAddress);
    }

    @Override
    /** 关闭底层 UDT 套接字 */
    protected void doClose() throws Exception {
        javaChannel().close();
    }

    @Override
    /** bind（可选）后 connect；未完成时注册 CONNECT 兴趣 */
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
    /** 断开即关闭 */
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    /** 完成非阻塞 connect 并更新 IO 注册 */
    protected void doFinishConnect() throws Exception {
        if (javaChannel().finishConnect()) {
            IoRegistration registration = registration();
            removeAndSubmit(NioIoOps.CONNECT);
        } else {
            throw new Error(
                    "Provider error: failed to finish connect. Provider library should be upgraded.");
        }
    }

    @Override
    /** 读单条 DATAGRAM 消息为 {@link UdtMessage}；超长则关闭通道并抛异常 */
    protected int doReadMessages(List<Object> buf) throws Exception {

        final int maximumMessageSize = config.getReceiveBufferSize();

        final ByteBuf byteBuf = config.getAllocator().directBuffer(
                maximumMessageSize);

        final int receivedMessageSize = byteBuf.writeBytes(javaChannel(),
                maximumMessageSize);

        if (receivedMessageSize <= 0) {
            byteBuf.release();
            return 0;
        }

        if (receivedMessageSize >= maximumMessageSize) {
            javaChannel().close();
            throw new ChannelException(
                    "Invalid config : increase receive buffer size to avoid message truncation");
        }

        // 将读到的字节包装为 UdtMessage 加入 buf
        buf.add(new UdtMessage(byteBuf));

        return 1;
    }

    @Override
    /** 将 {@link UdtMessage} 内容一次性写入 UDT 套接字；空消息视为已写完 */
    protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in) throws Exception {
        // 出站消息须为 UdtMessage
        final UdtMessage message = (UdtMessage) msg;

        final ByteBuf byteBuf = message.content();

        final int messageSize = byteBuf.readableBytes();
        if (messageSize == 0) {
            return true;
        }

        final long writtenBytes;
        if (byteBuf.nioBufferCount() == 1) {
            writtenBytes = javaChannel().write(byteBuf.nioBuffer());
        } else {
            writtenBytes = javaChannel().write(byteBuf.nioBuffers());
        }

        // 要求 UDT 提供方一次写完整条消息
        if (writtenBytes > 0 && writtenBytes != messageSize) {
            throw new Error(
                    "Provider error: failed to write message. Provider library should be upgraded.");
        }

        return writtenBytes > 0;
    }

    @Override
    /** 已打开且 connect 完成 */
    public boolean isActive() {
        final SocketChannelUDT channelUDT = javaChannel();
        return channelUDT.isOpen() && channelUDT.isConnectFinished();
    }

    @Override
    /** 底层 {@link SocketChannelUDT} */
    protected SocketChannelUDT javaChannel() {
        return (SocketChannelUDT) super.javaChannel();
    }

    @Override
    /** 本地套接字地址 */
    protected SocketAddress localAddress0() {
        return javaChannel().socket().getLocalSocketAddress();
    }

    @Override
    /** 消息模式无 stream 语义，metadata 固定 */
    public ChannelMetadata metadata() {
        return METADATA;
    }

    @Override
    /** 远端套接字地址 */
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

    /** 特权 bind，IOException 自 PrivilegedActionException 解包 */
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
