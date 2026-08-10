/*
 * Copyright 2012 The Netty Project
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
package io.netty.channel.nio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.EventLoop;
import io.netty.channel.IoEvent;
import io.netty.channel.IoEventLoop;
import io.netty.channel.IoEventLoopGroup;
import io.netty.channel.IoRegistration;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for {@link Channel} implementations which use a Selector based approach.
 * <p>基于 {@link java.nio.channels.Selector} 的 {@link Channel} 抽象基类，管理注册、connect、读 interest 与 {@link NioIoHandle} 事件分发。</p>
 */
public abstract class AbstractNioChannel extends AbstractChannel {

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(AbstractNioChannel.class);

    /** 底层 JDK {@link SelectableChannel} */
    private final SelectableChannel ch;
    /** 读 interest 的整型值（与 {@link #readOps} 对应） */
    protected final int readInterestOp;
    /** 读操作的 {@link NioIoOps} 常量 */
    protected final NioIoOps readOps;
    /** 在 {@link IoEventLoop} 上的 I/O 注册句柄 */
    volatile IoRegistration registration;
    /** 用户是否已调用 read 且尚未完成读循环 */
    boolean readPending;
    /** 在 EventLoop 上清除 readPending 并移除读 interest 的任务 */
    private final Runnable clearReadPendingRunnable = new Runnable() {
        @Override
        public void run() {
            clearReadPending0();
        }
    };

    /**
     * The future of the current connection attempt.  If not null, subsequent
     * connection attempts will fail.
     * <p>当前 connect 的 Promise；非 null 时拒绝新的 connect。</p>
     */
    private ChannelPromise connectPromise;
    /** connect 超时调度 Future */
    private Future<?> connectTimeoutFuture;
    /** 正在连接的远程地址（用于超时/异常消息） */
    private SocketAddress requestedRemoteAddress;

    /**
     * Create a new instance
     * <p>使用整型 readOps 构造，内部转换为 {@link NioIoOps}。</p>
     *
     * @param parent            the parent {@link Channel} by which this instance was created. May be {@code null}
     * @param ch                the underlying {@link SelectableChannel} on which it operates
     * @param readOps           the ops to set to receive data from the {@link SelectableChannel}
     */
    protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readOps) {
        this(parent, ch, NioIoOps.valueOf(readOps));
    }

    protected AbstractNioChannel(Channel parent, SelectableChannel ch, NioIoOps readOps) {
        super(parent);
        this.ch = ch;
        this.readInterestOp = ObjectUtil.checkNotNull(readOps, "readOps").value;
        this.readOps = readOps;
        try {
            ch.configureBlocking(false);
        } catch (IOException e) {
            try {
                ch.close();
            } catch (IOException e2) {
                logger.warn(
                            "Failed to close a partially initialized socket.", e2);
            }

            throw new ChannelException("Failed to enter non-blocking mode.", e);
        }
    }

    protected void addAndSubmit(NioIoOps addOps) {
        int interestOps = selectionKey().interestOps();
        if (!addOps.isIncludedIn(interestOps)) {
            try {
                registration().submit(NioIoOps.valueOf(interestOps).with(addOps));
            } catch (Exception e) {
                throw new ChannelException(e);
            }
        }
    }

    protected void removeAndSubmit(NioIoOps removeOps) {
        int interestOps = selectionKey().interestOps();
        if (removeOps.isIncludedIn(interestOps)) {
            try {
                registration().submit(NioIoOps.valueOf(interestOps).without(removeOps));
            } catch (Exception e) {
                throw new ChannelException(e);
            }
        }
    }

    @Override
    public boolean isOpen() {
        return ch.isOpen();
    }

    @Override
    public NioUnsafe unsafe() {
        return (NioUnsafe) super.unsafe();
    }

    protected SelectableChannel javaChannel() {
        return ch;
    }

    /**
     * Return the current {@link SelectionKey}
     * <p>返回当前 {@link SelectionKey}（已废弃，请使用 {@link #registration}）。</p>
     *
     * @deprecated use {@link #registration}.
     */
    @Deprecated
    protected SelectionKey selectionKey() {
        return registration().attachment();
    }

    @SuppressWarnings("unchecked")
    protected IoRegistration registration() {
        assert registration != null;
        return registration;
    }

    /**
     * @deprecated No longer supported.
     * No longer supported.
     * <p>已废弃：请改用 {@link #clearReadPending()}。</p>
     */
    @Deprecated
    protected boolean isReadPending() {
        return readPending;
    }

    /**
     * @deprecated Use {@link #clearReadPending()} if appropriate instead.
     * No longer supported.
     * <p>已废弃：请改用 {@link #clearReadPending()}。</p>
     */
    @Deprecated
    protected void setReadPending(final boolean readPending) {
        if (isRegistered()) {
            EventLoop eventLoop = eventLoop();
            if (eventLoop.inEventLoop()) {
                setReadPending0(readPending);
            } else {
                eventLoop.execute(new Runnable() {
                    @Override
                    public void run() {
                        setReadPending0(readPending);
                    }
                });
            }
        } else {
            // 尚未 register 时仅清 boolean，避免 selectionKey 断言失败
            this.readPending = readPending;
        }
    }

    /**
     * Set read pending to {@code false}.
     * <p>清除 readPending 并从 interest 中移除读 ops。</p>
     */
    protected final void clearReadPending() {
        if (isRegistered()) {
            EventLoop eventLoop = eventLoop();
            if (eventLoop.inEventLoop()) {
                clearReadPending0();
            } else {
                eventLoop.execute(clearReadPendingRunnable);
            }
        } else {
            // channel 初始化阶段尚未 register，仅清标志位
            readPending = false;
        }
    }

    private void setReadPending0(boolean readPending) {
        this.readPending = readPending;
        if (!readPending) {
            ((AbstractNioUnsafe) unsafe()).removeReadOp();
        }
    }

    private void clearReadPending0() {
        readPending = false;
        ((AbstractNioUnsafe) unsafe()).removeReadOp();
    }

    /**
     * Special {@link Unsafe} sub-type which allows to access the underlying {@link SelectableChannel}
     * <p>扩展 {@link Unsafe}，暴露 NIO channel 与 connect/read/flush 语义。</p>
     */
    public interface NioUnsafe extends Unsafe {
        /**
         * Return underlying {@link SelectableChannel}
         * <p>返回底层 {@link SelectableChannel}。</p>
         */
        SelectableChannel ch();

        /**
         * Finish connect
         * <p>在 OP_CONNECT 就绪后完成连接并触发 active。</p>
         */
        void finishConnect();

        /**
         * Read from underlying {@link SelectableChannel}
         * <p>从底层 channel 读取并入站 fireChannelRead。</p>
         */
        void read();

        /** 强制 flush，忽略 pending flush 优化 */
        void forceFlush();
    }

    protected abstract class AbstractNioUnsafe extends AbstractUnsafe implements NioUnsafe, NioIoHandle {
        @Override
        public void close() {
            close(voidPromise());
        }

        @Override
        public SelectableChannel selectableChannel() {
            return ch();
        }

        Channel channel() {
            return AbstractNioChannel.this;
        }

        protected final void removeReadOp() {
            IoRegistration registration = registration();
            // key 可能在 deregister 时已 cancel，先校验有效性
            // See https://github.com/netty/netty/issues/2104
            if (!registration.isValid()) {
                return;
            }
            removeAndSubmit(readOps);
        }

        @Override
        public final SelectableChannel ch() {
            return javaChannel();
        }

        @Override
        public final void connect(
                final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
            // 非阻塞 connect：Promise 可取消，勿标记为不可取消
            if (promise.isDone() || !ensureOpen(promise)) {
                return;
            }

            try {
                if (connectPromise != null) {
                    // 已有 connect 进行中
                    throw new ConnectionPendingException();
                }

                boolean wasActive = isActive();
                if (doConnect(remoteAddress, localAddress)) {
                    fulfillConnectPromise(promise, wasActive);
                } else {
                    connectPromise = promise;
                    requestedRemoteAddress = remoteAddress;

                    // 调度 connect 超时
                    final int connectTimeoutMillis = config().getConnectTimeoutMillis();
                    if (connectTimeoutMillis > 0) {
                        connectTimeoutFuture = eventLoop().schedule(new Runnable() {
                            @Override
                            public void run() {
                                ChannelPromise connectPromise = AbstractNioChannel.this.connectPromise;
                                if (connectPromise != null && !connectPromise.isDone()
                                        && connectPromise.tryFailure(new ConnectTimeoutException(
                                                "connection timed out after " + connectTimeoutMillis + " ms: " +
                                                        remoteAddress))) {
                                    close(voidPromise());
                                }
                            }
                        }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
                    }

                    promise.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) {
                            // If the connect future is cancelled we also cancel the timeout and close the
                            // underlying socket.
                            if (future.isCancelled()) {
                                if (connectTimeoutFuture != null) {
                                    connectTimeoutFuture.cancel(false);
                                }
                                connectPromise = null;
                                close(voidPromise());
                            }
                        }
                    });
                }
            } catch (Throwable t) {
                promise.tryFailure(annotateConnectException(t, remoteAddress));
                closeIfClosed();
            }
        }

        private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive) {
            if (promise == null) {
                // Closed via cancellation and the promise has been notified already.
                return;
            }

            // Get the state as trySuccess() may trigger an ChannelFutureListener that will close the Channel.
            // We still need to ensure we call fireChannelActive() in this case.
            boolean active = isActive();

            // trySuccess() will return false if a user cancelled the connection attempt.
            boolean promiseSet = promise.trySuccess();

            // Regardless if the connection attempt was cancelled, channelActive() event should be triggered,
            // because what happened is what happened.
            if (!wasActive && active) {
                pipeline().fireChannelActive();
            }

            // If a user cancelled the connection attempt, close the channel, which is followed by channelInactive().
            if (!promiseSet) {
                close(voidPromise());
            }
        }

        private void fulfillConnectPromise(ChannelPromise promise, Throwable cause) {
            if (promise == null) {
                // Closed via cancellation and the promise has been notified already.
                return;
            }

            // Use tryFailure() instead of setFailure() to avoid the race against cancel().
            promise.tryFailure(cause);
            closeIfClosed();
        }

        @Override
        public final void finishConnect() {
            // Note this method is invoked by the event loop only if the connection attempt was
            // neither cancelled nor timed out.

            assert eventLoop().inEventLoop();

            try {
                boolean wasActive = isActive();
                doFinishConnect();
                fulfillConnectPromise(connectPromise, wasActive);
            } catch (Throwable t) {
                fulfillConnectPromise(connectPromise, annotateConnectException(t, requestedRemoteAddress));
            } finally {
                // Check for null as the connectTimeoutFuture is only created if a connectTimeoutMillis > 0 is used
                // See https://github.com/netty/netty/issues/1770
                if (connectTimeoutFuture != null) {
                    connectTimeoutFuture.cancel(false);
                }
                connectPromise = null;
            }
        }

        @Override
        protected final void flush0() {
            // 仅当无 pending flush 时立即 flush；否则由 forceFlush 在 OP_WRITE 就绪时处理
            if (!isFlushPending()) {
                super.flush0();
            }
        }

        @Override
        public final void forceFlush() {
            // 直接 super.flush0() 强制立即写出
            super.flush0();
        }

        private boolean isFlushPending() {
            IoRegistration registration = registration();
            return registration.isValid() && NioIoOps.WRITE.isIncludedIn((
                    (SelectionKey) registration.attachment()).interestOps());
        }

        @Override
        public void handle(IoRegistration registration, IoEvent event) {
            try {
                NioIoEvent nioEvent = (NioIoEvent) event;
                NioIoOps nioReadyOps = nioEvent.ops();
                // 须先 finishConnect，否则 read/write 可能抛 NotYetConnectedException
                if (nioReadyOps.contains(NioIoOps.CONNECT)) {
                    // 移除 OP_CONNECT，否则 select 可能永不阻塞
                    // See https://github.com/netty/netty/issues/924
                    removeAndSubmit(NioIoOps.CONNECT);

                    unsafe().finishConnect();
                }

                // 优先处理 OP_WRITE，可能写出队列数据并释放内存
                if (nioReadyOps.contains(NioIoOps.WRITE)) {
                    // forceFlush 会在无数据可写时清除 OP_WRITE
                    forceFlush();
                }

                // readOps 为 0 时也尝试 read，规避部分 JDK 自旋 bug
                if (nioReadyOps.contains(NioIoOps.READ_AND_ACCEPT) || nioReadyOps.equals(NioIoOps.NONE)) {
                    read();
                }
            } catch (CancelledKeyException ignored) {
                close(voidPromise());
            }
        }
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof IoEventLoop && ((IoEventLoopGroup) loop).isCompatible(AbstractNioUnsafe.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doRegister(ChannelPromise promise) {
        assert registration == null;
        ((IoEventLoop) eventLoop()).register((AbstractNioUnsafe) unsafe()).addListener(f -> {
            if (f.isSuccess()) {
                registration = (IoRegistration) f.getNow();
                promise.setSuccess();
            } else {
                promise.setFailure(f.cause());
            }
        });
    }

    @Override
    protected void doDeregister() throws Exception {
        IoRegistration registration = this.registration;
        if (registration != null) {
            this.registration = null;
            registration.cancel();
        }
    }

    @Override
    protected void doBeginRead() throws Exception {
        // 用户调用了 Channel.read() / ChannelHandlerContext.read()
        IoRegistration registration = this.registration;
        if (registration == null || !registration.isValid()) {
            return;
        }

        readPending = true;

        addAndSubmit(readOps);
    }

    /**
     * Connect to the remote peer
     * <p>发起非阻塞 connect；返回 {@code true} 表示已同步连接完成。</p>
     */
    protected abstract boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception;

    /**
     * Finish the connect
     * <p>在 OP_CONNECT 就绪后完成 JDK channel 的 connect 握手。</p>
     */
    protected abstract void doFinishConnect() throws Exception;

    /**
     * Returns an off-heap copy of the specified {@link ByteBuf}, and releases the original one.
     * Note that this method does not create an off-heap copy if the allocation / deallocation cost is too high,
     * but just returns the original {@link ByteBuf}..
     * <p>将 {@link ByteBuf} 复制为堆外/direct 缓冲并释放原 buf；分配成本过高时直接返回原 buf。</p>
     */
    protected final ByteBuf newDirectBuffer(ByteBuf buf) {
        final int readableBytes = buf.readableBytes();
        if (readableBytes == 0) {
            ReferenceCountUtil.safeRelease(buf);
            return Unpooled.EMPTY_BUFFER;
        }

        final ByteBufAllocator alloc = alloc();
        if (alloc.isDirectBufferPooled()) {
            ByteBuf directBuf = alloc.directBuffer(readableBytes);
            directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
            ReferenceCountUtil.safeRelease(buf);
            return directBuf;
        }

        final ByteBuf directBuf = ByteBufUtil.threadLocalDirectBuffer();
        if (directBuf != null) {
            directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
            ReferenceCountUtil.safeRelease(buf);
            return directBuf;
        }

        // 非池化 direct 分配/释放代价高，放弃复制
        return buf;
    }

    /**
     * Returns an off-heap copy of the specified {@link ByteBuf}, and releases the specified holder.
     * The caller must ensure that the holder releases the original {@link ByteBuf} when the holder is released by
     * this method.  Note that this method does not create an off-heap copy if the allocation / deallocation cost is
     * too high, but just returns the original {@link ByteBuf}..
     * <p>复制为 direct 缓冲并释放 holder；成本过高时保留原 buf 并释放 holder。</p>
     */
    protected final ByteBuf newDirectBuffer(ReferenceCounted holder, ByteBuf buf) {
        final int readableBytes = buf.readableBytes();
        if (readableBytes == 0) {
            ReferenceCountUtil.safeRelease(holder);
            return Unpooled.EMPTY_BUFFER;
        }

        final ByteBufAllocator alloc = alloc();
        if (alloc.isDirectBufferPooled()) {
            ByteBuf directBuf = alloc.directBuffer(readableBytes);
            directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
            ReferenceCountUtil.safeRelease(holder);
            return directBuf;
        }

        final ByteBuf directBuf = ByteBufUtil.threadLocalDirectBuffer();
        if (directBuf != null) {
            directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
            ReferenceCountUtil.safeRelease(holder);
            return directBuf;
        }

        // 非池化 direct 分配/释放代价高，放弃复制
        if (holder != buf) {
            // 确保 holder.release() 以释放除内容外的其他资源
            buf.retain();
            ReferenceCountUtil.safeRelease(holder);
        }

        return buf;
    }

    @Override
    protected void doClose() throws Exception {
        ChannelPromise promise = connectPromise;
        if (promise != null) {
            // Use tryFailure() instead of setFailure() to avoid the race against cancel().
            promise.tryFailure(new ClosedChannelException());
            connectPromise = null;
        }

        Future<?> future = connectTimeoutFuture;
        if (future != null) {
            future.cancel(false);
            connectTimeoutFuture = null;
        }
    }
}
