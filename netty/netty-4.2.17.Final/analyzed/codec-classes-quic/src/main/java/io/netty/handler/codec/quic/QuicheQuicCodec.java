/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.quic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import static io.netty.handler.codec.quic.Quiche.allocateNativeOrder;

/**
 * QUIC 编解码器抽象基类：解析 UDP 报文、维护连接 ID 映射、批量 flush 出站数据。
 * 子类实现 {@link #quicPacketRead} 与 {@link #connectQuicChannel} 以区分客户端/服务端。
 */
abstract class QuicheQuicCodec extends ChannelDuplexHandler {
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(QuicheQuicCodec.class);
    private final ConnectionIdChannelMap connectionIdToChannel = new ConnectionIdChannelMap();
    private final Set<QuicheQuicChannel> channels = new HashSet<>();
    private final Queue<QuicheQuicChannel> needsFireChannelReadComplete = new ArrayDeque<>();
    private final Queue<QuicheQuicChannel> delayedRemoval = new ArrayDeque<>();

    private final Consumer<QuicheQuicChannel> freeTask = this::removeChannel;
    private final FlushStrategy flushStrategy;
    private final int localConnIdLength;
    private final QuicheConfig config;

    private MessageSizeEstimator.Handle estimatorHandle;
    private QuicHeaderParser headerParser;
    private QuicHeaderParser.QuicHeaderProcessor parserCallback;
    private int pendingBytes;
    private int pendingPackets;
    private boolean inChannelReadComplete;
    private boolean delayRemoval;

    // 将 InetSocketAddress 拷贝为 sockaddr_storage，供 JNI 层 Quiche 调用
    private ByteBuf senderSockaddrMemory;
    private ByteBuf recipientSockaddrMemory;

    QuicheQuicCodec(QuicheConfig config, int localConnIdLength, FlushStrategy flushStrategy) {
        this.config = config;
        this.localConnIdLength = localConnIdLength;
        this.flushStrategy = flushStrategy;
    }

    @Override
    public final boolean isSharable() {
        return false;
    }

    @Nullable
    protected final QuicheQuicChannel getChannel(ByteBuffer key) {
        return connectionIdToChannel.get(key);
    }

    private void addMapping(QuicheQuicChannel channel, ByteBuffer id) {
        QuicheQuicChannel ch = connectionIdToChannel.put(id, channel);
        assert ch == null || ch == channel;
    }

    private void removeMapping(QuicheQuicChannel channel, ByteBuffer id) {
        QuicheQuicChannel ch = connectionIdToChannel.remove(id);
        assert ch == channel;
    }

    private void processDelayedRemoval() {
        for (;;) {
            // 移除先前标记为延迟删除的连接
            QuicheQuicChannel toBeRemoved = delayedRemoval.poll();
            if (toBeRemoved == null) {
                break;
            }
            removeChannel(toBeRemoved);
        }
    }

    private void removeChannel(QuicheQuicChannel channel) {
        if (delayRemoval) {
            boolean added = delayedRemoval.offer(channel);
            assert added;
        } else {
            boolean removed = channels.remove(channel);
            if (removed) {
                for (ByteBuffer id : channel.sourceConnectionIds()) {
                    QuicheQuicChannel ch = connectionIdToChannel.remove(id);
                    assert ch == channel;
                }
            }
        }
    }

    protected final void addChannel(QuicheQuicChannel channel) {
        boolean added = channels.add(channel);
        assert added;
        for (ByteBuffer id : channel.sourceConnectionIds()) {
            QuicheQuicChannel ch = connectionIdToChannel.put(id.duplicate(), channel);
            assert ch == null;
        }
    }

    @Override
    public final void handlerAdded(ChannelHandlerContext ctx) {
        senderSockaddrMemory = allocateNativeOrder(Quiche.SIZEOF_SOCKADDR_STORAGE);
        recipientSockaddrMemory = allocateNativeOrder(Quiche.SIZEOF_SOCKADDR_STORAGE);
        headerParser = new QuicHeaderParser(localConnIdLength);
        parserCallback = new QuicCodecHeaderProcessor(ctx);
        estimatorHandle = ctx.channel().config().getMessageSizeEstimator().newHandle();
        handlerAdded(ctx, localConnIdLength);
    }

    /**
     * 子类扩展点，等价于 {@link io.netty.channel.ChannelHandler#handlerAdded}。
     */
    protected void handlerAdded(ChannelHandlerContext ctx, int localConnIdLength) {
        // NOOP.
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        try {
            // 复制数组后迭代，关闭通道可能触发 writability 变更并修改集合
            for (QuicheQuicChannel ch : channels.toArray(new QuicheQuicChannel[0])) {
                ch.forceClose();
            }
            if (pendingPackets > 0) {
                flushNow(ctx);
            }
        } finally {
            channels.clear();
            connectionIdToChannel.clear();
            needsFireChannelReadComplete.clear();
            delayedRemoval.clear();

            config.free();
            if (senderSockaddrMemory != null) {
                senderSockaddrMemory.release();
            }
            if (recipientSockaddrMemory != null) {
                recipientSockaddrMemory.release();
            }
            if (headerParser != null) {
                headerParser.close();
                headerParser = null;
            }
        }
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DatagramPacket packet = (DatagramPacket) msg;
        try {
            ByteBuf buffer = ((DatagramPacket) msg).content();
            if (!buffer.isDirect()) {
                // JNI 需要直接内存才能读取 memoryAddress，非直接缓冲则拷贝一份
                ByteBuf direct = ctx.alloc().directBuffer(buffer.readableBytes());
                try {
                    direct.writeBytes(buffer, buffer.readerIndex(), buffer.readableBytes());
                    handleQuicPacket(packet.sender(), packet.recipient(), direct);
                } finally {
                    direct.release();
                }
            } else {
                handleQuicPacket(packet.sender(), packet.recipient(), buffer);
            }
        } finally {
            packet.release();
        }
    }

    private void handleQuicPacket(InetSocketAddress sender, InetSocketAddress recipient, ByteBuf buffer) {
        try {
            headerParser.parse(sender, recipient, buffer, parserCallback);
        } catch (Exception e) {
            LOGGER.debug("Error while processing QUIC packet", e);
        }
    }

    /**
     * 处理 QUIC 报文并返回与连接 ID 映射的 {@link QuicheQuicChannel}。
     *
     * @param ctx the {@link ChannelHandlerContext}.
     * @param sender the {@link InetSocketAddress} of the sender of the QUIC packet
     * @param recipient the {@link InetSocketAddress} of the recipient of the QUIC packet
     * @param type the type of the packet.
     * @param version the QUIC version
     * @param scid the source connection id.
     * @param dcid the destination connection id
     * @param token the token
     * @param senderSockaddrMemory the {@link ByteBuf} that can be used for the sender {@code struct sockaddr).
     * @param recipientSockaddrMemory the {@link ByteBuf} that can be used for the recipient {@code struct sockaddr).
     * @param freeTask the {@link Consumer} that will be called once native memory of the {@link QuicheQuicChannel} is
     *                  freed and so the mappings should be deleted to the ids.
     * @param localConnIdLength the length of the local connection ids.
     * @param config the {@link QuicheConfig} that is used.
     * @return the {@link QuicheQuicChannel} that is mapped to the id.
     * @throws Exception  thrown if there is an error during processing.
     */
    @Nullable
    protected abstract QuicheQuicChannel quicPacketRead(ChannelHandlerContext ctx, InetSocketAddress sender,
                                                        InetSocketAddress recipient, QuicPacketType type, long version,
                                                        ByteBuf scid, ByteBuf dcid, ByteBuf token,
                                                        ByteBuf senderSockaddrMemory, ByteBuf recipientSockaddrMemory,
                                                        Consumer<QuicheQuicChannel> freeTask,
                                                        int localConnIdLength, QuicheConfig config) throws Exception;

    @Override
    public final void channelReadComplete(ChannelHandlerContext ctx) {
        inChannelReadComplete = true;
        try {
            for (;;) {
                QuicheQuicChannel channel = needsFireChannelReadComplete.poll();
                if (channel == null) {
                    break;
                }
                channel.recvComplete();
            }
        } finally {
            inChannelReadComplete = false;
            if (pendingPackets > 0) {
                flushNow(ctx);
            }
        }
    }

    @Override
    public final void channelWritabilityChanged(ChannelHandlerContext ctx) {
        if (ctx.channel().isWritable()) {
            // 可写时延迟从 channels 集合移除，避免迭代中修改导致异常
            delayRemoval = true;
            try {
                for (QuicheQuicChannel channel : channels) {
                    // TODO: Be a bit smarter about this.
                    channel.writable();
                }
            } finally {
                // We are done with the loop, reset the flag and process the removals from the channels Set.
                delayRemoval = false;
                processDelayedRemoval();
            }
        } else {
            // 批量 flush 策略下，不可写时至少 flush 一次，避免出站缓冲占用过多内存
            ctx.flush();
        }

        ctx.fireChannelWritabilityChanged();
    }

    @Override
    public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)  {
        pendingPackets ++;
        int size = estimatorHandle.size(msg);
        if (size > 0) {
            pendingBytes += size;
        }
        try {
            ctx.write(msg, promise);
        } finally {
            flushIfNeeded(ctx);
        }
    }

    @Override
    public final void flush(ChannelHandlerContext ctx) {
        // 在 channelReadComplete 内可延迟 flush，待所有连接处理完毕再统一发送
        if (inChannelReadComplete) {
            flushIfNeeded(ctx);
        } else if (pendingPackets > 0) {
            flushNow(ctx);
        }
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                        ChannelPromise promise) throws Exception {
        if (remoteAddress instanceof QuicheQuicChannelAddress) {
            QuicheQuicChannelAddress addr = (QuicheQuicChannelAddress) remoteAddress;
            QuicheQuicChannel channel = addr.channel;
            connectQuicChannel(channel, remoteAddress, localAddress,
                    senderSockaddrMemory, recipientSockaddrMemory, freeTask, localConnIdLength, config, promise);
        } else {
            ctx.connect(remoteAddress, localAddress, promise);
        }
    }

    /**
     * 完成给定 {@link QuicheQuicChannel} 的连接建立（客户端实现）。
     *
     * @param channel                   the {@link QuicheQuicChannel} to connect.
     * @param remoteAddress             the remote {@link SocketAddress}.
     * @param localAddress              the local  {@link SocketAddress}
     * @param senderSockaddrMemory      the {@link ByteBuf} that can be used for the sender {@code struct sockaddr).
     * @param recipientSockaddrMemory   the {@link ByteBuf} that can be used for the recipient {@code struct sockaddr).
     * @param freeTask                  the {@link Consumer} that will be called once native memory of the
     *                                  {@link QuicheQuicChannel} is freed and so the mappings should be deleted to
     *                                  the ids.
     * @param localConnIdLength         the length of the local connection ids.
     * @param config                    the {@link QuicheConfig} that is used.
     * @param promise                   the {@link ChannelPromise} to notify once the connect is done.
     */
    protected abstract void connectQuicChannel(QuicheQuicChannel channel, SocketAddress remoteAddress,
                                               SocketAddress localAddress, ByteBuf senderSockaddrMemory,
                                               ByteBuf recipientSockaddrMemory, Consumer<QuicheQuicChannel> freeTask,
                                               int localConnIdLength, QuicheConfig config, ChannelPromise promise);

    private void flushIfNeeded(ChannelHandlerContext ctx) {
        // 按 FlushStrategy 决定是否立即 flush，及时发送并释放底层出站缓冲
        if (flushStrategy.shouldFlushNow(pendingPackets, pendingBytes)) {
            flushNow(ctx);
        }
    }

    private void flushNow(ChannelHandlerContext ctx) {
        pendingBytes = 0;
        pendingPackets = 0;
        ctx.flush();
    }

    private final class QuicCodecHeaderProcessor implements QuicHeaderParser.QuicHeaderProcessor {

        private final ChannelHandlerContext ctx;

        QuicCodecHeaderProcessor(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void process(InetSocketAddress sender, InetSocketAddress recipient, ByteBuf buffer, QuicPacketType type,
                            long version, ByteBuf scid, ByteBuf dcid, ByteBuf token) throws Exception {
            QuicheQuicChannel channel = quicPacketRead(ctx, sender, recipient,
                    type, version, scid,
                    dcid, token, senderSockaddrMemory, recipientSockaddrMemory, freeTask, localConnIdLength, config);
            if (channel != null) {
                // 先入队，在 channelReadComplete 中合并 flush 以减少系统调用
                if (channel.markInFireChannelReadCompleteQueue()) {
                    needsFireChannelReadComplete.add(channel);
                }
                channel.recv(sender, recipient, buffer);
                for (ByteBuffer retiredSourceConnectionId : channel.retiredSourceConnectionId()) {
                    removeMapping(channel, retiredSourceConnectionId);
                }
                for (ByteBuffer newSourceConnectionId : channel.newSourceConnectionIds()) {
                    addMapping(channel, newSourceConnectionId);
                }
            }
        }
    }
}
