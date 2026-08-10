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
package io.netty.util.concurrent;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.ThreadExecutorMap;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import org.jetbrains.annotations.Async.Schedule;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Single-thread singleton {@link EventExecutor}.  It starts the thread automatically and stops it when there is no
 * task pending in the task queue for {@code io.netty.globalEventExecutor.quietPeriodSeconds} second
 * (default is 1 second).  Please note it is not scalable to schedule large number of tasks to this executor;
 * use a dedicated executor.
 *
 * <p>全局单线程 {@link OrderedEventExecutor} 单例。有任务时自动启动工作线程；任务队列在
 * {@code io.netty.globalEventExecutor.quietPeriodSeconds}（默认 1 秒）静默期后为空则停止线程以节省资源。
 * 不适合大量调度，应使用专用 EventExecutorGroup。</p>
 */
public final class GlobalEventExecutor extends AbstractScheduledEventExecutor implements OrderedEventExecutor {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(GlobalEventExecutor.class);

    /** 静默期时长（纳秒），由系统属性 quietPeriodSeconds 配置。 */
    private static final long SCHEDULE_QUIET_PERIOD_INTERVAL;

    static {
        int quietPeriod = SystemPropertyUtil.getInt("io.netty.globalEventExecutor.quietPeriodSeconds", 1);
        if (quietPeriod <= 0) {
            quietPeriod = 1;
        }
        logger.debug("-Dio.netty.globalEventExecutor.quietPeriodSeconds: {}", quietPeriod);

        SCHEDULE_QUIET_PERIOD_INTERVAL = TimeUnit.SECONDS.toNanos(quietPeriod);
    }

    /** 全局唯一实例。 */
    public static final GlobalEventExecutor INSTANCE = new GlobalEventExecutor();

    /** 普通任务队列（含从调度队列转移来的任务）。 */
    final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
    /** 周期性 noop 任务，用于在静默期后触发线程自终止逻辑。 */
    final ScheduledFutureTask<Void> quietPeriodTask = new ScheduledFutureTask<Void>(
            this, Executors.<Void>callable(new Runnable() {
        @Override
        public void run() {
            // NOOP — 仅占位，使 TaskRunner 在静默期后有机会检查是否退出
        }
    }, null),
            // note: the getCurrentTimeNanos() call here only works because this is a final class, otherwise the method
            // could be overridden leading to unsafe initialization here!
            deadlineNanos(getCurrentTimeNanos(), SCHEDULE_QUIET_PERIOD_INTERVAL),
            -SCHEDULE_QUIET_PERIOD_INTERVAL
    );

    // because the GlobalEventExecutor is a singleton, tasks submitted to it can come from arbitrary threads and this
    // can trigger the creation of a thread from arbitrary thread groups; for this reason, the thread factory must not
    // be sticky about its thread group
    // visible for testing
    /** 工作线程工厂；不绑定特定 ThreadGroup，因提交方可来自任意线程。 */
    final ThreadFactory threadFactory;
    private final TaskRunner taskRunner = new TaskRunner();
    /** 工作线程是否已启动（CAS 控制单线程运行）。 */
    private final AtomicBoolean started = new AtomicBoolean();
    volatile Thread thread;

    /** 不支持关闭，terminationFuture 恒为失败。 */
    private final Future<?> terminationFuture;

    private GlobalEventExecutor() {
        scheduleFromEventLoop(quietPeriodTask);
        threadFactory = ThreadExecutorMap.apply(new DefaultThreadFactory(
                DefaultThreadFactory.toPoolName(getClass()), false, Thread.NORM_PRIORITY, null), this);

        terminationFuture = new FailedFuture<Object>(this,
                StacklessUnsupportedOperationException.newInstance(GlobalEventExecutor.class, "terminationFuture"));
    }

    /**
     * Take the next {@link Runnable} from the task queue and so will block if no task is currently present.
     *
     * @return {@code null} if the executor thread has been interrupted or waken up.
     *
     * <p>从任务队列取下一个 Runnable；无任务时阻塞。同时合并到期调度任务。
     * 被中断唤醒时返回 {@code null}。</p>
     */
    Runnable takeTask() {
        BlockingQueue<Runnable> taskQueue = this.taskQueue;
        for (;;) {
            ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
            if (scheduledTask == null) {
                Runnable task = null;
                try {
                    task = taskQueue.take();
                } catch (InterruptedException e) {
                    // Ignore — 中断时返回 null 由 TaskRunner 处理
                }
                return task;
            } else {
                long delayNanos = scheduledTask.delayNanos();
                Runnable task = null;
                if (delayNanos > 0) {
                    try {
                        task = taskQueue.poll(delayNanos, TimeUnit.NANOSECONDS);
                    } catch (InterruptedException e) {
                        // Waken up.
                        return null;
                    }
                }
                if (task == null) {
                    // We need to fetch the scheduled tasks now as otherwise there may be a chance that
                    // scheduled tasks are never executed if there is always one task in the taskQueue.
                    // This is for example true for the read task of OIO Transport
                    // See https://github.com/netty/netty/issues/1614
                    // 到期调度任务转入普通队列，避免 taskQueue 一直有任务导致调度任务饿死
                    fetchFromScheduledTaskQueue();
                    task = taskQueue.poll();
                }

                if (task != null) {
                    return task;
                }
            }
        }
    }

    /** 将已到期的调度任务移入 taskQueue。 */
    private void fetchFromScheduledTaskQueue() {
        long nanoTime = getCurrentTimeNanos();
        ScheduledFutureTask scheduledTask;
        while ((scheduledTask = (ScheduledFutureTask) pollScheduledTask(nanoTime)) != null) {
            if (scheduledTask.isCancelled()) {
                continue;
            }
            taskQueue.add(scheduledTask);
        }
    }

    /**
     * Return the number of tasks that are pending for processing.
     *
     * <p>当前 taskQueue 中待处理任务数（不含调度队列）。</p>
     */
    public int pendingTasks() {
        return taskQueue.size();
    }

    /**
     * Add a task to the task queue, or throws a {@link RejectedExecutionException} if this instance was shutdown
     * before.
     */
    private void addTask(Runnable task) {
        taskQueue.add(ObjectUtil.checkNotNull(task, "task"));
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return thread == this.thread;
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        return terminationFuture();
    }

    @Override
    public Future<?> terminationFuture() {
        return terminationFuture;
    }

    @Override
    @Deprecated
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShuttingDown() {
        return false;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return false;
    }

    /**
     * Waits until the worker thread of this executor has no tasks left in its task queue and terminates itself.
     * Because a new worker thread will be started again when a new task is submitted, this operation is only useful
     * when you want to ensure that the worker thread is terminated <strong>after</strong> your application is shut
     * down and there's no chance of submitting a new task afterwards.
     *
     * @return {@code true} if and only if the worker thread has been terminated
     *
     * <p>等待工作线程在无任务且过静默期后自行退出。应用关闭且不会再提交任务时才有意义。</p>
     */
    public boolean awaitInactivity(long timeout, TimeUnit unit) throws InterruptedException {
        ObjectUtil.checkNotNull(unit, "unit");

        final Thread thread = this.thread;
        if (thread == null) {
            throw new IllegalStateException("thread was not started");
        }
        thread.join(unit.toMillis(timeout));
        return !thread.isAlive();
    }

    @Override
    public void execute(Runnable task) {
        execute0(task);
    }

    private void execute0(@Schedule Runnable task) {
        addTask(ObjectUtil.checkNotNull(task, "task"));
        if (!inEventLoop()) {
            startThread();
        }
    }

    /** CAS 启动唯一工作线程，并避免 ClassLoader 泄漏。 */
    private void startThread() {
        if (started.compareAndSet(false, true)) {
            final Thread callingThread = Thread.currentThread();
            ClassLoader parentCCL = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    return callingThread.getContextClassLoader();
                }
            });
            // Avoid calling classloader leaking through Thread.inheritedAccessControlContext.
            setContextClassLoader(callingThread, null);
            try {
                final Thread t = threadFactory.newThread(taskRunner);
                // Set to null to ensure we not create classloader leaks by holds a strong reference to the inherited
                // classloader.
                // See:
                // - https://github.com/netty/netty/issues/7290
                // - https://bugs.openjdk.java.net/browse/JDK-7008595
                setContextClassLoader(t, null);

                // Set the thread before starting it as otherwise inEventLoop() may return false and so produce
                // an assert error.
                // See https://github.com/netty/netty/issues/4357
                thread = t;
                t.start();
            } finally {
                setContextClassLoader(callingThread, parentCCL);
            }
        }
    }

    private static void setContextClassLoader(final Thread t, final ClassLoader cl) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                t.setContextClassLoader(cl);
                return null;
            }
        });
    }

    /** 工作线程主循环：取任务执行，静默期后尝试自终止。 */
    final class TaskRunner implements Runnable {
        @Override
        public void run() {
            for (;;) {
                Runnable task = takeTask();
                if (task != null) {
                    try {
                        runTask(task);
                    } catch (Throwable t) {
                        logger.warn("Unexpected exception from the global event executor: ", t);
                    }

                    if (task != quietPeriodTask) {
                        continue;
                    }
                }

                Queue<ScheduledFutureTask<?>> scheduledTaskQueue = GlobalEventExecutor.this.scheduledTaskQueue;
                // Terminate if there is no task in the queue (except the noop task).
                if (taskQueue.isEmpty() && (scheduledTaskQueue == null || scheduledTaskQueue.size() == 1)) {
                    // Mark the current thread as stopped.
                    // The following CAS must always success and must be uncontended,
                    // because only one thread should be running at the same time.
                    boolean stopped = started.compareAndSet(true, false);
                    assert stopped;

                    // Check if there are pending entries added by execute() or schedule*() while we do CAS above.
                    // Do not check scheduledTaskQueue because it is not thread-safe and can only be mutated from a
                    // TaskRunner actively running tasks.
                    if (taskQueue.isEmpty()) {
                        // A) No new task was added and thus there's nothing to handle
                        //    -> safe to terminate because there's nothing left to do
                        // B) A new thread started and handled all the new tasks.
                        //    -> safe to terminate the new thread will take care the rest
                        break;
                    }

                    // There are pending tasks added again.
                    if (!started.compareAndSet(false, true)) {
                        // startThread() started a new thread and set 'started' to true.
                        // -> terminate this thread so that the new thread reads from taskQueue exclusively.
                        break;
                    }

                    // New tasks were added, but this worker was faster to set 'started' to true.
                    // i.e. a new worker thread was not started by startThread().
                    // -> keep this thread alive to handle the newly added entries.
                }
            }
        }
    }

    /** 无栈跟踪的 UnsupportedOperationException，避免单例 terminationFuture 长期持有栈导致 ClassLoader 泄漏。 */
    private static final class StacklessUnsupportedOperationException extends UnsupportedOperationException {

        private static final long serialVersionUID = -8060232216137960173L;

        private StacklessUnsupportedOperationException() { }

        // Override fillInStackTrace() so we not populate the backtrace via a native call and so leak the
        // Classloader. As the GlobalEventExecutor.INSTANCE is a singleton and holds on to this exception via its
        // terminationFuture, a populated backtrace would pin the Classloader of whatever thread happened to trigger
        // the lazy initialization of INSTANCE (see https://github.com/netty/netty/issues/17128).
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }

        static StacklessUnsupportedOperationException newInstance(Class<?> clazz, String method) {
            return ThrowableUtil.unknownStackTrace(new StacklessUnsupportedOperationException(), clazz, method);
        }
    }
}
