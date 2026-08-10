/*
 * Copyright 2025 The Netty Project
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

import io.netty.util.concurrent.AbstractScheduledEventExecutor;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.Ticker;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ThreadExecutorMap;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link IoEventLoop} implementation that is owned by the user and so needs to be driven by the user manually with the
 * given {@link Thread}. This means that the user is responsible to call either {@link #runNow()} or {@link #run(long)}
 * to execute IO and tasks that were submitted to this {@link IoEventLoop}.
 * <p>
 * This is for <strong>advanced use-cases only</strong>, where the user wants to own the {@link Thread} that drives the
 * {@link IoEventLoop} to also do other work. Care must be taken that the {@link #runNow() or
 * {@link #waitAndRun()}} methods are called in a timely fashion.
 * <p>由用户线程手动驱动的 {@link IoEventLoop}：须定期调用 {@link #runNow()} 或 {@link #run(long)}
 * 推进 I/O 与已提交任务。仅适用于高级场景，用户在同一线程上兼做其他工作时须及时调用驱动方法。</p>
 */
public class ManualIoEventLoop extends AbstractScheduledEventExecutor implements IoEventLoop {
    /** 唤醒占位任务，用于打断阻塞等待 */
    private static final Runnable WAKEUP_TASK = () -> {
        // NOOP
    };
    /** 生命周期：运行中 */
    private static final int ST_STARTED = 0;
    /** 生命周期：优雅关闭中 */
    private static final int ST_SHUTTING_DOWN = 1;
    /** 生命周期：已 shutdown */
    private static final int ST_SHUTDOWN = 2;
    /** 生命周期：已终止 */
    private static final int ST_TERMINATED = 3;

    /** 当前生命周期状态 */
    private final AtomicInteger state;
    /** 终止完成的 Future */
    private final Promise<?> terminationFuture = new DefaultPromise<Void>(GlobalEventExecutor.INSTANCE);
    /** 跨线程提交的任务队列 */
    private final Queue<Runnable> taskQueue = PlatformDependent.newMpscQueue();
    /** 非阻塞 I/O 运行上下文 */
    private final IoHandlerContext nonBlockingContext = new IoHandlerContext() {
        @Override
        public boolean canBlock() {
            assert inEventLoop();
            return false;
        }

        @Override
        public long delayNanos(long currentTimeNanos) {
            assert inEventLoop();
            return 0;
        }

        @Override
        public long deadlineNanos() {
            assert inEventLoop();
            return -1;
        }
    };
    /** 允许阻塞等待 I/O 的运行上下文 */
    private final BlockingIoHandlerContext blockingContext = new BlockingIoHandlerContext();
    /** 父 EventLoopGroup，可为 null */
    private final IoEventLoopGroup parent;
    /** 驱动本 EventLoop 的用户线程 */
    private final AtomicReference<Thread> owningThread;
    /** 底层 I/O 处理器 */
    private final IoHandler handler;
    /** 时间源，用于定时与超时 */
    private final Ticker ticker;

    /** 优雅关闭静默期（纳秒） */
    private volatile long gracefulShutdownQuietPeriod;
    /** 优雅关闭总超时（纳秒） */
    private volatile long gracefulShutdownTimeout;
    /** 优雅关闭开始时间 */
    private long gracefulShutdownStartTime;
    /** 上次任务执行时间 */
    private long lastExecutionTime;
    /** 是否已完成 handler 初始化 */
    private boolean initialized;

    /**
     * This allows to specify additional blocking conditions which will be used by the {@link IoHandler} to decide
     * whether it is allowed to block or not.
     * <p>子类可覆盖以追加阻塞条件，供 {@link IoHandler} 判断是否允许阻塞。</p>
     */
    protected boolean canBlock() {
        return true;
    }

    /**
     * Create a new {@link IoEventLoop} that is owned by the user and so needs to be driven by the user with the given
     * {@link Thread}. This means that the user is responsible to call either {@link #runNow()} or
     * {@link #run(long)} to execute IO or tasks that were submitted to this {@link IoEventLoop}.
     * <p>使用指定线程与 {@link IoHandlerFactory} 创建手动驱动的 EventLoop。</p>
     *
     * @param owningThread      the {@link Thread} that executes the IO and tasks for this {@link IoEventLoop}. The
     *                          user will use this {@link Thread} to call {@link #runNow()} or {@link #run(long)} to
     *                          make progress.
     * @param factory           the {@link IoHandlerFactory} that will be used to create the {@link IoHandler} that is
     *                          used by this {@link IoEventLoop}.
     */
    public ManualIoEventLoop(Thread owningThread, IoHandlerFactory factory) {
        this(null, owningThread, factory);
    }

    /**
     * Create a new {@link IoEventLoop} that is owned by the user and so needs to be driven by the user with the given
     * {@link Thread}. This means that the user is responsible to call either {@link #runNow()} or
     * {@link #run(long)} to execute IO or tasks that were submitted to this {@link IoEventLoop}.
     *
     * @param parent            the parent {@link IoEventLoopGroup} or {@code null} if no parent.
     * @param owningThread      the {@link Thread} that executes the IO and tasks for this {@link IoEventLoop}. The
     *                          user will use this {@link Thread} to call {@link #runNow()} or {@link #run(long)} to
     *                          make progress. If {@code null}, must be set later using
     *                          {@link #setOwningThread(Thread)}.
     * @param factory           the {@link IoHandlerFactory} that will be used to create the {@link IoHandler} that is
     *                          used by this {@link IoEventLoop}.
     * <p>可指定父 {@link IoEventLoopGroup}；{@code owningThread} 为 {@code null} 时须稍后
     * {@link #setOwningThread(Thread)}。</p>
     */
    public ManualIoEventLoop(IoEventLoopGroup parent, Thread owningThread, IoHandlerFactory factory) {
        this(parent, owningThread, factory, Ticker.systemTicker());
    }

    /**
     * Create a new {@link IoEventLoop} that is owned by the user and so needs to be driven by the user with the given
     * {@link Thread}. This means that the user is responsible to call either {@link #runNow()} or
     * {@link #run(long)} to execute IO or tasks that were submitted to this {@link IoEventLoop}.
     * <p>可自定义 {@link Ticker}；若 ticker 快于系统时间，可能需手动 {@link #wakeup()}。</p>
     *
     * @param parent            the parent {@link IoEventLoopGroup} or {@code null} if no parent.
     * @param owningThread      the {@link Thread} that executes the IO and tasks for this {@link IoEventLoop}. The
     *                          user will use this {@link Thread} to call {@link #runNow()} or {@link #run(long)} to
     *                          make progress. If {@code null}, must be set later using
     *                          {@link #setOwningThread(Thread)}.
     * @param factory           the {@link IoHandlerFactory} that will be used to create the {@link IoHandler} that is
     *                          used by this {@link IoEventLoop}.
     * @param ticker            The {@link #ticker()} to use for this event loop. Note that the {@link IoHandler} does
     *                          not use the ticker, so if the ticker advances faster than system time, you may have to
     *                          {@link #wakeup()} this event loop manually.
     */
    public ManualIoEventLoop(IoEventLoopGroup parent, Thread owningThread, IoHandlerFactory factory, Ticker ticker) {
        this.parent = parent;
        this.owningThread = new AtomicReference<>(owningThread);
        this.handler = factory.newHandler(this);
        this.ticker = Objects.requireNonNull(ticker, "ticker");
        state = new AtomicInteger(ST_STARTED);
    }

    /** 返回本 EventLoop 使用的时间源。 */
    @Override
    public final Ticker ticker() {
        return ticker;
    }

    /**
     * Poll and run tasks from the task queue, until the task queue is empty or the given deadline is exceeded.<br>
     * If {@code timeoutNanos} is less or equals 0, no deadline is applied.
     * <p>非阻塞地运行任务队列中的任务，直至队列为空或超过 {@code timeoutNanos} 截止时间。</p>
     *
     * @param timeoutNanos the maximum time in nanoseconds to run tasks.
     */
    public final int runNonBlockingTasks(long timeoutNanos) {
        return runAllTasks(timeoutNanos, true);
    }

    private int runAllTasks(long timeoutNanos, boolean setCurrentExecutor) {
        assert inEventLoop();
        final Queue<Runnable> taskQueue = this.taskQueue;
        // since the taskQueue is unbounded we don't need to keep on calling this while draining it.
        boolean alwaysTrue = fetchFromScheduledTaskQueue(taskQueue);
        assert alwaysTrue;
        Runnable task = taskQueue.poll();
        if (task == null) {
            return 0;
        }
        EventExecutor old = setCurrentExecutor? ThreadExecutorMap.setCurrentExecutor(this) : null;
        try {
            final long deadline = timeoutNanos > 0 ? getCurrentTimeNanos() + timeoutNanos : 0;
            int runTasks = 0;
            long lastExecutionTime;
            final Ticker ticker = this.ticker;
            for (;;) {
                safeExecute(task);

                runTasks++;

               if (timeoutNanos > 0) {
                    lastExecutionTime = ticker.nanoTime();
                    if ((lastExecutionTime - deadline) >= 0) {
                        break;
                    }
                }

                task = taskQueue.poll();
                if (task == null) {
                    lastExecutionTime = ticker.nanoTime();
                    break;
                }
            }
            this.lastExecutionTime = lastExecutionTime;
            return runTasks;
        } finally {
            if (setCurrentExecutor) {
                ThreadExecutorMap.setCurrentExecutor(old);
            }
        }
    }

    private int run(IoHandlerContext context, long runAllTasksTimeoutNanos) {
        if (!initialized) {
            if (owningThread.get() == null) {
                throw new IllegalStateException("Owning thread not set");
            }
            initialized = true;
            handler.initialize();
        }
        EventExecutor old = ThreadExecutorMap.setCurrentExecutor(this);
        try {
            if (isShuttingDown()) {
                if (terminationFuture.isDone()) {
                    // Already completely terminated
                    return 0;
                }
                return runAllTasksBeforeDestroy();
            }
            final int ioTasks = handler.run(context);
            // Now run all tasks.
            if (runAllTasksTimeoutNanos < 0) {
                return ioTasks;
            }
            assert runAllTasksTimeoutNanos >= 0;
            return ioTasks + runAllTasks(runAllTasksTimeoutNanos, false);
        } finally {
            ThreadExecutorMap.setCurrentExecutor(old);
        }
    }

    private int runAllTasksBeforeDestroy() {
        // Run all tasks before prepare to destroy.
        int run = runAllTasks(-1, false);
        handler.prepareToDestroy();
        if (confirmShutdown()) {
            // Destroy the handler now and run all remaining tasks.
            try {
                handler.destroy();
                for (;;) {
                    int r = runAllTasks(-1, false);
                    run += r;
                    if (r == 0) {
                        break;
                    }
                }
            } finally {
                state.set(ST_TERMINATED);
                terminationFuture.setSuccess(null);
            }
        }
        return run;
    }

    /**
     * Executes all ready IO and tasks for this {@link IoEventLoop}.
     * This methods will <strong>NOT</strong> block and wait for IO / tasks to be ready, it will just
     * return directly if there is nothing to do.
     * <p>
     * <strong>Must be called from the owning {@link Thread} that was passed as a parameter on construction.</strong>
     * <p>
     *
     * @param runAllTasksTimeoutNanos the maximum time in nanoseconds to run tasks.
     *                                If {@code = 0}, no timeout is applied; if {@code < 0} it just perform I/O tasks.
     * @return the number of IO and tasks executed.
     * @throws IllegalStateException if the method is not called from the owning {@link Thread}.
     * <p>非阻塞执行就绪 I/O 与任务；必须在构造时指定的 owning 线程上调用。</p>
     */
    public final int runNow(long runAllTasksTimeoutNanos) {
        checkCurrentThread();
        return run(nonBlockingContext, runAllTasksTimeoutNanos);
    }

    /**
     * Run all ready IO and tasks for this {@link IoEventLoop}.
     * This methods will <strong>NOT</strong> block and wait for IO / tasks to be ready, it will just
     * return directly if there is nothing to do.
     * <p>
     * <strong>Must be called from the owning {@link Thread} that was passed as a parameter on construction.</strong>
     *
     * @return the number of IO and tasks executed.
     * <p>非阻塞执行就绪 I/O 与任务（无任务运行超时限制）。</p>
     */
    public final int runNow() {
        checkCurrentThread();
        return run(nonBlockingContext, 0);
    }

    /**
     * Run all ready IO and tasks for this {@link IoEventLoop}.
     * This methods will block and wait for IO / tasks to be ready if there is nothing to process atm for the given
     * {@code waitNanos}.
     * <p>
     * <strong>Must be called from the owning {@link Thread} that was passed as an parameter on construction.</strong>
     *
     * @param runAllTasksTimeoutNanos the maximum time in nanoseconds to run tasks.
     *                                If {@code = 0}, no timeout is applied; if {@code < 0} it just perform I/O tasks.
     * @param waitNanos the maximum amount of nanoseconds to wait before returning. IF {@code 0} it will block until
     *                  there is some IO / tasks ready, if {@code -1} will not block at all and just return directly
     *                  if there is nothing to run (like {@link #runNow()}).
     * @return          the number of IO and tasks executed.
     * <p>可阻塞等待 I/O/任务就绪；{@code waitNanos} 控制最长阻塞时间。</p>
     */
    public final int run(long waitNanos, long runAllTasksTimeoutNanos) {
        checkCurrentThread();

        final IoHandlerContext context;
        if (waitNanos < 0) {
            context = nonBlockingContext;
        } else {
            context = blockingContext;
            blockingContext.maxBlockingNanos = waitNanos == 0 ? Long.MAX_VALUE : waitNanos;
        }
        return run(context, runAllTasksTimeoutNanos);
    }

    /**
     * Run all ready IO and tasks for this {@link IoEventLoop}.
     * This methods will block and wait for IO / tasks to be ready if there is nothing to process atm for the given
     * {@code waitNanos}.
     * <p>
     * <strong>Must be called from the owning {@link Thread} that was passed as an parameter on construction.</strong>
     *
     * @param waitNanos the maximum amount of nanoseconds to wait before returning. IF {@code 0} it will block until
     *                  there is some IO / tasks ready, if {@code -1} will not block at all and just return directly
     *                  if there is nothing to run (like {@link #runNow()}).
     * @return          the number of IO and tasks executed.
     * <p>阻塞版驱动入口，任务运行无额外超时。</p>
     */
    public final int run(long waitNanos) {
        return run(waitNanos, 0);
    }

    /** 断言当前线程为 owning 线程。 */
    private void checkCurrentThread() {
        if (!inEventLoop(Thread.currentThread())) {
            throw new IllegalStateException();
        }
    }

    /**
     * Force a wakeup and so the {@link #run(long)} method will unblock and return even if there was nothing to do.
     * <p>强制唤醒，使 {@link #run(long)} 等阻塞调用尽快返回。</p>
     */
    public final void wakeup() {
        if (isShuttingDown()) {
            return;
        }
        handler.wakeup();
    }

    @Override
    public final ManualIoEventLoop next() {
        return this;
    }

    @Override
    public final IoEventLoopGroup parent() {
        return parent;
    }

    @Deprecated
    @Override
    public final ChannelFuture register(Channel channel) {
        return register(new DefaultChannelPromise(channel, this));
    }

    @Deprecated
    @Override
    public final ChannelFuture register(final ChannelPromise promise) {
        ObjectUtil.checkNotNull(promise, "promise");
        promise.channel().unsafe().register(this, promise);
        return promise;
    }

    @Override
    public final Future<IoRegistration> register(final IoHandle handle) {
        Promise<IoRegistration> promise = newPromise();
        if (inEventLoop()) {
            registerForIo0(handle, promise);
        } else {
            execute(() -> registerForIo0(handle, promise));
        }

        return promise;
    }

    private void registerForIo0(final IoHandle handle, Promise<IoRegistration> promise) {
        assert inEventLoop();
        final IoRegistration registration;
        try {
            registration = handler.register(handle);
        } catch (Exception e) {
            promise.setFailure(e);
            return;
        }
        promise.setSuccess(registration);
    }

    @Deprecated
    @Override
    public final ChannelFuture register(final Channel channel, final ChannelPromise promise) {
        ObjectUtil.checkNotNull(promise, "promise");
        ObjectUtil.checkNotNull(channel, "channel");
        channel.unsafe().register(this, promise);
        return promise;
    }

    @Override
    public final boolean isCompatible(Class<? extends IoHandle> handleType) {
        return handler.isCompatible(handleType);
    }

    @Override
    public final boolean isIoType(Class<? extends IoHandler> handlerType) {
        return handler.getClass().equals(handlerType);
    }

    @Override
    public final boolean inEventLoop(Thread thread) {
        return this.owningThread.get() == thread;
    }

    /**
     * Set the owning thread that will call {@link #run}. May only be called once, and only if the owning thread was
     * not set in the constructor already.
     *
     * @param owningThread The owning thread
     * <p>设置驱动线程；仅当构造时未指定且尚未设置时可调用一次。</p>
     */
    public final void setOwningThread(Thread owningThread) {
        Objects.requireNonNull(owningThread, "owningThread");
        if (!this.owningThread.compareAndSet(null, owningThread)) {
            throw new IllegalStateException("Owning thread already set");
        }
    }

    private void shutdown0(long quietPeriod, long timeout, int shutdownState) {
        boolean inEventLoop = inEventLoop();
        boolean wakeup;
        int oldState;
        for (;;) {
            if (isShuttingDown()) {
                return;
            }
            int newState;
            wakeup = true;
            oldState = state.get();
            if (inEventLoop) {
                newState = shutdownState;
            } else if (oldState == ST_STARTED) {
                newState = shutdownState;
            } else {
                newState = oldState;
                wakeup = false;
            }

            if (state.compareAndSet(oldState, newState)) {
                break;
            }
        }
        if (quietPeriod != -1) {
            gracefulShutdownQuietPeriod = quietPeriod;
        }
        if (timeout != -1) {
            gracefulShutdownTimeout = timeout;
        }

        if (wakeup) {
            // same as AbstractScheduledEventExecutor.WAKEUP_TASK
            taskQueue.offer(WAKEUP_TASK);
            handler.wakeup();
        }
    }

    @Override
    public final Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        ObjectUtil.checkPositiveOrZero(quietPeriod, "quietPeriod");
        if (timeout < quietPeriod) {
            throw new IllegalArgumentException(
                    "timeout: " + timeout + " (expected >= quietPeriod (" + quietPeriod + "))");
        }
        ObjectUtil.checkNotNull(unit, "unit");

        shutdown0(unit.toNanos(quietPeriod), unit.toNanos(timeout), ST_SHUTTING_DOWN);
        return terminationFuture();
    }

    @Override
    @Deprecated
    public final void shutdown() {
        shutdown0(-1, -1, ST_SHUTDOWN);
    }

    @Override
    public final Future<?> terminationFuture() {
        return terminationFuture;
    }

    @Override
    public final boolean isShuttingDown() {
        return state.get() >= ST_SHUTTING_DOWN;
    }

    @Override
    public final boolean isShutdown() {
        return state.get() >= ST_SHUTDOWN;
    }

    @Override
    public final boolean isTerminated() {
        return state.get() == ST_TERMINATED;
    }

    @Override
    public final boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return terminationFuture.await(timeout, unit);
    }

    @Override
    public final void execute(Runnable command) {
        Objects.requireNonNull(command, "command");
        boolean inEventLoop = inEventLoop();
        if (inEventLoop) {
            if (isShutdown()) {
                throw new RejectedExecutionException("event executor terminated");
            }
        }
        taskQueue.add(command);
        if (!inEventLoop) {
            if (isShutdown()) {
                boolean reject = false;
                try {
                    if (taskQueue.remove(command)) {
                        reject = true;
                    }
                } catch (UnsupportedOperationException e) {
                    // The task queue does not support removal so the best thing we can do is to just move on and
                    // hope we will be able to pick-up the task before its completely terminated.
                    // In worst case we will log on termination.
                }
                if (reject) {
                    throw new RejectedExecutionException("event executor terminated");
                }
            }
            handler.wakeup();
        }
    }

    private boolean hasTasks() {
        return !taskQueue.isEmpty();
    }

    private boolean confirmShutdown() {
        if (!isShuttingDown()) {
            return false;
        }

        if (!inEventLoop()) {
            throw new IllegalStateException("must be invoked from an event loop");
        }

        cancelScheduledTasks();

        if (gracefulShutdownStartTime == 0) {
            gracefulShutdownStartTime = ticker.nanoTime();
        }

        if (runAllTasks(-1, false) > 0) {
            if (isShutdown()) {
                // Executor shut down - no new tasks anymore.
                return true;
            }

            // There were tasks in the queue. Wait a little bit more until no tasks are queued for the quiet period or
            // terminate if the quiet period is 0.
            // See https://github.com/netty/netty/issues/4241
            if (gracefulShutdownQuietPeriod == 0) {
                return true;
            }
            return false;
        }

        final long nanoTime = ticker.nanoTime();

        if (isShutdown() || nanoTime - gracefulShutdownStartTime > gracefulShutdownTimeout) {
            return true;
        }

        if (nanoTime - lastExecutionTime <= gracefulShutdownQuietPeriod) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Ignore
            }

            return false;
        }

        // No tasks were added for last quiet period - hopefully safe to shut down.
        // (Hopefully because we really cannot make a guarantee that there will be no execute() calls by a user.)
        return true;
    }

    @Override
    public final <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        // We need to check if the method was called from within the EventLoop as this would cause a deadlock.
        throwIfInEventLoop("invokeAny");
        return super.invokeAny(tasks);
    }

    @Override
    public final <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        // We need to check if the method was called from within the EventLoop as this would cause a deadlock.
        throwIfInEventLoop("invokeAny");
        return super.invokeAny(tasks, timeout, unit);
    }

    @Override
    public final <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        // We need to check if the method was called from within the EventLoop as this would cause a deadlock.
        throwIfInEventLoop("invokeAll");
        return super.invokeAll(tasks);
    }

    @Override
    public final <T> List<java.util.concurrent.Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        // We need to check if the method was called from within the EventLoop as this would cause a deadlock.
        throwIfInEventLoop("invokeAll");
        return super.invokeAll(tasks, timeout, unit);
    }

    private void throwIfInEventLoop(String method) {
        if (inEventLoop()) {
            throw new RejectedExecutionException(
                    "Calling " + method + " from within the EventLoop is not allowed as it would deadlock");
        }
    }

    /** 允许在无待处理任务/定时任务且 {@link #canBlock()} 为真时阻塞的 I/O 上下文 */
    private class BlockingIoHandlerContext implements IoHandlerContext {
        // this is a positive amount of nanos or Long.MAX_VALUE for no limit
        // 最大阻塞纳秒数，Long.MAX_VALUE 表示无上限
        long maxBlockingNanos = Long.MAX_VALUE;

        @Override
        public boolean canBlock() {
            assert inEventLoop();
            return !hasTasks() && !hasScheduledTasks() && ManualIoEventLoop.this.canBlock();
        }

        @Override
        public long delayNanos(long currentTimeNanos) {
            assert inEventLoop();
            return Math.min(maxBlockingNanos, ManualIoEventLoop.this.delayNanos(currentTimeNanos, maxBlockingNanos));
        }

        @Override
        public long deadlineNanos() {
            assert inEventLoop();
            long next = nextScheduledTaskDeadlineNanos();
            if (maxBlockingNanos == Long.MAX_VALUE) {
                // next == -1? -1 : next i.e. return next
                return next;
            }
            long now = ticker.nanoTime();
            // we cannot just check Math.min as nanoTime can be negative or wrap around!
            if (next == -1 || next - now > maxBlockingNanos) {
                return now + maxBlockingNanos;
            }
            return next;
        }
    }
}
