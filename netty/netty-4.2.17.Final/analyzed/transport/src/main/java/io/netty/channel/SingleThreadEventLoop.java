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

import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.concurrent.RejectedExecutionHandlers;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.UnstableApi;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * Abstract base class for {@link EventLoop}s that execute all its submitted tasks in a single thread.
 * <p>在单线程中执行全部已提交任务的 {@link EventLoop} 抽象基类；除普通任务队列外，还提供
 * {@link #executeAfterEventLoopIteration(Runnable)} 在每次事件循环迭代末尾执行尾部任务。</p>
 */
public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor implements EventLoop {

    /** 默认最大待处理任务数，可通过系统属性 {@code io.netty.eventLoop.maxPendingTasks} 覆盖 */
    protected static final int DEFAULT_MAX_PENDING_TASKS = Math.max(16,
            SystemPropertyUtil.getInt("io.netty.eventLoop.maxPendingTasks", Integer.MAX_VALUE));

    /** 在当前或下一次事件循环迭代末尾执行的任务队列 */
    private final Queue<Runnable> tailTasks;

    protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp) {
        this(parent, threadFactory, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS, RejectedExecutionHandlers.reject());
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory,
                                    boolean addTaskWakesUp, boolean supportSuspension) {
        this(parent, threadFactory, addTaskWakesUp, supportSuspension, DEFAULT_MAX_PENDING_TASKS,
                RejectedExecutionHandlers.reject());
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor, boolean addTaskWakesUp) {
        this(parent, executor, addTaskWakesUp, false);
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor,
                                    boolean addTaskWakesUp, boolean supportSuspension) {
        this(parent, executor, addTaskWakesUp, supportSuspension, DEFAULT_MAX_PENDING_TASKS,
                RejectedExecutionHandlers.reject());
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory,
                                    boolean addTaskWakesUp, int maxPendingTasks,
                                    RejectedExecutionHandler rejectedExecutionHandler) {
        this(parent, threadFactory, addTaskWakesUp, false, maxPendingTasks, rejectedExecutionHandler);
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory,
                                    boolean addTaskWakesUp, boolean supportSuspension, int maxPendingTasks,
                                    RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, threadFactory, addTaskWakesUp, supportSuspension, maxPendingTasks, rejectedExecutionHandler);
        tailTasks = newTaskQueue(maxPendingTasks);
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor,
                                    boolean addTaskWakesUp, int maxPendingTasks,
                                    RejectedExecutionHandler rejectedExecutionHandler) {
        this(parent, executor, addTaskWakesUp, false, maxPendingTasks, rejectedExecutionHandler);
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor,
                                    boolean addTaskWakesUp, boolean supportSuspension, int maxPendingTasks,
                                    RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, executor, addTaskWakesUp, supportSuspension, maxPendingTasks, rejectedExecutionHandler);
        tailTasks = newTaskQueue(maxPendingTasks);
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor,
                                    boolean addTaskWakesUp, Queue<Runnable> taskQueue, Queue<Runnable> tailTaskQueue,
                                    RejectedExecutionHandler rejectedExecutionHandler) {
        this(parent, executor, addTaskWakesUp, false, taskQueue, tailTaskQueue, rejectedExecutionHandler);
    }

    protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor,
                                    boolean addTaskWakesUp, boolean supportSuspension,
                                    Queue<Runnable> taskQueue, Queue<Runnable> tailTaskQueue,
                                    RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, executor, addTaskWakesUp, supportSuspension, taskQueue, rejectedExecutionHandler);
        tailTasks = ObjectUtil.checkNotNull(tailTaskQueue, "tailTaskQueue");
    }

    @Override
    public EventLoopGroup parent() {
        return (EventLoopGroup) super.parent();
    }

    @Override
    public EventLoop next() {
        return (EventLoop) super.next();
    }

    /** 将 {@link Channel} 注册到本 {@link EventLoop} */
    @Override
    public ChannelFuture register(Channel channel) {
        return register(new DefaultChannelPromise(channel, this));
    }

    /** 使用给定 {@link ChannelPromise} 完成注册 */
    @Override
    public ChannelFuture register(final ChannelPromise promise) {
        ObjectUtil.checkNotNull(promise, "promise");
        promise.channel().unsafe().register(this, promise);
        return promise;
    }

    @Deprecated
    @Override
    public ChannelFuture register(final Channel channel, final ChannelPromise promise) {
        ObjectUtil.checkNotNull(promise, "promise");
        ObjectUtil.checkNotNull(channel, "channel");
        channel.unsafe().register(this, promise);
        return promise;
    }

    /**
     * Adds a task to be run once at the end of next (or current) {@code eventloop} iteration.
     * <p>在下次（或当前）事件循环迭代末尾执行一次的任务；常用于在 I/O 轮次结束后清理或汇总。</p>
     *
     * @param task to be added.
     */
    public final void executeAfterEventLoopIteration(Runnable task) {
        ObjectUtil.checkNotNull(task, "task");
        if (isShutdown()) {
            reject();
        }

        if (!tailTasks.offer(task)) {
            reject(task);
        }

        if (wakesUpForTask(task)) {
            wakeup(inEventLoop());
        }
    }

    /**
     * Removes a task that was added previously via {@link #executeAfterEventLoopIteration(Runnable)}.
     * <p>移除此前通过 {@link #executeAfterEventLoopIteration(Runnable)} 加入的尾部任务。</p>
     *
     * @param task to be removed.
     *
     * @return {@code true} if the task was removed as a result of this call.
     */
    final boolean removeAfterEventLoopIterationTask(Runnable task) {
        return tailTasks.remove(ObjectUtil.checkNotNull(task, "task"));
    }

    /** 普通任务执行完毕后，继续 drain 尾部任务队列 */
    @Override
    protected void afterRunningAllTasks() {
        runAllTasksFrom(tailTasks);
    }

    /** 普通任务或尾部任务任一非空即视为有待执行任务 */
    @Override
    protected boolean hasTasks() {
        return super.hasTasks() || !tailTasks.isEmpty();
    }

    /** 待处理任务数 = 普通队列 + 尾部队列 */
    @Override
    public int pendingTasks() {
        return super.pendingTasks() + tailTasks.size();
    }

    /**
     * Returns the number of {@link Channel}s registered with this {@link EventLoop} or {@code -1}
     * if operation is not supported. The returned value is not guaranteed to be exact accurate and
     * should be viewed as a best effort.
     * <p>返回注册到本 {@link EventLoop} 的 {@link Channel} 数量；不支持时返回 {@code -1}。
     * 数值为尽力估计，不保证精确。</p>
     */
    @UnstableApi
    public int registeredChannels() {
        return -1;
    }

    /**
     * @return read-only iterator of active {@link Channel}s registered with this {@link EventLoop}.
     *         The returned value is not guaranteed to be exact accurate and
     *         should be viewed as a best effort. This method is expected to be called from within
     *         event loop.
     * @throws UnsupportedOperationException if operation is not supported by implementation.
     * <p>返回已注册活跃 {@link Channel} 的只读迭代器；应在事件循环线程内调用，数值为尽力估计。</p>
     */
    @UnstableApi
    public Iterator<Channel> registeredChannelsIterator() {
        throw new UnsupportedOperationException("registeredChannelsIterator");
    }

    /** 已注册 {@link Channel} 的只读迭代器包装 */
    protected static final class ChannelsReadOnlyIterator<T extends Channel> implements Iterator<Channel> {
        private final Iterator<T> channelIterator;

        public ChannelsReadOnlyIterator(Iterable<T> channelIterable) {
            this.channelIterator =
                    ObjectUtil.checkNotNull(channelIterable, "channelIterable").iterator();
        }

        @Override
        public boolean hasNext() {
            return channelIterator.hasNext();
        }

        @Override
        public Channel next() {
            return channelIterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        /** 返回永不产生元素的只读空迭代器 */
        @SuppressWarnings("unchecked")
        public static <T> Iterator<T> empty() {
            return (Iterator<T>) EMPTY;
        }

        private static final Iterator<Object> EMPTY = new Iterator<Object>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Object next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }
}
