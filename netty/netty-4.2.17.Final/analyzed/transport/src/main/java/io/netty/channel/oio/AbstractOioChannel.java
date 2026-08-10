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
package io.netty.channel.oio;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.ThreadPerChannelEventLoop;

import java.net.SocketAddress;

/**
 * Abstract base class for {@link Channel} implementations that use Old-Blocking-IO
 * <p>使用阻塞 I/O（OIO）的 {@link Channel} 抽象基类；已废弃，请改用 NIO/EPOLL/KQUEUE。</p>
 *
 * @deprecated use NIO / EPOLL / KQUEUE transport.
 */
@Deprecated
public abstract class AbstractOioChannel extends AbstractChannel {

    /** Socket 读超时（毫秒），供子类阻塞读时使用 */
    protected static final int SO_TIMEOUT = 1000;

    /** 是否已调度读任务（OIO 读为单线程串行） */
    boolean readPending;
    /** channel 未激活时收到 read 请求，激活后再调度 */
    boolean readWhenInactive;
    /** 在 EventLoop 上执行 {@link #doRead()} 的任务 */
    final Runnable readTask = new Runnable() {
        @Override
        public void run() {
            doRead();
        }
    };
    /** 在 EventLoop 上将 readPending 置为 false 的任务 */
    private final Runnable clearReadPendingRunnable = new Runnable() {
        @Override
        public void run() {
            readPending = false;
        }
    };

    /**
     * @see AbstractChannel#AbstractChannel(Channel)
     * <p>指定父 channel 构造 OIO channel。</p>
     */
    protected AbstractOioChannel(Channel parent) {
        super(parent);
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new DefaultOioUnsafe();
    }

    /** OIO channel 的 Unsafe 实现，connect 在 EventLoop 线程同步完成 */
    private final class DefaultOioUnsafe extends AbstractUnsafe {
        @Override
        public void connect(
                final SocketAddress remoteAddress,
                final SocketAddress localAddress, final ChannelPromise promise) {
            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }

            try {
                boolean wasActive = isActive();
                doConnect(remoteAddress, localAddress);

                // trySuccess 可能触发 listener 关闭 channel，仍须在此判断并 fireChannelActive
                // Get the state as trySuccess() may trigger an ChannelFutureListener that will close the Channel.
                // We still need to ensure we call fireChannelActive() in this case.
                boolean active = isActive();

                safeSetSuccess(promise);
                if (!wasActive && active) {
                    pipeline().fireChannelActive();
                }
            } catch (Throwable t) {
                safeSetFailure(promise, annotateConnectException(t, remoteAddress));
                closeIfClosed();
            }
        }
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        // OIO 要求每 channel 独占 EventLoop（ThreadPerChannelEventLoop）
        return loop instanceof ThreadPerChannelEventLoop;
    }

    /**
     * Connect to the remote peer using the given localAddress if one is specified or {@code null} otherwise.
     * <p>连接远端；{@code localAddress} 可为 {@code null}。</p>
     */
    protected abstract void doConnect(
            SocketAddress remoteAddress, SocketAddress localAddress) throws Exception;

    @Override
    protected void doBeginRead() throws Exception {
        if (readPending) {
            return;
        }
        if (!isActive()) {
            // 尚未激活，记录待读标志
            readWhenInactive = true;
            return;
        }

        readPending = true;
        eventLoop().execute(readTask);
    }

    /** 子类实现：在 EventLoop 上执行一次阻塞读 */
    protected abstract void doRead();

    /**
     * @deprecated No longer supported.
     * No longer supported.
     * <p>已不再支持；请使用 {@link #clearReadPending()}。</p>
     */
    @Deprecated
    protected boolean isReadPending() {
        return readPending;
    }

    /**
     * @deprecated Use {@link #clearReadPending()} if appropriate instead.
     * No longer supported.
     * <p>已不再支持；请改用 {@link #clearReadPending()}。</p>
     */
    @Deprecated
    protected void setReadPending(final boolean readPending) {
        if (isRegistered()) {
            EventLoop eventLoop = eventLoop();
            if (eventLoop.inEventLoop()) {
                this.readPending = readPending;
            } else {
                eventLoop.execute(new Runnable() {
                    @Override
                    public void run() {
                        AbstractOioChannel.this.readPending = readPending;
                    }
                });
            }
        } else {
            this.readPending = readPending;
        }
    }

    /**
     * Set read pending to {@code false}.
     * <p>将 readPending 置为 {@code false}，避免重复调度读任务。</p>
     */
    protected final void clearReadPending() {
        if (isRegistered()) {
            EventLoop eventLoop = eventLoop();
            if (eventLoop.inEventLoop()) {
                readPending = false;
            } else {
                eventLoop.execute(clearReadPendingRunnable);
            }
        } else {
            // 尚未注册时尽力清除；常见于 channel 初始化阶段
            // Best effort if we are not registered yet clear readPending. This happens during channel initialization.
            readPending = false;
        }
    }
}
