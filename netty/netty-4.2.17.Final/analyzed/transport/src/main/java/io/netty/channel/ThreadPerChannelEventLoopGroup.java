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


import io.netty.util.concurrent.AbstractEventExecutorGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.ReadOnlyIterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * An {@link EventLoopGroup} that creates one {@link EventLoop} per {@link Channel}.
 * <p>为每个 {@link Channel} 创建独立 {@link EventLoop} 的 {@link EventLoopGroup}；
 * 通过 active/idle 子 loop 池复用已终止 channel 的线程。</p>
 *
 * @deprecated this will be remove in the next-major release.
 */
@Deprecated
public class ThreadPerChannelEventLoopGroup extends AbstractEventExecutorGroup implements EventLoopGroup {

    /** 传给 {@link #newChild(Object...)} 的构造参数 */
    private final Object[] childArgs;
    /** 允许同时 active 的最大 channel 数，{@code 0} 表示无限制 */
    private final int maxChannels;
    /** 创建子 loop 线程的 {@link Executor} */
    final Executor executor;
    /** 当前正在处理 channel 的子 loop 集合 */
    final Set<EventLoop> activeChildren = ConcurrentHashMap.newKeySet();
    /** 空闲可复用的子 loop 队列 */
    final Queue<EventLoop> idleChildren = new ConcurrentLinkedQueue<>();
    /** 超过 {@code maxChannels} 时抛出的静态 {@link ChannelException} */
    private final ChannelException tooManyChannels;

    /** 是否已进入关闭流程 */
    private volatile boolean shuttingDown;
    /** 全部子 loop 终止后的 future */
    private final Promise<?> terminationFuture = new DefaultPromise<Void>(GlobalEventExecutor.INSTANCE);
    /** 子 loop 终止时检查是否整组已 terminated */
    private final FutureListener<Object> childTerminationListener = new FutureListener<Object>() {
        @Override
        public void operationComplete(Future<Object> future) throws Exception {
            // Inefficient, but works. — 逐个监听子 loop 终止以完成 group future
            if (isTerminated()) {
                terminationFuture.trySuccess(null);
            }
        }
    };

    /**
     * Create a new {@link ThreadPerChannelEventLoopGroup} with no limit in place.
     * <p>创建无 channel 数量上限的 group。</p>
     */
    protected ThreadPerChannelEventLoopGroup() {
        this(0);
    }

    /**
     * Create a new {@link ThreadPerChannelEventLoopGroup}.
     * <p>指定最大 channel 数；{@code maxChannels=0} 表示不限制。</p>
     *
     * @param maxChannels       the maximum number of channels to handle with this instance. Once you try to register
     *                          a new {@link Channel} and the maximum is exceed it will throw an
     *                          {@link ChannelException}. on the {@link #register(Channel)} and
     *                          {@link #register(ChannelPromise)} method.
     *                          Use {@code 0} to use no limit
     */
    protected ThreadPerChannelEventLoopGroup(int maxChannels) {
        this(maxChannels, (ThreadFactory) null);
    }

    /**
     * Create a new {@link ThreadPerChannelEventLoopGroup}.
     *
     * @param maxChannels       the maximum number of channels to handle with this instance. Once you try to register
     *                          a new {@link Channel} and the maximum is exceed it will throw an
     *                          {@link ChannelException} on the {@link #register(Channel)} and
     *                          {@link #register(ChannelPromise)} method.
     *                          Use {@code 0} to use no limit
     * @param threadFactory     the {@link ThreadFactory} used to create new {@link Thread} instances that handle the
     *                          registered {@link Channel}s
     * @param args              arguments which will passed to each {@link #newChild(Object...)} call.
     */
    protected ThreadPerChannelEventLoopGroup(int maxChannels, ThreadFactory threadFactory, Object... args) {
        this(maxChannels, threadFactory == null ? null : new ThreadPerTaskExecutor(threadFactory), args);
    }

    /**
     * Create a new {@link ThreadPerChannelEventLoopGroup}.
     *
     * @param maxChannels       the maximum number of channels to handle with this instance. Once you try to register
     *                          a new {@link Channel} and the maximum is exceed it will throw an
     *                          {@link ChannelException} on the {@link #register(Channel)} and
     *                          {@link #register(ChannelPromise)} method.
     *                          Use {@code 0} to use no limit
     * @param executor          the {@link Executor} used to create new {@link Thread} instances that handle the
     *                          registered {@link Channel}s
     * @param args              arguments which will passed to each {@link #newChild(Object...)} call.
     */
    protected ThreadPerChannelEventLoopGroup(int maxChannels, Executor executor, Object... args) {
        ObjectUtil.checkPositiveOrZero(maxChannels, "maxChannels");
        if (executor == null) {
            executor = new ThreadPerTaskExecutor(new DefaultThreadFactory(getClass()));
        }

        if (args == null) {
            childArgs = EmptyArrays.EMPTY_OBJECTS;
        } else {
            childArgs = args.clone();
        }

        this.maxChannels = maxChannels;
        this.executor = executor;

        tooManyChannels =
                ChannelException.newStatic("too many channels (max: " + maxChannels + ')',
                ThreadPerChannelEventLoopGroup.class, "nextChild()");
    }

    /**
     * Creates a new {@link EventLoop}.  The default implementation creates a new {@link ThreadPerChannelEventLoop}.
     * <p>工厂方法：默认返回 {@link ThreadPerChannelEventLoop}。</p>
     */
    protected EventLoop newChild(@SuppressWarnings("UnusedParameters") Object... args) throws Exception {
        return new ThreadPerChannelEventLoop(this);
    }

    @Override
    public Iterator<EventExecutor> iterator() {
        return new ReadOnlyIterator<EventExecutor>(activeChildren.iterator());
    }

    /** 本 group 不支持 round-robin {@link #next()} */
    @Override
    public EventLoop next() {
        throw new UnsupportedOperationException();
    }

    /** 向所有 active 与 idle 子 loop 发起优雅关闭 */
    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        shuttingDown = true;

        for (EventLoop l: activeChildren) {
            l.shutdownGracefully(quietPeriod, timeout, unit);
        }
        for (EventLoop l: idleChildren) {
            l.shutdownGracefully(quietPeriod, timeout, unit);
        }

        // Notify the future if there was no children.
        if (isTerminated()) {
            terminationFuture.trySuccess(null);
        }

        return terminationFuture();
    }

    @Override
    public Future<?> terminationFuture() {
        return terminationFuture;
    }

    @Override
    @Deprecated
    public void shutdown() {
        shuttingDown = true;

        for (EventLoop l: activeChildren) {
            l.shutdown();
        }
        for (EventLoop l: idleChildren) {
            l.shutdown();
        }

        // Notify the future if there was no children.
        if (isTerminated()) {
            terminationFuture.trySuccess(null);
        }
    }

    @Override
    public boolean isShuttingDown() {
        for (EventLoop l: activeChildren) {
            if (!l.isShuttingDown()) {
                return false;
            }
        }
        for (EventLoop l: idleChildren) {
            if (!l.isShuttingDown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isShutdown() {
        for (EventLoop l: activeChildren) {
            if (!l.isShutdown()) {
                return false;
            }
        }
        for (EventLoop l: idleChildren) {
            if (!l.isShutdown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTerminated() {
        for (EventLoop l: activeChildren) {
            if (!l.isTerminated()) {
                return false;
            }
        }
        for (EventLoop l: idleChildren) {
            if (!l.isTerminated()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        for (EventLoop l: activeChildren) {
            for (;;) {
                long timeLeft = deadline - System.nanoTime();
                if (timeLeft <= 0) {
                    return isTerminated();
                }
                if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
                    break;
                }
            }
        }
        for (EventLoop l: idleChildren) {
            for (;;) {
                long timeLeft = deadline - System.nanoTime();
                if (timeLeft <= 0) {
                    return isTerminated();
                }
                if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
                    break;
                }
            }
        }
        return isTerminated();
    }

    /** 注册 channel：从 idle 池取或新建子 loop，再在其上 register */
    @Override
    public ChannelFuture register(Channel channel) {
        ObjectUtil.checkNotNull(channel, "channel");
        try {
            EventLoop l = nextChild();
            return l.register(new DefaultChannelPromise(channel, l));
        } catch (Throwable t) {
            return new FailedChannelFuture(channel, GlobalEventExecutor.INSTANCE, t);
        }
    }

    @Override
    public ChannelFuture register(ChannelPromise promise) {
        try {
            return nextChild().register(promise);
        } catch (Throwable t) {
            promise.setFailure(t);
            return promise;
        }
    }

    @Deprecated
    @Override
    public ChannelFuture register(Channel channel, ChannelPromise promise) {
        ObjectUtil.checkNotNull(channel, "channel");
        try {
            return nextChild().register(channel, promise);
        } catch (Throwable t) {
            promise.setFailure(t);
            return promise;
        }
    }

    /** 从 idle 池获取或创建子 loop，并加入 active 集合 */
    private EventLoop nextChild() throws Exception {
        if (shuttingDown) {
            throw new RejectedExecutionException("shutting down");
        }

        EventLoop loop = idleChildren.poll();
        if (loop == null) {
            if (maxChannels > 0 && activeChildren.size() >= maxChannels) {
                throw tooManyChannels;
            }
            loop = newChild(childArgs);
            loop.terminationFuture().addListener(childTerminationListener);
        }
        activeChildren.add(loop);
        return loop;
    }
}
