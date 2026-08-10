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
package io.netty.channel.sctp.nio;

import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.NotificationHandler;
import com.sun.nio.sctp.SctpChannel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.nio.AbstractNioMessageChannel;
import io.netty.channel.nio.NioIoOps;
import io.netty.channel.sctp.DefaultSctpChannelConfig;
import io.netty.channel.sctp.SctpChannelConfig;
import io.netty.channel.sctp.SctpMessage;
import io.netty.channel.sctp.SctpNotificationHandler;
import io.netty.channel.sctp.SctpServerChannel;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link io.netty.channel.sctp.SctpChannel} implementation which use non-blocking mode and allows to read /
 * write {@link SctpMessage}s to the underlying {@link SctpChannel}.
 * <p>基于 NIO 的非阻塞 SCTP 客户端：{@link AbstractNioMessageChannel} 上读写  {@link SctpMessage}，JDK 22–24 上对 direct {@link ByteBuffer} 使用拷贝规避  JDK-8357268。</p>
 *
 * Be aware that not all operations systems support SCTP. Please refer to the documentation of your operation system,
 * to understand what you need to do to use it. Also this feature is only supported on Java 7+.
 */
public class NioSctpChannel extends AbstractNioMessageChannel implements io.netty.channel.sctp.SctpChannel {
    /** 非服务端通道，无固定远端（连接后才有） */
    private static final ChannelMetadata METADATA = new ChannelMetadata(false);

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioSctpChannel.class);

    /** 通道配置（含 SCTP 选项） */
    private final SctpChannelConfig config;

    /** receive 时分发 SCTP 通知的 JDK handler */
    private final NotificationHandler<?> notificationHandler;
    /** Java 22–24 读路径 direct 缓冲拷贝 */
    private ByteBuffer inputCopy;
    /** Java 22–24 写路径 direct 缓冲拷贝 */
    private ByteBuffer outputCopy;

    /** 打开 JDK {@link SctpChannel}，失败抛 {@link ChannelException} */
    private static SctpChannel newSctpChannel() {
        try {
            return SctpChannel.open();
        } catch (IOException e) {
            throw new ChannelException("Failed to open a sctp channel.", e);
        }
    }

    /**
     * Create a new instance
     * <p>打开新 JDK SCTP 通道并进入非阻塞模式。</p>
     */
    public NioSctpChannel() {
        this(newSctpChannel());
    }

    /**
     * Create a new instance using {@link SctpChannel}
     * <p>包装已有 JDK 通道（如 accept 产物）。</p>
     */
    public NioSctpChannel(SctpChannel sctpChannel) {
        this(null, sctpChannel);
    }

    /**
     * Create a new instance
     * <p>创建新实例。</p>
     *
     * @param parent        the {@link Channel} which is the parent of this {@link NioSctpChannel}
     *                      or {@code null}.
     * @param sctpChannel   the underlying {@link SctpChannel}
     */
    public NioSctpChannel(Channel parent, SctpChannel sctpChannel) {
        super(parent, sctpChannel, SelectionKey.OP_READ);
        try {
            sctpChannel.configureBlocking(false);
            config = new NioSctpChannelConfig(this, sctpChannel);
            notificationHandler = new SctpNotificationHandler(this);
        } catch (IOException e) {
            try {
                sctpChannel.close();
            } catch (IOException e2) {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Failed to close a partially initialized sctp channel.", e2);
                }
            }

            throw new ChannelException("Failed to enter non-blocking mode.", e);
        }
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) super.localAddress();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) super.remoteAddress();
    }

    @Override
    public SctpServerChannel parent() {
        return (SctpServerChannel) super.parent();
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    @Override
    public Association association() {
        try {
            return javaChannel().association();
        } catch (IOException ignored) {
            return null;
        }
    }

    @Override
    public Set<InetSocketAddress> allLocalAddresses() {
        try {
            final Set<SocketAddress> allLocalAddresses = javaChannel().getAllLocalAddresses();
            final Set<InetSocketAddress> addresses = new LinkedHashSet<InetSocketAddress>(allLocalAddresses.size());
            for (SocketAddress socketAddress : allLocalAddresses) {
                addresses.add((InetSocketAddress) socketAddress);
            }
            return addresses;
        } catch (Throwable ignored) {
            return Collections.emptySet();
        }
    }

    @Override
    public SctpChannelConfig config() {
        return config;
    }

    @Override
    public Set<InetSocketAddress> allRemoteAddresses() {
        try {
            final Set<SocketAddress> allLocalAddresses = javaChannel().getRemoteAddresses();
            final Set<InetSocketAddress> addresses = new HashSet<InetSocketAddress>(allLocalAddresses.size());
            for (SocketAddress socketAddress : allLocalAddresses) {
                addresses.add((InetSocketAddress) socketAddress);
            }
            return addresses;
        } catch (Throwable ignored) {
            return Collections.emptySet();
        }
    }

    @Override
    protected SctpChannel javaChannel() {
        return (SctpChannel) super.javaChannel();
    }

    @Override
    public boolean isActive() {
        SctpChannel ch = javaChannel();
        return ch.isOpen() && association() != null;
    }

    @Override
    protected SocketAddress localAddress0() {
        try {
            Iterator<SocketAddress> i = javaChannel().getAllLocalAddresses().iterator();
            if (i.hasNext()) {
                return i.next();
            }
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        try {
            Iterator<SocketAddress> i = javaChannel().getRemoteAddresses().iterator();
            if (i.hasNext()) {
                return i.next();
            }
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        javaChannel().bind(localAddress);
    }

    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        if (localAddress != null) {
            javaChannel().bind(localAddress);
        }

        boolean success = false;
        try {
            boolean connected = javaChannel().connect(remoteAddress);
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
    protected void doFinishConnect() throws Exception {
        if (!javaChannel().finishConnect()) {
            throw new UnsupportedOperationException("finishConnect is not supported for " + getClass().getName());
        }
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    protected void doClose() throws Exception {
        javaChannel().close();
    }

    @Override
    /** 从 JDK 通道 receive，封装为 {@link SctpMessage} 入队 */
    protected int doReadMessages(List<Object> buf) throws Exception {
        SctpChannel ch = javaChannel();

        RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
        ByteBuf buffer = allocHandle.allocate(config().getAllocator());
        boolean free = true;
        try {
            ByteBuffer data = buffer.internalNioBuffer(buffer.writerIndex(), buffer.writableBytes());
            boolean useInputCopy = false;
            int javaVersion = PlatformDependent.javaVersion();
            if (javaVersion >= 22 && javaVersion < 25 && data.isDirect()) {
                // Java 22–24：避免 MemorySegment 支撑的 direct ByteBuffer（JDK-8357268）
                if (inputCopy == null || inputCopy.capacity() < data.remaining()) {
                    inputCopy = ByteBuffer.allocateDirect(data.remaining());
                }
                inputCopy.clear();
                inputCopy.limit(data.remaining());
                useInputCopy = true;
            }
            int pos = data.position();

            MessageInfo messageInfo = ch.receive(useInputCopy ? inputCopy : data, null, notificationHandler);
            if (messageInfo == null) {
                return 0;
            }
            if (useInputCopy) {
                inputCopy.flip();
                data.put(inputCopy);
            }

            allocHandle.lastBytesRead(data.position() - pos);
            buf.add(new SctpMessage(messageInfo,
                    buffer.writerIndex(buffer.writerIndex() + allocHandle.lastBytesRead())));
            free = false;
            return 1;
        } catch (Throwable cause) {
            PlatformDependent.throwException(cause);
            return -1;
        }  finally {
            if (free) {
                buffer.release();
            }
        }
    }

    @Override
    /** 将 {@link SctpMessage} 写入 JDK 通道 send */
    protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in) throws Exception {
        SctpMessage packet = (SctpMessage) msg;
        ByteBuf data = packet.content();
        int dataLen = data.readableBytes();
        if (dataLen == 0) {
            return true;
        }

        ByteBuffer nioData;
        int javaVersion = PlatformDependent.javaVersion();
        if (javaVersion >= 22 && javaVersion < 25 && data.isDirect() ||
                !data.isDirect() || data.nioBufferCount() != 1) {
            // SCTP IO 需单一 direct ByteBuffer；复合/on-heap 或 Java 22–24 时拷贝
            if (outputCopy == null || outputCopy.capacity() < dataLen) {
                outputCopy = ByteBuffer.allocateDirect(dataLen);
            }
            outputCopy.clear();
            outputCopy.limit(dataLen);
            data.readBytes(outputCopy);
            outputCopy.flip();
            nioData = outputCopy;
        } else {
            nioData = data.nioBuffer();
        }

        final MessageInfo mi = MessageInfo.createOutgoing(association(), null, packet.streamIdentifier());
        mi.payloadProtocolID(packet.protocolIdentifier());
        mi.streamNumber(packet.streamIdentifier());
        mi.unordered(packet.isUnordered());

        final int writtenBytes = javaChannel().send(nioData, mi);
        return writtenBytes > 0;
    }

    @Override
    /** 出站仅接受 {@link SctpMessage}，非 direct 单缓冲则拷贝 */
    protected final Object filterOutboundMessage(Object msg) throws Exception {
        if (msg instanceof SctpMessage) {
            SctpMessage m = (SctpMessage) msg;
            ByteBuf buf = m.content();
            if (buf.isDirect() && buf.nioBufferCount() == 1) {
                return m;
            }

            return new SctpMessage(m.protocolIdentifier(), m.streamIdentifier(), m.isUnordered(),
                                   newDirectBuffer(m, buf));
        }

        throw new UnsupportedOperationException(
                "unsupported message type: " + StringUtil.simpleClassName(msg) +
                " (expected: " + StringUtil.simpleClassName(SctpMessage.class));
    }

    @Override
    public ChannelFuture bindAddress(InetAddress localAddress) {
        return bindAddress(localAddress, newPromise());
    }

    @Override
    public ChannelFuture bindAddress(final InetAddress localAddress, final ChannelPromise promise) {
        if (eventLoop().inEventLoop()) {
            try {
                javaChannel().bindAddress(localAddress);
                promise.setSuccess();
            } catch (Throwable t) {
                promise.setFailure(t);
            }
        } else {
            eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    bindAddress(localAddress, promise);
                }
            });
        }
        return promise;
    }

    @Override
    public ChannelFuture unbindAddress(InetAddress localAddress) {
        return unbindAddress(localAddress, newPromise());
    }

    @Override
    public ChannelFuture unbindAddress(final InetAddress localAddress, final ChannelPromise promise) {
        if (eventLoop().inEventLoop()) {
            try {
                javaChannel().unbindAddress(localAddress);
                promise.setSuccess();
            } catch (Throwable t) {
                promise.setFailure(t);
            }
        } else {
            eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    unbindAddress(localAddress, promise);
                }
            });
        }
        return promise;
    }

    /** NIO 专用配置：autoRead 清除时同步清除 readPending */
    private final class NioSctpChannelConfig extends DefaultSctpChannelConfig {
        private NioSctpChannelConfig(NioSctpChannel channel, SctpChannel javaChannel) {
            super(channel, javaChannel);
        }

        @Override
        protected void autoReadCleared() {
            clearReadPending();
        }
    }
}
