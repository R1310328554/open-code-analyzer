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
package io.netty.channel;

import io.netty.channel.socket.ChannelOutputShutdownEvent;
import io.netty.channel.socket.ChannelOutputShutdownException;
import io.netty.util.DefaultAttributeMap;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * A skeletal {@link Channel} implementation.
 */
public abstract class AbstractChannel extends DefaultAttributeMap implements Channel {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractChannel.class);

    /** 父 Channel（ServerChannel 接受的子连接） */
    private final Channel parent;
    /** 全局唯一 Channel 标识 */
    private final ChannelId id;
    /** 底层 IO 操作入口 */
    private final Unsafe unsafe;
    /** 关联的事件处理管道 */
    private final DefaultChannelPipeline pipeline;
    private final VoidChannelPromise unsafeVoidPromise = new VoidChannelPromise(this, false);
    private final CloseFuture closeFuture = new CloseFuture(this);

    /** 缓存的本地地址 */
    private volatile SocketAddress localAddress;
    /** 缓存的远程地址 */
    private volatile SocketAddress remoteAddress;
    /** 注册到的 EventLoop */
    private volatile EventLoop eventLoop;
    /** 是否已注册到 EventLoop */
    private volatile boolean registered;
    /** 是否已发起 close 流程 */
    private boolean closeInitiated;
    /** 首次导致关闭的原因，用于后续写/flush 失败信息 */
    private Throwable initialCloseCause;

    /** {@link #toString()} 结果缓存，随 active 状态失效 */
    private boolean strValActive;
    private String strVal;

    /**
     * 创建新实例
     *
     * @param parent 父 Channel；无父节点时为 {@code null}
     */
    protected AbstractChannel(Channel parent) {
        this.parent = parent;
        id = newId();
        unsafe = newUnsafe();
        pipeline = newChannelPipeline();
    }

    /**
     * 创建新实例
     *
     * @param parent 父 Channel；无父节点时为 {@code null}
     */
    protected AbstractChannel(Channel parent, ChannelId id) {
        this.parent = parent;
        this.id = id;
        unsafe = newUnsafe();
        pipeline = newChannelPipeline();
    }

    protected final int maxMessagesPerWrite() {
        ChannelConfig config = config();
        if (config instanceof DefaultChannelConfig) {
            return ((DefaultChannelConfig) config).getMaxMessagesPerWrite();
        }
        Integer value = config.getOption(ChannelOption.MAX_MESSAGES_PER_WRITE);
        if (value == null) {
            return Integer.MAX_VALUE;
        }
        return value;
    }

    @Override
    public final ChannelId id() {
        return id;
    }

    /**
     * 返回新 {@link DefaultChannelId}；子类可覆盖以自定义 ID Subclasses may override this method to assign custom
     * {@link ChannelId}s to {@link Channel}s that use the {@link AbstractChannel#AbstractChannel(Channel)} constructor.
     */
    protected ChannelId newId() {
        return DefaultChannelId.newInstance();
    }

    /**
     * 创建并返回 {@link DefaultChannelPipeline}
     */
    protected DefaultChannelPipeline newChannelPipeline() {
        return new DefaultChannelPipeline(this);
    }

    @Override
    public Channel parent() {
        return parent;
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
    }

    @Override
    public EventLoop eventLoop() {
        EventLoop eventLoop = this.eventLoop;
        if (eventLoop == null) {
            throw new IllegalStateException("channel not registered to an event loop");
        }
        return eventLoop;
    }

    @Override
    public SocketAddress localAddress() {
        SocketAddress localAddress = this.localAddress;
        if (localAddress == null) {
            try {
                this.localAddress = localAddress = unsafe().localAddress();
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                // Windows 上已关闭 socket 查询地址可能失败
                return null;
            }
        }
        return localAddress;
    }

    /**
     * @deprecated 无使用场景
     */
    @Deprecated
    protected void invalidateLocalAddress() {
        localAddress = null;
    }

    @Override
    public SocketAddress remoteAddress() {
        SocketAddress remoteAddress = this.remoteAddress;
        if (remoteAddress == null) {
            try {
                this.remoteAddress = remoteAddress = unsafe().remoteAddress();
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                // Windows 上已关闭 socket 查询地址可能失败
                return null;
            }
        }
        return remoteAddress;
    }

    /**
     * @deprecated 无使用场景
     */
    @Deprecated
    protected void invalidateRemoteAddress() {
        remoteAddress = null;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public ChannelFuture closeFuture() {
        return closeFuture;
    }

    @Override
    public Unsafe unsafe() {
        return unsafe;
    }

    /**
     * 创建贯穿 Channel 生命周期的 {@link AbstractUnsafe} 实例 of the {@link Channel}
     */
    protected abstract AbstractUnsafe newUnsafe();

    /**
     * 基于 {@link ChannelId} 的 hashCode
     */
    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    /**
     * Channel 身份比较：仅当 {@code this == o} 时为 {@code true}
     * with this channel (i.e: {@code this == o}).
     */
    @Override
    public final boolean equals(Object o) {
        return this == o;
    }

    @Override
    public final int compareTo(Channel o) {
        if (this == o) {
            return 0;
        }

        return id().compareTo(o.id());
    }

    /**
     * 返回含 ID、本地/远程地址及 active 状态的字符串，便于日志识别  The returned
     * string contains the {@linkplain #hashCode() ID}, {@linkplain #localAddress() local address},
     * and {@linkplain #remoteAddress() remote address} of this channel for
     * easier identification.
     */
    @Override
    public String toString() {
        boolean active = isActive();
        if (strValActive == active && strVal != null) {
            return strVal;
        }

        SocketAddress remoteAddr = remoteAddress();
        SocketAddress localAddr = localAddress();
        if (remoteAddr != null) {
            StringBuilder buf = new StringBuilder(96)
                .append("[id: 0x")
                .append(id.asShortText())
                .append(", L:")
                .append(localAddr)
                .append(active? " - " : " ! ")
                .append("R:")
                .append(remoteAddr)
                .append(']');
            strVal = buf.toString();
        } else if (localAddr != null) {
            StringBuilder buf = new StringBuilder(64)
                .append("[id: 0x")
                .append(id.asShortText())
                .append(", L:")
                .append(localAddr)
                .append(']');
            strVal = buf.toString();
        } else {
            StringBuilder buf = new StringBuilder(16)
                .append("[id: 0x")
                .append(id.asShortText())
                .append(']');
            strVal = buf.toString();
        }

        strValActive = active;
        return strVal;
    }

    @Override
    public final ChannelPromise voidPromise() {
        return pipeline.voidPromise();
    }

    /**
     * 子类必须扩展并使用的 {@link Unsafe} 实现，处理注册、bind、读写与关闭。
     */
    protected abstract class AbstractUnsafe implements Unsafe {

        private volatile ChannelOutboundBuffer outboundBuffer = new ChannelOutboundBuffer(AbstractChannel.this);
        private RecvByteBufAllocator.Handle recvHandle;
        private boolean inFlush0;
        /** {@code true} 表示从未注册过，用于控制是否触发 channelActive */
        private boolean neverRegistered = true;

        private void assertEventLoop() {
            assert !registered || eventLoop.inEventLoop();
        }

        @Override
        public RecvByteBufAllocator.Handle recvBufAllocHandle() {
            if (recvHandle == null) {
                recvHandle = config().getRecvByteBufAllocator().newHandle();
            }
            return recvHandle;
        }

        @Override
        public final ChannelOutboundBuffer outboundBuffer() {
            return outboundBuffer;
        }

        @Override
        public final SocketAddress localAddress() {
            return localAddress0();
        }

        @Override
        public final SocketAddress remoteAddress() {
            return remoteAddress0();
        }

        @Override
        public final void register(EventLoop eventLoop, final ChannelPromise promise) {
            ObjectUtil.checkNotNull(eventLoop, "eventLoop");
            if (isRegistered()) {
                promise.setFailure(new IllegalStateException("registered to an event loop already"));
                return;
            }
            if (!isCompatible(eventLoop)) {
                promise.setFailure(
                        new IllegalStateException("incompatible event loop type: " + eventLoop.getClass().getName()));
                return;
            }

            AbstractChannel.this.eventLoop = eventLoop;

            // 清除旧 EventLoop 注册遗留的 contextExecutor 缓存
            AbstractChannelHandlerContext context = pipeline.tail;
            do {
                context.contextExecutor = null;
                context = context.prev;
            } while (context != null);

            if (eventLoop.inEventLoop()) {
                register0(promise);
            } else {
                try {
                    eventLoop.execute(new Runnable() {
                        @Override
                        public void run() {
                            register0(promise);
                        }
                    });
                } catch (Throwable t) {
                    logger.warn(
                            "Force-closing a channel whose registration task was not accepted by an event loop: {}",
                            AbstractChannel.this, t);
                    closeForcibly();
                    closeFuture.setClosed();
                    safeSetFailure(promise, t);
                }
            }
        }

        private void register0(ChannelPromise promise) {
            // 异步 register 路径：再次检查 Channel 是否仍打开 in the mean time when the register
            // call was outside of the eventLoop
            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }
            ChannelPromise registerPromise = newPromise();
            boolean firstRegistration = neverRegistered;
            registerPromise.addListener(future -> {
                if (future.isSuccess()) {
                    neverRegistered = false;
                    registered = true;

                    // 先完成 handlerAdded，再通知 promise，避免 listener 中过早触发 pipeline 事件 This is needed as the
                    // user may already fire events through the pipeline in the ChannelFutureListener.
                    pipeline.invokeHandlerAddedIfNeeded();

                    safeSetSuccess(promise);
                    pipeline.fireChannelRegistered();
                    // 仅首次注册时 fire channelActive，避免 deregister 再 register 重复触发 This prevents firing
                    // multiple channel actives if the channel is deregistered and re-registered.
                    if (isActive()) {
                        if (firstRegistration) {
                            pipeline.fireChannelActive();
                        } else if (config().isAutoRead()) {
                            // 非首次注册且 autoRead 开启：需重新 beginRead 以处理入站数据（见 netty#4805） This means we need to
                            // begin read again so that we process inbound data.
                            //
                            // See https://github.com/netty/netty/issues/4805
                            beginRead();
                        }
                    }
                } else {
                    // 注册失败时直接关闭 Channel，避免 FD 泄漏
                    close(newPromise());
                    closeFuture.setClosed();
                    safeSetFailure(promise, future.cause());
                }
            });
            doRegister(registerPromise);
        }

        @Override
        public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {
            assertEventLoop();

            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }

            // 非 root 用户 bind 非通配地址时无法接收广播（见 netty#576）
            if (Boolean.TRUE.equals(config().getOption(ChannelOption.SO_BROADCAST)) &&
                localAddress instanceof InetSocketAddress &&
                !((InetSocketAddress) localAddress).getAddress().isAnyLocalAddress() &&
                !PlatformDependent.isWindows() && !PlatformDependent.maybeSuperUser()) {
                // 仍按用户请求 bind，但记录警告 can't receive a
                // broadcast packet on *nix if the socket is bound on non-wildcard address.
                logger.warn(
                        "A non-root user can't receive a broadcast packet if the socket " +
                        "is not bound to a wildcard address; binding to a non-wildcard " +
                        "address (" + localAddress + ") anyway as requested.");
            }

            boolean wasActive = isActive();
            try {
                doBind(localAddress);
            } catch (Throwable t) {
                safeSetFailure(promise, t);
                closeIfClosed();
                return;
            }

            if (!wasActive && isActive()) {
                invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        pipeline.fireChannelActive();
                    }
                });
            }

            safeSetSuccess(promise);
        }

        @Override
        public final void disconnect(final ChannelPromise promise) {
            assertEventLoop();

            if (!promise.setUncancellable()) {
                return;
            }

            boolean wasActive = isActive();
            try {
                doDisconnect();
                // disconnect 后清空缓存地址
                remoteAddress = null;
                localAddress = null;
            } catch (Throwable t) {
                safeSetFailure(promise, t);
                closeIfClosed();
                return;
            }

            if (wasActive && !isActive()) {
                invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        pipeline.fireChannelInactive();
                    }
                });
            }

            safeSetSuccess(promise);
            closeIfClosed(); // doDisconnect() might have closed the channel
        }

        @Override
        public void close(final ChannelPromise promise) {
            assertEventLoop();

            ClosedChannelException closedChannelException =
                    StacklessClosedChannelException.newInstance(AbstractChannel.class, "close(ChannelPromise)");
            close(promise, closedChannelException, closedChannelException);
        }

        /**
         * 关闭 {@link Channel} 的输出侧：清空 {@link ChannelOutboundBuffer} 并禁止后续写入
         * For example this will clean up the {@link ChannelOutboundBuffer} and not allow any more writes.
         */
        public final void shutdownOutput(final ChannelPromise promise) {
            assertEventLoop();
            shutdownOutput(promise, null);
        }

        /**
         * 关闭 {@link Channel} 的输出侧：清空 {@link ChannelOutboundBuffer} 并禁止后续写入
         * For example this will clean up the {@link ChannelOutboundBuffer} and not allow any more writes.
         * @param cause 可选的关闭原因
         */
        private void shutdownOutput(final ChannelPromise promise, Throwable cause) {
            if (!promise.setUncancellable()) {
                return;
            }

            final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            if (outboundBuffer == null) {
                promise.setFailure(new ClosedChannelException());
                return;
            }
            this.outboundBuffer = null; // 禁止继续向 outboundBuffer 追加消息与 flush and flushes to outboundBuffer.

            final Throwable shutdownCause = cause == null ?
                    new ChannelOutputShutdownException("Channel output shutdown") :
                    new ChannelOutputShutdownException("Channel output shutdown", cause);

            // SO_LINGER 半关闭场景：不可在此 doDeregister，须保持 FIN_WAIT2 仍能收数据（见 netty#11981）(...) to start TCP half-closure
            // we can not call doDeregister here because we should ensure this side in fin_wait2 state
            // can still receive and process the data which is send by another side in the close_wait state。
            // See https://github.com/netty/netty/issues/11981
            try {
                // The shutdown function does not block regardless of the SO_LINGER setting on the socket
                // so we don't need to use GlobalEventExecutor to execute the shutdown
                doShutdownOutput();
                promise.setSuccess();
            } catch (Throwable err) {
                promise.setFailure(err);
            } finally {
                closeOutboundBufferForShutdown(pipeline, outboundBuffer, shutdownCause);
            }
        }

        private void closeOutboundBufferForShutdown(
                ChannelPipeline pipeline, ChannelOutboundBuffer buffer, Throwable cause) {
            buffer.failFlushed(cause, false);
            buffer.close(cause, true);
            pipeline.fireUserEventTriggered(ChannelOutputShutdownEvent.INSTANCE);
        }

        protected void close(final ChannelPromise promise, final Throwable cause,
                           final ClosedChannelException closeCause) {
            if (!promise.setUncancellable()) {
                return;
            }

            if (closeInitiated) {
                if (closeFuture.isDone()) {
                    // 已完成关闭，直接成功 promise
                    safeSetSuccess(promise);
                } else if (!(promise instanceof VoidChannelPromise)) { // Only needed if no VoidChannelPromise.
                    // close 已在进行，注册 listener 在 closeFuture 完成时成功 promise and return
                    closeFuture.addListener(future -> promise.setSuccess());
                }
                return;
            }

            closeInitiated = true;

            final boolean wasActive = isActive();
            final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            this.outboundBuffer = null; // 禁止继续向 outboundBuffer 追加消息与 flush and flushes to outboundBuffer.
            Executor closeExecutor = prepareToClose();
            if (closeExecutor != null) {
                closeExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 在 closeExecutor 线程执行 doClose
                            doClose0(promise);
                        } finally {
                            // 通过 invokeLater 回到 EventLoop 执行 inactive/deregister
                            invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (outboundBuffer != null) {
                                        // 以失败完成 outboundBuffer 中排队的写请求
                                        outboundBuffer.failFlushed(cause, false);
                                        outboundBuffer.close(closeCause);
                                    }
                                    fireChannelInactiveAndDeregister(wasActive);
                                }
                            });
                        }
                    }
                });
            } else {
                try {
                    // Close the channel and fail the queued messages in all cases.
                    doClose0(promise);
                } finally {
                    if (outboundBuffer != null) {
                        // 以失败完成 outboundBuffer 中排队的写请求.
                        outboundBuffer.failFlushed(cause, false);
                        outboundBuffer.close(closeCause);
                    }
                }
                if (inFlush0) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            fireChannelInactiveAndDeregister(wasActive);
                        }
                    });
                } else {
                    fireChannelInactiveAndDeregister(wasActive);
                }
            }
        }

        private void doClose0(ChannelPromise promise) {
            try {
                doClose();
                closeFuture.setClosed();
                safeSetSuccess(promise);
            } catch (Throwable t) {
                closeFuture.setClosed();
                safeSetFailure(promise, t);
            }
        }

        private void fireChannelInactiveAndDeregister(final boolean wasActive) {
            deregister(voidPromise(), wasActive && !isActive());
        }

        @Override
        public final void closeForcibly() {
            assertEventLoop();

            try {
                doClose();
            } catch (Exception e) {
                logger.warn("Failed to close a channel.", e);
            }
        }

        @Override
        public void deregister(final ChannelPromise promise) {
            assertEventLoop();

            deregister(promise, false);
        }

        private void deregister(final ChannelPromise promise, final boolean fireChannelInactive) {
            if (!promise.setUncancellable()) {
                return;
            }

            if (!registered) {
                safeSetSuccess(promise);
                return;
            }

            // 延迟 deregister 到 EventLoop 任务，避免与 pipeline 处理跨线程（见 netty#4435） while doing processing in the ChannelPipeline,
            // we need to ensure we do the actual deregister operation later. This is needed as for example,
            // we may be in the ByteToMessageDecoder.callDecode(...) method and so still try to do processing in
            // the old EventLoop while the user already registered the Channel to a new EventLoop. Without delay,
            // the deregister operation this could lead to have a handler invoked by different EventLoop and so
            // threads.
            //
            // See:
            // https://github.com/netty/netty/issues/4435
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        doDeregister();
                    } catch (Throwable t) {
                        logger.warn("Unexpected exception occurred while deregistering a channel.", t);
                    } finally {
                        if (fireChannelInactive) {
                            pipeline.fireChannelInactive();
                        }
                        // Local/AIO 等传输 doDeregister 可能间接 close；若已关闭则跳过 channelUnregistered the deregistration of
                        // an open channel.  Their doDeregister() calls close(). Consequently,
                        // close() calls deregister() again - no need to fire channelUnregistered, so check
                        // if it was registered.
                        if (registered) {
                            registered = false;
                            pipeline.fireChannelUnregistered();
                        }
                        safeSetSuccess(promise);
                    }
                }
            });
        }

        @Override
        public final void beginRead() {
            assertEventLoop();

            try {
                doBeginRead();
            } catch (final Exception e) {
                invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        pipeline.fireExceptionCaught(e);
                    }
                });
                close(voidPromise());
            }
        }

        @Override
        public final void write(Object msg, ChannelPromise promise) {
            assertEventLoop();

            ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            if (outboundBuffer == null) {
                try {
                    // 立即 release 消息防止泄漏
                    ReferenceCountUtil.release(msg);
                } finally {
                    // outboundBuffer 为 null 表示已关闭，立即失败 promise（见 netty#2362） and so
                    // need to fail the future right away. If it is not null the handling of the rest
                    // will be done in flush0()
                    // See https://github.com/netty/netty/issues/2362
                    safeSetFailure(promise,
                            newClosedChannelException(initialCloseCause, "write(Object, ChannelPromise)"));
                }
                return;
            }

            int size;
            try {
                msg = filterOutboundMessage(msg);
                size = pipeline.estimatorHandle().size(msg);
                if (size < 0) {
                    size = 0;
                }
            } catch (Throwable t) {
                try {
                    ReferenceCountUtil.release(msg);
                } finally {
                    safeSetFailure(promise, t);
                }
                return;
            }

            outboundBuffer.addMessage(msg, size, promise);
        }

        @Override
        public final void flush() {
            assertEventLoop();

            ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            if (outboundBuffer == null) {
                return;
            }

            outboundBuffer.addFlush();
            flush0();
        }

        @SuppressWarnings("deprecation")
        protected void flush0() {
            if (inFlush0) {
                // 防止 flush0 重入
                return;
            }

            final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            if (outboundBuffer == null || outboundBuffer.isEmpty()) {
                return;
            }

            inFlush0 = true;

            // Channel 非 active 时将待写请求标记为失败
            if (!isActive()) {
                try {
                    // Check if we need to generate the exception at all.
                    if (!outboundBuffer.isEmpty()) {
                        if (isOpen()) {
                            outboundBuffer.failFlushed(new NotYetConnectedException(), true);
                        } else {
                            // 已关闭时不触发 channelWritabilityChanged
                            outboundBuffer.failFlushed(newClosedChannelException(initialCloseCause, "flush0()"), false);
                        }
                    }
                } finally {
                    inFlush0 = false;
                }
                return;
            }

            try {
                doWrite(outboundBuffer);
            } catch (Throwable t) {
                handleWriteError(t);
            } finally {
                inFlush0 = false;
            }
        }

        protected final void handleWriteError(Throwable t) {
            if (t instanceof IOException && config().isAutoClose()) {
                /**
                 * Just call {@link #close(ChannelPromise, Throwable, boolean)} here which will take care of
                 * failing all flushed messages and also ensure the actual close of the underlying transport
                 * will happen before the promises are notified.
                 *
                 * This is needed as otherwise {@link #isActive()} , {@link #isOpen()} and {@link #isWritable()}
                 * may still return {@code true} even if the channel should be closed as result of the exception.
                 */
                initialCloseCause = t;
                close(voidPromise(), t, newClosedChannelException(t, "flush0()"));
            } else {
                try {
                    shutdownOutput(voidPromise(), t);
                } catch (Throwable t2) {
                    initialCloseCause = t;
                    close(voidPromise(), t2, newClosedChannelException(t, "flush0()"));
                }
            }
        }

        private ClosedChannelException newClosedChannelException(Throwable cause, String method) {
            ClosedChannelException exception =
                    StacklessClosedChannelException.newInstance(AbstractChannel.AbstractUnsafe.class, method);
            if (cause != null) {
                exception.initCause(cause);
            }
            return exception;
        }

        @Override
        public final ChannelPromise voidPromise() {
            assertEventLoop();

            return unsafeVoidPromise;
        }

        protected final boolean ensureOpen(ChannelPromise promise) {
            if (isOpen()) {
                return true;
            }

            safeSetFailure(promise, newClosedChannelException(initialCloseCause, "ensureOpen(ChannelPromise)"));
            return false;
        }

        /**
         * 将 {@code promise} 标记为成功；已完成则记录警告  If the {@code promise} is done already, log a message.
         */
        protected final void safeSetSuccess(ChannelPromise promise) {
            if (!(promise instanceof VoidChannelPromise) && !promise.trySuccess()) {
                logger.warn("Failed to mark a promise as success because it is done already: {}", promise);
            }
        }

        /**
         * 将 {@code promise} 标记为失败；已完成则记录警告  If the {@code promise} is done already, log a message.
         */
        protected final void safeSetFailure(ChannelPromise promise, Throwable cause) {
            if (!(promise instanceof VoidChannelPromise) && !promise.tryFailure(cause)) {
                logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, cause);
            }
        }

        protected final void closeIfClosed() {
            if (isOpen()) {
                return;
            }
            close(voidPromise());
        }

        private void invokeLater(Runnable task) {
            try {
                // 出站操作触发的入站事件须延迟到 EventLoop 任务，避免同 handler 入站方法栈重叠
                // They do not trigger an inbound event immediately because an outbound operation might have been
                // triggered by another inbound event handler method.  If fired immediately, the call stack
                // will look like this for example:
                //
                //   handlerA.inboundBufferUpdated() - (1) an inbound handler method closes a connection.
                //   -> handlerA.ctx.close()
                //      -> channel.unsafe.close()
                //         -> handlerA.channelInactive() - (2) another inbound handler method called while in (1) yet
                //
                // which means the execution of two inbound handler methods of the same handler overlap undesirably.
                eventLoop().execute(task);
            } catch (RejectedExecutionException e) {
                logger.warn("Can't invoke task later as EventLoop rejected it", e);
            }
        }

        /**
         * 为连接失败异常附加远程地址信息，便于诊断
         */
        protected final Throwable annotateConnectException(Throwable cause, SocketAddress remoteAddress) {
            if (cause instanceof ConnectException) {
                return new AnnotatedConnectException((ConnectException) cause, remoteAddress);
            }
            if (cause instanceof NoRouteToHostException) {
                return new AnnotatedNoRouteToHostException((NoRouteToHostException) cause, remoteAddress);
            }
            if (cause instanceof SocketException) {
                return new AnnotatedSocketException((SocketException) cause, remoteAddress);
            }

            return cause;
        }

        /**
         * 关闭前准备：若返回非 null {@link Executor}，调用方须在其上执行 {@link #doClose()}；
     * 返回 {@code null} 时须在 EventLoop 线程直接调用 {@link #doClose()}, the
         * caller must call the {@link Executor#execute(Runnable)} method with a task that calls
         * {@link #doClose()} on the returned {@link Executor}. If this method returns {@code null},
         * {@link #doClose()} must be called from the caller thread. (i.e. {@link EventLoop})
         */
        protected Executor prepareToClose() {
            return null;
        }
    }

    /**
     * 判断给定 {@link EventLoop} 是否与本 Channel 实现兼容
     */
    protected abstract boolean isCompatible(EventLoop loop);

    /**
     * 返回本地 bind 地址（子类实现）
     */
    protected abstract SocketAddress localAddress0();

    /**
     * 返回 Channel 连接的远程地址（子类实现）
     */
    protected abstract SocketAddress remoteAddress0();

    /**
     * Channel 注册到 {@link EventLoop} 时调用（注册流程的一部分）
     * Subclasses may override this method
     *
     * @deprecated use {@link #doRegister(ChannelPromise)}
     */
    @Deprecated
    protected void doRegister() throws Exception {
        // NOOP
    }

    /**
     * Channel 注册到 {@link EventLoop} 时调用（注册流程的一部分）
     * Subclasses may override this method
     *
     * @param promise 注册完成后须通知的 {@link ChannelPromise}
     */
    protected void doRegister(ChannelPromise promise) {
        try {
            doRegister();
        } catch (Throwable cause) {
            promise.setFailure(cause);
            return;
        }
        promise.setSuccess();
    }

    /**
     * 将 {@link Channel} bind 到本地 {@link SocketAddress}
     */
    protected abstract void doBind(SocketAddress localAddress) throws Exception;

    /**
     * 断开 {@link Channel} 与远程对端的连接
     */
    protected abstract void doDisconnect() throws Exception;

    /**
     * 关闭 {@link Channel}
     */
    protected abstract void doClose() throws Exception;

    /**
     * 写异常等条件下关闭输出侧；默认委托 {@link #doClose()} This may happen if a write
     * operation throws an exception.
     */
    protected void doShutdownOutput() throws Exception {
        doClose();
    }

    /**
     * 从 {@link EventLoop} 注销 {@link Channel}
     *
     * Sub-classes may override this method
     */
    protected void doDeregister() throws Exception {
        // NOOP
    }

    /**
     * 调度读操作（子类实现具体 IO 注册）
     */
    protected abstract void doBeginRead() throws Exception;

    /**
     * 将 {@link ChannelOutboundBuffer} 内容 flush 到远程对端
     */
    protected abstract void doWrite(ChannelOutboundBuffer in) throws Exception;

    /**
     * 消息入队 {@link ChannelOutboundBuffer} 时调用，子类可转换消息类型（如 heap→direct） of this {@link AbstractChannel}, so that
     * the {@link Channel} implementation converts the message to another. (e.g. heap buffer -> direct buffer)
     */
    protected Object filterOutboundMessage(Object msg) throws Exception {
        return msg;
    }

    protected void validateFileRegion(DefaultFileRegion region, long position) throws IOException {
        DefaultFileRegion.validate(region, position);
    }

    static final class CloseFuture extends DefaultChannelPromise {

        CloseFuture(AbstractChannel ch) {
            super(ch);
        }

        @Override
        public ChannelPromise setSuccess() {
            throw new IllegalStateException();
        }

        @Override
        public ChannelPromise setFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        @Override
        public boolean trySuccess() {
            throw new IllegalStateException();
        }

        @Override
        public boolean tryFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        boolean setClosed() {
            return super.trySuccess();
        }
    }

    private static final class AnnotatedConnectException extends ConnectException {

        private static final long serialVersionUID = 3901958112696433556L;

        AnnotatedConnectException(ConnectException exception, SocketAddress remoteAddress) {
            super(exception.getMessage() + ": " + remoteAddress);
            initCause(exception);
        }

        // Suppress a warning since this method doesn't need synchronization
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    private static final class AnnotatedNoRouteToHostException extends NoRouteToHostException {

        private static final long serialVersionUID = -6801433937592080623L;

        AnnotatedNoRouteToHostException(NoRouteToHostException exception, SocketAddress remoteAddress) {
            super(exception.getMessage() + ": " + remoteAddress);
            initCause(exception);
        }

        // Suppress a warning since this method doesn't need synchronization
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    private static final class AnnotatedSocketException extends SocketException {

        private static final long serialVersionUID = 3896743275010454039L;

        AnnotatedSocketException(SocketException exception, SocketAddress remoteAddress) {
            super(exception.getMessage() + ": " + remoteAddress);
            initCause(exception);
        }

        // Suppress a warning since this method doesn't need synchronization
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
