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
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelId;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.PendingWriteQueue;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.VoidChannelPromise;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.channel.socket.ChannelOutputShutdownException;
import io.netty.util.DefaultAttributeMap;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.PromiseNotifier;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.RejectedExecutionException;

/**
 * 基于 <a href="https://github.com/cloudflare/quiche">quiche</a> 的 {@link QuicStreamChannel} 实现。
 * 封装单条 QUIC 流的读写、半关闭、优先级与 FIN 语义。
 */
final class QuicheQuicStreamChannel extends DefaultAttributeMap implements QuicStreamChannel {
    private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(QuicheQuicStreamChannel.class);
    private final QuicheQuicChannel parent;
    private final ChannelId id;
    private final ChannelPipeline pipeline;
    private final QuicStreamChannelUnsafe unsafe;
    private final ChannelPromise closePromise;
    private final PendingWriteQueue queue;

    private final QuicStreamChannelConfig config;
    private final QuicStreamAddress address;

    private boolean readable;
    private boolean readPending;
    private boolean inRecv;
    private boolean inWriteQueued;
    private boolean finReceived;
    private boolean finSent;

    private volatile boolean registered;
    private volatile boolean writable = true;
    private volatile boolean active = true;
    private volatile boolean inputShutdown;
    private volatile boolean outputShutdown;
    private volatile QuicStreamPriority priority;
    private volatile long capacity;

    QuicheQuicStreamChannel(QuicheQuicChannel parent, long streamId) {
        this.parent = parent;
        this.id = DefaultChannelId.newInstance();
        unsafe = new QuicStreamChannelUnsafe();
        this.pipeline = new DefaultChannelPipeline(this) {
            // TODO: 可按需覆盖 pipeline 行为
        };
        config = new QuicheQuicStreamChannelConfig(this);
        this.address = new QuicStreamAddress(streamId);
        this.closePromise = newPromise();
        queue = new PendingWriteQueue(this);
        // 本地创建的单向流按规范输入端已关闭，不会再有可读数据
        if (parent.streamType(streamId) == QuicStreamType.UNIDIRECTIONAL && parent.isStreamLocalCreated(streamId)) {
            inputShutdown = true;
        }
    }

    @Override
    public QuicStreamAddress localAddress() {
        return address;
    }

    @Override
    public QuicStreamAddress remoteAddress() {
        return address;
    }

    @Override
    public boolean isLocalCreated() {
        return parent().isStreamLocalCreated(streamId());
    }

    @Override
    public QuicStreamType type() {
        return parent().streamType(streamId());
    }

    @Override
    public long streamId() {
        return address.streamId();
    }

    @Override
    public QuicStreamPriority priority() {
        return priority;
    }

    @Override
    public ChannelFuture updatePriority(QuicStreamPriority priority, ChannelPromise promise) {
        if (eventLoop().inEventLoop()) {
            updatePriority0(priority, promise);
        } else {
            eventLoop().execute(() -> updatePriority0(priority, promise));
        }
        return promise;
    }

    private void updatePriority0(QuicStreamPriority priority, ChannelPromise promise) {
        assert eventLoop().inEventLoop();
        if (!promise.setUncancellable()) {
            return;
        }
        try {
            parent().streamPriority(streamId(), (byte) priority.urgency(), priority.isIncremental());
        } catch (Throwable cause) {
            promise.setFailure(cause);
            return;
        }
        this.priority = priority;
        promise.setSuccess();
    }

    @Override
    public boolean isInputShutdown() {
        return inputShutdown;
    }

    @Override
    public ChannelFuture shutdownOutput(ChannelPromise promise) {
        if (eventLoop().inEventLoop()) {
            shutdownOutput0(promise);
        } else {
            eventLoop().execute(() -> shutdownOutput0(promise));
        }
        return promise;
    }

    private void shutdownOutput0(ChannelPromise promise) {
        assert eventLoop().inEventLoop();
        if (!promise.setUncancellable()) {
            return;
        }
        outputShutdown = true;
        unsafe.writeWithoutCheckChannelState(QuicStreamFrame.EMPTY_FIN, promise);
        unsafe.flush();
    }

    @Override
    public ChannelFuture shutdownInput(int error, ChannelPromise promise) {
        if (eventLoop().inEventLoop()) {
            shutdownInput0(error, promise);
        } else {
            eventLoop().execute(() -> shutdownInput0(error, promise));
        }
        return promise;
    }

    @Override
    public ChannelFuture shutdownOutput(int error, ChannelPromise promise) {
        if (eventLoop().inEventLoop()) {
            shutdownOutput0(error, promise);
        } else {
            eventLoop().execute(() -> shutdownOutput0(error, promise));
        }
        return promise;
    }

    @Override
    public QuicheQuicChannel parent() {
        return parent;
    }

    private void shutdownInput0(int err, ChannelPromise channelPromise) {
        assert eventLoop().inEventLoop();
        if (!channelPromise.setUncancellable()) {
            return;
        }
        inputShutdown = true;
        parent().streamShutdown(streamId(), true, false, err, channelPromise);
        closeIfDone();
    }

    @Override
    public boolean isOutputShutdown() {
        return outputShutdown;
    }

    private void shutdownOutput0(int error, ChannelPromise channelPromise) {
        assert eventLoop().inEventLoop();
        if (!channelPromise.setUncancellable()) {
            return;
        }
        parent().streamShutdown(streamId(), false, true, error, channelPromise);
        outputShutdown = true;
        closeIfDone();
    }

    @Override
    public boolean isShutdown() {
        return outputShutdown && inputShutdown;
    }

    @Override
    public ChannelFuture shutdown(ChannelPromise channelPromise) {
        if (eventLoop().inEventLoop()) {
            shutdown0(channelPromise);
        } else {
            eventLoop().execute(() -> shutdown0(channelPromise));
        }
        return channelPromise;
    }

    private void shutdown0(ChannelPromise promise) {
        assert eventLoop().inEventLoop();
        if (!promise.setUncancellable()) {
            return;
        }
        inputShutdown = true;
        outputShutdown = true;
        unsafe.writeWithoutCheckChannelState(QuicStreamFrame.EMPTY_FIN, unsafe.voidPromise());
        unsafe.flush();
        parent().streamShutdown(streamId(), true, false, 0, promise);
        closeIfDone();
    }

    @Override
    public ChannelFuture shutdown(int error, ChannelPromise promise) {
        if (eventLoop().inEventLoop()) {
            shutdown0(error, promise);
        } else {
            eventLoop().execute(() -> shutdown0(error, promise));
        }
        return promise;
    }

    private void shutdown0(int error, ChannelPromise channelPromise) {
        assert eventLoop().inEventLoop();
        if (!channelPromise.setUncancellable()) {
            return;
        }
        inputShutdown = true;
        outputShutdown = true;
        parent().streamShutdown(streamId(), true, true, error, channelPromise);
        closeIfDone();
    }

    private void sendFinIfNeeded() throws Exception {
        if (!finSent) {
            finSent = true;
            parent().streamSendFin(streamId());
        }
    }

    private void closeIfDone() {
        if (finSent && (finReceived || type() == QuicStreamType.UNIDIRECTIONAL && isLocalCreated())) {
            unsafe().close(unsafe().voidPromise());
        }
    }

    private void removeStreamFromParent() {
        if (!active && finReceived) {
            parent().streamClosed(streamId());
            inputShutdown = true;
            outputShutdown = true;
        }
    }

    @Override
    public QuicStreamChannel flush() {
        pipeline.flush();
        return this;
    }

    @Override
    public QuicStreamChannel read() {
        pipeline.read();
        return this;
    }

    @Override
    public QuicStreamChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return active;
    }

    @Override
    public boolean isActive() {
        return isOpen();
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    @Override
    public ChannelId id() {
        return id;
    }

    @Override
    public EventLoop eventLoop() {
        return parent.eventLoop();
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public ChannelFuture closeFuture() {
        return closePromise;
    }

    @Override
    public boolean isWritable() {
        return writable;
    }

    @Override
    public long bytesBeforeUnwritable() {
        // 流关闭后 capacity 可能为负，对外显示为 0
        return Math.max(capacity, 0);
    }

    @Override
    public long bytesBeforeWritable() {
        if (writable) {
            return 0;
        }
        // 不可写时暂返回正数占位
        return 8;
    }

    @Override
    public QuicStreamChannelUnsafe unsafe() {
        return unsafe;
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
    }

    @Override
    public ByteBufAllocator alloc() {
        return config.getAllocator();
    }

    @Override
    public int compareTo(Channel o) {
        return id.compareTo(o.id());
    }

    /** 基于 {@link ChannelId} 的哈希值。 */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** 仅当 {@code this == o} 时为 true（流通道不做值相等比较）。 */
    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public String toString() {
        return "[id: 0x" + id.asShortText() + ", " + address + "]";
    }

    /** 父连接通知流可写容量变化，处理排队写与 STOP_SENDING。 */
    boolean writable(long capacity) {
        assert eventLoop().inEventLoop();
        if (capacity < 0) {
            // 负 capacity 表示 Quiche 错误码
            if (capacity != Quiche.QUICHE_ERR_DONE) {
                if (!queue.isEmpty()) {
                    if (capacity == Quiche.QUICHE_ERR_STREAM_STOPPED) {
                        queue.removeAndFailAll(new ChannelOutputShutdownException("STOP_SENDING frame received"));
                        // 收到 STOP_SENDING 仅失败排队写，不立即关闭通道
                        return false;
                    } else {
                        queue.removeAndFailAll(Quiche.convertToException((int) capacity));
                    }
                } else if (capacity == Quiche.QUICHE_ERR_STREAM_STOPPED) {
                    // STOP_SENDING 时保持通道打开以便读完剩余数据
                    return false;
                }
                // 非 STREAM_STOPPED 的错误需关闭流
                finSent = true;
                unsafe().close(unsafe().voidPromise());
            }
            return false;
        }
        this.capacity = capacity;
        boolean mayNeedWrite = unsafe().writeQueued();
        // writeQueued 可能更新 capacity，需重新读取
        updateWritabilityIfNeeded(this.capacity > 0);
        return mayNeedWrite;
    }

    private void updateWritabilityIfNeeded(boolean newWritable) {
        if (writable != newWritable) {
            writable = newWritable;
            pipeline.fireChannelWritabilityChanged();
        }
    }

    /** 标记流可读；若已有 pending read 则立即 recv。 */
    void readable() {
        assert eventLoop().inEventLoop();
        // 标记可读，有挂起 read 时触发 recv
        readable = true;
        if (readPending) {
            unsafe().recv();
        }
    }

    final class QuicStreamChannelUnsafe implements Unsafe {

        @SuppressWarnings("deprecation")
        private RecvByteBufAllocator.Handle recvHandle;

        private final ChannelPromise voidPromise = new VoidChannelPromise(
                QuicheQuicStreamChannel.this, false);
        @Override
        public void connect(SocketAddress remote, SocketAddress local, ChannelPromise promise) {
            assert eventLoop().inEventLoop();
            promise.setFailure(new UnsupportedOperationException());
        }

        @SuppressWarnings("deprecation")
        @Override
        public RecvByteBufAllocator.Handle recvBufAllocHandle() {
            if (recvHandle == null) {
                recvHandle = config.getRecvByteBufAllocator().newHandle();
            }
            return recvHandle;
        }

        @Override
        public SocketAddress localAddress() {
            return address;
        }

        @Override
        public SocketAddress remoteAddress() {
            return address;
        }

        @Override
        public void register(EventLoop eventLoop, ChannelPromise promise) {
            assert eventLoop.inEventLoop();
            if (!promise.setUncancellable()) {
                return;
            }
            if (registered) {
                promise.setFailure(new IllegalStateException());
                return;
            }
            if (eventLoop != parent.eventLoop()) {
                promise.setFailure(new IllegalArgumentException());
                return;
            }
            registered = true;
            promise.setSuccess();
            pipeline.fireChannelRegistered();
            pipeline.fireChannelActive();
        }

        @Override
        public void bind(SocketAddress localAddress, ChannelPromise promise) {
            assert eventLoop().inEventLoop();
            if (!promise.setUncancellable()) {
                return;
            }
            promise.setFailure(new UnsupportedOperationException());
        }

        @Override
        public void disconnect(ChannelPromise promise) {
            assert eventLoop().inEventLoop();
            close(promise);
        }

        @Override
        public void close(ChannelPromise promise) {
            close(null, promise);
        }

        void close(@Nullable ClosedChannelException writeFailCause, ChannelPromise promise) {
            assert eventLoop().inEventLoop();
            if (!promise.setUncancellable()) {
                return;
            }
            if (!active || closePromise.isDone()) {
                if (promise.isVoid()) {
                    return;
                }
                closePromise.addListener(new PromiseNotifier<>(promise));
                return;
            }
            active = false;
            try {
                // 关闭流并失败所有排队消息
                sendFinIfNeeded();
            } catch (Exception ignore) {
                // sendFin 异常可忽略
            } finally {
                if (!queue.isEmpty()) {
                    // 队列非空时才批量失败
                    if (writeFailCause == null) {
                        writeFailCause = new ClosedChannelException();
                    }
                    queue.removeAndFailAll(writeFailCause);
                }

                promise.trySuccess();
                closePromise.trySuccess();
                if (type() == QuicStreamType.UNIDIRECTIONAL && isLocalCreated()) {
                    inputShutdown = true;
                    outputShutdown = true;
                    // 本地单向流不会再收到对端数据，可立即通知父连接 streamClosed
                    parent().streamClosed(streamId());
                } else {
                    removeStreamFromParent();
                }
            }
            if (inWriteQueued) {
                invokeLater(() -> deregister(voidPromise(), true));
            } else {
                deregister(voidPromise(), true);
            }
        }

        private void deregister(final ChannelPromise promise, final boolean fireChannelInactive) {
            assert eventLoop().inEventLoop();
            if (!promise.setUncancellable()) {
                return;
            }

            if (!registered) {
                promise.trySuccess();
                return;
            }

            // deregister 延迟到 EventLoop 任务中执行，避免 pipeline 回调栈内重入导致跨线程调用
            // 参见 https://github.com/netty/netty/issues/4435
            invokeLater(() -> {
                if (fireChannelInactive) {
                    pipeline.fireChannelInactive();
                }
                // Some transports like local and AIO does not allow the deregistration of
                // an open channel.  Their doDeregister() calls close(). Consequently,
                // close() calls deregister() again - no need to fire channelUnregistered, so check
                // if it was registered.
                if (registered) {
                    registered = false;
                    pipeline.fireChannelUnregistered();
                }
                promise.setSuccess();
            });
        }

        private void invokeLater(Runnable task) {
            try {
                // 出站操作触发的入站事件延后执行，避免同一 handler 入站方法嵌套调用
                eventLoop().execute(task);
            } catch (RejectedExecutionException e) {
                LOGGER.warn("Can't invoke task later as EventLoop rejected it", e);
            }
        }

        @Override
        public void closeForcibly() {
            assert eventLoop().inEventLoop();
            close(unsafe().voidPromise());
        }

        @Override
        public void deregister(ChannelPromise promise) {
            assert eventLoop().inEventLoop();
            deregister(promise, false);
        }

        @Override
        public void beginRead() {
            assert eventLoop().inEventLoop();
            readPending = true;
            if (readable) {
                unsafe().recv();

                // recv 可能产生窗口更新，需 connectionSendAndFlush 将对端窗口变化发回
                parent().connectionSendAndFlush();
            }
        }

        private void closeIfNeeded(boolean wasFinSent) {
            // 已发 FIN 后：单向流可关；双向流需同时收到对端 FIN
            if (!wasFinSent && QuicheQuicStreamChannel.this.finSent
                    && (type() == QuicStreamType.UNIDIRECTIONAL || finReceived)) {
                // close the channel now
                close(voidPromise());
            }
        }

        boolean writeQueued() {
            assert eventLoop().inEventLoop();
            boolean wasFinSent = QuicheQuicStreamChannel.this.finSent;
            inWriteQueued = true;
            try {
                if (queue.isEmpty()) {
                    return false;
                }
                boolean written = false;
                for (;;) {
                    Object msg = queue.current();
                    if (msg == null) {
                        break;
                    }
                    try {
                        int res = write0(msg);
                        if (res == 1) {
                            queue.remove().setSuccess();
                            written = true;
                        } else if (res == 0 || res == Quiche.QUICHE_ERR_DONE) {
                            break;
                        } else if (res == Quiche.QUICHE_ERR_STREAM_STOPPED) {
                            // STREAM_STOPPED 时失败排队写，但通道保持打开供用户读完
                            queue.removeAndFailAll(
                                    new ChannelOutputShutdownException("STOP_SENDING frame received"));
                            break;
                        } else {
                            queue.remove().setFailure(Quiche.convertToException(res));
                        }
                    } catch (Exception e) {
                        queue.remove().setFailure(e);
                    }
                }
                if (written) {
                    updateWritabilityIfNeeded(true);
                }
                return written;
            } finally {
                closeIfNeeded(wasFinSent);
                inWriteQueued = false;
            }
        }

        @Override
        public void write(Object msg, ChannelPromise promise) {
            assert eventLoop().inEventLoop();
            if (!promise.setUncancellable()) {
                ReferenceCountUtil.release(msg);
                return;
            }
            // 先检查通道是否仍可写，否则用合适异常失败
            if (!isOpen()) {
                queueAndFailAll(msg, promise, new ClosedChannelException());
            } else if (finSent) {
                queueAndFailAll(msg, promise, new ChannelOutputShutdownException("Fin was sent already"));
            } else if (!queue.isEmpty()) {
                // 队列非空则入队，待可写时 drain
                try {
                    msg = filterMsg(msg);
                } catch (UnsupportedOperationException e) {
                    ReferenceCountUtil.release(msg);
                    promise.setFailure(e);
                    return;
                }

                // touch 便于排查 ByteBuf 泄漏
                ReferenceCountUtil.touch(msg);
                queue.add(msg, promise);

                // 再次尝试写出排队消息
                writeQueued();
            } else {
                assert queue.isEmpty();
                writeWithoutCheckChannelState(msg, promise);
            }
        }

        private void queueAndFailAll(Object msg, ChannelPromise promise, Throwable cause) {
            // Touch the message to make things easier in terms of debugging buffer leaks.
            ReferenceCountUtil.touch(msg);

            queue.add(msg, promise);
            queue.removeAndFailAll(cause);
        }

        private Object filterMsg(Object msg) {
            if (msg instanceof ByteBuf) {
                ByteBuf buffer = (ByteBuf)  msg;
                if (!buffer.isDirect()) {
                    ByteBuf tmpBuffer = alloc().directBuffer(buffer.readableBytes());
                    tmpBuffer.writeBytes(buffer, buffer.readerIndex(), buffer.readableBytes());
                    buffer.release();
                    return tmpBuffer;
                }
            } else if (msg instanceof QuicStreamFrame) {
                QuicStreamFrame frame = (QuicStreamFrame) msg;
                ByteBuf buffer = frame.content();
                if (!buffer.isDirect()) {
                    ByteBuf tmpBuffer = alloc().directBuffer(buffer.readableBytes());
                    tmpBuffer.writeBytes(buffer, buffer.readerIndex(), buffer.readableBytes());
                    QuicStreamFrame tmpFrame = frame.replace(tmpBuffer);
                    frame.release();
                    return tmpFrame;
                }
            } else {
                throw new UnsupportedOperationException(
                        "unsupported message type: " + StringUtil.simpleClassName(msg));
            }
            return msg;
        }

        void writeWithoutCheckChannelState(Object msg, ChannelPromise promise) {
            try {
                msg = filterMsg(msg);
            } catch (UnsupportedOperationException e) {
                ReferenceCountUtil.release(msg);
                promise.setFailure(e);
                return;
            }

            boolean wasFinSent = QuicheQuicStreamChannel.this.finSent;
            boolean mayNeedWritabilityUpdate = false;
            try {
                int res = write0(msg);
                if (res > 0) {
                    ReferenceCountUtil.release(msg);
                    promise.setSuccess();
                    mayNeedWritabilityUpdate = capacity == 0;
                } else if (res == 0 || res == Quiche.QUICHE_ERR_DONE) {
                    // Touch the message to make things easier in terms of debugging buffer leaks.
                    ReferenceCountUtil.touch(msg);
                    queue.add(msg, promise);
                    mayNeedWritabilityUpdate = true;
                } else if (res == Quiche.QUICHE_ERR_STREAM_STOPPED) {
                    throw new ChannelOutputShutdownException("STOP_SENDING frame received");
                } else {
                    throw Quiche.convertToException(res);
                }
            } catch (Exception e) {
                ReferenceCountUtil.release(msg);
                promise.setFailure(e);
                mayNeedWritabilityUpdate = capacity == 0;
            } finally {
                if (mayNeedWritabilityUpdate) {
                    updateWritabilityIfNeeded(false);
                }
                closeIfNeeded(wasFinSent);
            }
        }

        private int write0(Object msg) throws Exception {
            if (type() == QuicStreamType.UNIDIRECTIONAL && !isLocalCreated()) {
                throw new UnsupportedOperationException(
                        "Writes on non-local created streams that are unidirectional are not supported");
            }
            if (finSent) {
                throw new ChannelOutputShutdownException("Fin was sent already");
            }

            final boolean fin;
            ByteBuf buffer;
            if (msg instanceof ByteBuf) {
                fin = false;
                buffer = (ByteBuf) msg;
            } else {
                QuicStreamFrame frame = (QuicStreamFrame) msg;
                fin = frame.hasFin();
                buffer = frame.content();
            }

            boolean readable = buffer.isReadable();
            if (!fin && !readable) {
                return 1;
            }

            boolean sendSomething = false;
            try {
                do {
                    int res = parent().streamSend(streamId(), buffer, fin);

                    // Update the capacity as well.
                    long cap = parent.streamCapacity(streamId());
                    if (cap >= 0) {
                        capacity = cap;
                    }
                    if (res < 0) {
                        return res;
                    }
                    if (readable && res == 0) {
                        return 0;
                    }
                    sendSomething = true;
                    buffer.skipBytes(res);
                } while (buffer.isReadable());

                if (fin) {
                    finSent = true;
                    outputShutdown = true;
                }
                return 1;
            } finally {
                // stream_send 后需 connectionSendAndFlush，否则报文可能滞留在 Quiche 连接缓冲
                if (sendSomething) {
                    parent.connectionSendAndFlush();
                }
            }
        }

        @Override
        public void flush() {
            assert eventLoop().inEventLoop();
            // 流级 flush 为空操作，由父连接统一发送
        }

        @Override
        public ChannelPromise voidPromise() {
            assert eventLoop().inEventLoop();
            return voidPromise;
        }

        @Override
        @Nullable
        public ChannelOutboundBuffer outboundBuffer() {
            return null;
        }

        private void closeOnRead(ChannelPipeline pipeline, boolean readFrames) {
            if (readFrames && finReceived && finSent) {
                close(voidPromise());
            } else if (config.isAllowHalfClosure()) {
                if (finReceived) {
                    // If we receive a fin there will be no more data to read so we need to fire both events
                    // to be consistent with other transports.
                    pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
                    pipeline.fireUserEventTriggered(ChannelInputShutdownReadComplete.INSTANCE);
                    if (finSent) {
                        // This was an unidirectional stream which means as soon as we received FIN and sent a FIN
                        // we need close the connection.
                        close(voidPromise());
                    }
                }
            } else {
                // This was an unidirectional stream which means as soon as we received FIN we need
                // close the connection.
                close(voidPromise());
            }
        }

        private void handleReadException(ChannelPipeline pipeline, @Nullable ByteBuf byteBuf, Throwable cause,
                                         @SuppressWarnings("deprecation") RecvByteBufAllocator.Handle allocHandle,
                                         boolean readFrames) {
            if (byteBuf != null) {
                if (byteBuf.isReadable()) {
                    pipeline.fireChannelRead(byteBuf);
                } else {
                    byteBuf.release();
                }
            }

            readComplete(allocHandle, pipeline);
            pipeline.fireExceptionCaught(cause);
            if (finReceived) {
                closeOnRead(pipeline, readFrames);
            }
        }

        void recv() {
            assert eventLoop().inEventLoop();
            if (inRecv) {
                // 防止 read 重入 recv
                return;
            }

            inRecv = true;
            try {
                ChannelPipeline pipeline = pipeline();
                QuicheQuicStreamChannelConfig config = (QuicheQuicStreamChannelConfig) config();
                // 读路径始终需要直接缓冲供 JNI 写入
                DirectIoByteBufAllocator allocator = config.allocator;
                @SuppressWarnings("deprecation")
                RecvByteBufAllocator.Handle allocHandle = this.recvBufAllocHandle();
                boolean readFrames = config.isReadFrames();

                // 在 readPending 且 readable 时循环读取
                while (active && readPending && readable) {
                    allocHandle.reset(config);
                    ByteBuf byteBuf = null;
                    QuicheQuicChannel parent = parent();
                    // 迭代过程中流可能已结束，避免对已 FIN 流继续 recv
                    boolean readCompleteNeeded = false;
                    boolean continueReading = true;
                    try {
                        while (!finReceived && continueReading) {
                            byteBuf = allocHandle.allocate(allocator);
                            allocHandle.attemptedBytesRead(byteBuf.writableBytes());
                            QuicheQuicChannel.StreamRecvResult result = parent.streamRecv(streamId(), byteBuf);
                            switch (result) {
                                case DONE:
                                    // 无更多数据可读
                                    readable = false;
                                    break;
                                case FIN:
                                    // 收到 FIN 后标记不可读并设置 inputShutdown
                                    readable = false;
                                    finReceived = true;
                                    inputShutdown = true;
                                    break;
                                case OK:
                                    break;
                                default:
                                    throw new Error("Unexpected StreamRecvResult: " + result);
                            }
                            allocHandle.lastBytesRead(byteBuf.readableBytes());
                            if (allocHandle.lastBytesRead() <= 0) {
                                byteBuf.release();
                                if (finReceived && readFrames) {
                                    // If we read QuicStreamFrames we should fire an frame through the pipeline
                                    // with an empty buffer but the fin flag set to true.
                                    byteBuf = Unpooled.EMPTY_BUFFER;
                                } else {
                                    byteBuf = null;
                                    break;
                                }
                            }
                            // 已成功读取一条消息
                            allocHandle.incMessagesRead(1);
                            readCompleteNeeded = true;

                            // fireChannelRead 前清除 readPending，允许用户在回调中再次 read
                            readPending = false;

                            if (readFrames) {
                                pipeline.fireChannelRead(new DefaultQuicStreamFrame(byteBuf, finReceived));
                            } else {
                                pipeline.fireChannelRead(byteBuf);
                            }
                            byteBuf = null;
                            continueReading = allocHandle.continueReading();
                        }

                        if (readCompleteNeeded) {
                            readComplete(allocHandle, pipeline);
                        }
                        if (finReceived) {
                            readable = false;
                            closeOnRead(pipeline, readFrames);
                        }
                    } catch (Throwable cause) {
                        readable = false;
                        handleReadException(pipeline, byteBuf, cause, allocHandle, readFrames);
                    }
                }
            } finally {
                // 退出 recv 时清除 inRecv 标志
                inRecv = false;
                removeStreamFromParent();
            }
        }

        // 一次 read 批次结束，触发 fireChannelReadComplete
        private void readComplete(@SuppressWarnings("deprecation") RecvByteBufAllocator.Handle allocHandle,
                                  ChannelPipeline pipeline) {
            allocHandle.readComplete();
            pipeline.fireChannelReadComplete();
        }
    }
}
