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
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.ThreadExecutorMap;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.jetbrains.annotations.Async.Schedule;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract base class for {@link OrderedEventExecutor}'s that execute all its submitted tasks in a single thread.
 *
 * <p>单线程 {@link OrderedEventExecutor} 抽象基类：所有提交任务在同一线程顺序执行。
 * 管理任务队列、定时任务转移、优雅关闭、可选挂起（suspend）及自动扩缩容指标采集。</p>
 */
public abstract class SingleThreadEventExecutor extends AbstractScheduledEventExecutor implements OrderedEventExecutor {

    /** 默认待处理任务上限，由 {@code io.netty.eventexecutor.maxPendingTasks} 配置。 */
    static final int DEFAULT_MAX_PENDING_EXECUTOR_TASKS = Math.max(16,
            SystemPropertyUtil.getInt("io.netty.eventexecutor.maxPendingTasks", Integer.MAX_VALUE));

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(SingleThreadEventExecutor.class);

    /** 生命周期状态：未启动 → 运行/挂起中/已挂起 → 关闭中 → 已关闭 → 已终止。 */
    private static final int ST_NOT_STARTED = 1;
    private static final int ST_SUSPENDING = 2;
    private static final int ST_SUSPENDED = 3;
    private static final int ST_STARTED = 4;
    private static final int ST_SHUTTING_DOWN = 5;
    private static final int ST_SHUTDOWN = 6;
    private static final int ST_TERMINATED = 7;

    /** 用于 threadProperties() 懒启动 EventLoop 线程的空任务。 */
    private static final Runnable NOOP_TASK = new Runnable() {
        @Override
        public void run() {
            // Do nothing.
        }
    };

    private static final AtomicIntegerFieldUpdater<SingleThreadEventExecutor> STATE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(SingleThreadEventExecutor.class, "state");
    private static final AtomicReferenceFieldUpdater<SingleThreadEventExecutor, ThreadProperties> PROPERTIES_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(
                    SingleThreadEventExecutor.class, ThreadProperties.class, "threadProperties");
    private static final AtomicLongFieldUpdater<SingleThreadEventExecutor> ACCUMULATED_ACTIVE_TIME_NANOS_UPDATER =
            AtomicLongFieldUpdater.newUpdater(SingleThreadEventExecutor.class, "accumulatedActiveTimeNanos");
    private static final AtomicIntegerFieldUpdater<SingleThreadEventExecutor> CONSECUTIVE_IDLE_CYCLES_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(SingleThreadEventExecutor.class, "consecutiveIdleCycles");
    private static final AtomicIntegerFieldUpdater<SingleThreadEventExecutor> CONSECUTIVE_BUSY_CYCLES_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(SingleThreadEventExecutor.class, "consecutiveBusyCycles");
    /** 普通任务队列（BlockingQueue 或 MPSC 等，由子类 {@link #newTaskQueue(int)} 决定）。 */
    private final Queue<Runnable> taskQueue;

    /** 当前 EventLoop 工作线程；挂起或终止后为 null。 */
    private volatile Thread thread;
    @SuppressWarnings("unused")
    private volatile ThreadProperties threadProperties;
    private final Executor executor;
    private volatile boolean interrupted;

    /** 保证同一时刻仅一个 run() 循环持有 thread 引用。 */
    private final Lock processingLock = new ReentrantLock();
    private final CountDownLatch threadLock = new CountDownLatch(1);
    /** 关闭前在 EventLoop 上执行的钩子（可动态增删）。 */
    private final Set<Runnable> shutdownHooks = new LinkedHashSet<Runnable>();
    /** {@code false} 时 addTask 不自动 wakeup，由子类 I/O 线程自行唤醒（如 NIO）。 */
    private final boolean addTaskWakesUp;
    private final int maxPendingTasks;
    /** 队列满或已关闭时的拒绝策略。 */
    private final RejectedExecutionHandler rejectedExecutionHandler;
    private final boolean supportSuspension;

    /** 自上次重置以来累计“活跃”纳秒（任务执行 + I/O 上报），供自动扩缩容监控。 */
    private volatile long accumulatedActiveTimeNanos;
    /** 最后一次任务或 I/O 活动的时间戳（纳秒）。 */
    private volatile long lastActivityTimeNanos;
    /**
     * Tracks the number of consecutive monitor cycles this executor's
     * utilization has been below the scale-down threshold.
     */
    private volatile int consecutiveIdleCycles;

    /**
     * Tracks the number of consecutive monitor cycles this executor's
     * utilization has been above the scale-up threshold.
     */
    private volatile int consecutiveBusyCycles;
    private long lastExecutionTime;

    @SuppressWarnings({ "FieldMayBeFinal", "unused" })
    private volatile int state = ST_NOT_STARTED;

    /** 优雅关闭静默期：此期间无新任务则确认关闭。 */
    private volatile long gracefulShutdownQuietPeriod;
    private volatile long gracefulShutdownTimeout;
    private long gracefulShutdownStartTime;

    /** 终止 Future，在 ST_TERMINATED 时 complete。 */
    private final Promise<?> terminationFuture = new DefaultPromise<Void>(GlobalEventExecutor.INSTANCE);

    /**
     * Create a new instance
     *
     * @param parent            the {@link EventExecutorGroup} which is the parent of this instance and belongs to it
     * @param threadFactory     the {@link ThreadFactory} which will be used for the used {@link Thread}
     * @param addTaskWakesUp    {@code true} if and only if invocation of {@link #addTask(Runnable)} will wake up the
     *                          executor thread
     */
    protected SingleThreadEventExecutor(
            EventExecutorGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp) {
        this(parent, new ThreadPerTaskExecutor(threadFactory), addTaskWakesUp);
    }

    /**
     * Create a new instance
     *
     * @param parent            the {@link EventExecutorGroup} which is the parent of this instance and belongs to it
     * @param threadFactory     the {@link ThreadFactory} which will be used for the used {@link Thread}
     * @param addTaskWakesUp    {@code true} if and only if invocation of {@link #addTask(Runnable)} will wake up the
     *                          executor thread
     * @param maxPendingTasks   the maximum number of pending tasks before new tasks will be rejected.
     * @param rejectedHandler   the {@link RejectedExecutionHandler} to use.
     */
    protected SingleThreadEventExecutor(
            EventExecutorGroup parent, ThreadFactory threadFactory,
            boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rejectedHandler) {
        this(parent, new ThreadPerTaskExecutor(threadFactory), addTaskWakesUp, maxPendingTasks, rejectedHandler);
    }

    /**
     * Create a new instance
     *
     * @param parent            the {@link EventExecutorGroup} which is the parent of this instance and belongs to it
     * @param threadFactory     the {@link ThreadFactory} which will be used for the used {@link Thread}
     * @param addTaskWakesUp    {@code true} if and only if invocation of {@link #addTask(Runnable)} will wake up the
     *                          executor thread
     * @param supportSuspension {@code true} if suspension of this {@link SingleThreadEventExecutor} is supported.
     * @param maxPendingTasks   the maximum number of pending tasks before new tasks will be rejected.
     * @param rejectedHandler   the {@link RejectedExecutionHandler} to use.
     */
    protected SingleThreadEventExecutor(
            EventExecutorGroup parent, ThreadFactory threadFactory,
            boolean addTaskWakesUp, boolean supportSuspension,
            int maxPendingTasks, RejectedExecutionHandler rejectedHandler) {
        this(parent, new ThreadPerTaskExecutor(threadFactory), addTaskWakesUp, supportSuspension,
                maxPendingTasks, rejectedHandler);
    }

    /**
     * Create a new instance
     *
     * @param parent            the {@link EventExecutorGroup} which is the parent of this instance and belongs to it
     * @param executor          the {@link Executor} which will be used for executing
     * @param addTaskWakesUp    {@code true} if and only if invocation of {@link #addTask(Runnable)} will wake up the
     *                          executor thread
     */
    protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor, boolean addTaskWakesUp) {
        this(parent, executor, addTaskWakesUp, DEFAULT_MAX_PENDING_EXECUTOR_TASKS, RejectedExecutionHandlers.reject());
    }

    /**
     * Create a new instance
     *
     * @param parent            the {@link EventExecutorGroup} which is the parent of this instance and belongs to it
     * @param executor          the {@link Executor} which will be used for executing
     * @param addTaskWakesUp    {@code true} if and only if invocation of {@link #addTask(Runnable)} will wake up the
     *                          executor thread
     * @param maxPendingTasks   the maximum number of pending tasks before new tasks will be rejected.
     * @param rejectedHandler   the {@link RejectedExecutionHandler} to use.
     */
    protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor,
                                        boolean addTaskWakesUp, int maxPendingTasks,
                                        RejectedExecutionHandler rejectedHandler) {
        this(parent, executor, addTaskWakesUp, false, maxPendingTasks, rejectedHandler);
    }

    /**
     * Create a new instance
     *
     * @param parent            the {@link EventExecutorGroup} which is the parent of this instance and belongs to it
     * @param executor          the {@link Executor} which will be used for executing
     * @param addTaskWakesUp    {@code true} if and only if invocation of {@link #addTask(Runnable)} will wake up the
     *                          executor thread
     * @param supportSuspension {@code true} if suspension of this {@link SingleThreadEventExecutor} is supported.
     * @param maxPendingTasks   the maximum number of pending tasks before new tasks will be rejected.
     * @param rejectedHandler   the {@link RejectedExecutionHandler} to use.
     */
    protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor,
                                        boolean addTaskWakesUp, boolean supportSuspension,
                                        int maxPendingTasks, RejectedExecutionHandler rejectedHandler) {
        super(parent);
        this.addTaskWakesUp = addTaskWakesUp;
        this.supportSuspension = supportSuspension;
        this.maxPendingTasks = Math.max(16, maxPendingTasks);
        this.executor = ThreadExecutorMap.apply(executor, this);
        taskQueue = newTaskQueue(this.maxPendingTasks);
        rejectedExecutionHandler = ObjectUtil.checkNotNull(rejectedHandler, "rejectedHandler");
        lastActivityTimeNanos = ticker().nanoTime();
    }

    protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor,
                                        boolean addTaskWakesUp, Queue<Runnable> taskQueue,
                                        RejectedExecutionHandler rejectedHandler) {
        this(parent, executor, addTaskWakesUp, false, taskQueue, rejectedHandler);
    }

    protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor,
                                        boolean addTaskWakesUp, boolean supportSuspension,
                                        Queue<Runnable> taskQueue, RejectedExecutionHandler rejectedHandler) {
        super(parent);
        this.addTaskWakesUp = addTaskWakesUp;
        this.supportSuspension = supportSuspension;
        this.maxPendingTasks = DEFAULT_MAX_PENDING_EXECUTOR_TASKS;
        this.executor = ThreadExecutorMap.apply(executor, this);
        this.taskQueue = ObjectUtil.checkNotNull(taskQueue, "taskQueue");
        this.rejectedExecutionHandler = ObjectUtil.checkNotNull(rejectedHandler, "rejectedHandler");
    }

    /**
     * @deprecated Please use and override {@link #newTaskQueue(int)}.
     */
    @Deprecated
    protected Queue<Runnable> newTaskQueue() {
        return newTaskQueue(maxPendingTasks);
    }

    /**
     * Create a new {@link Queue} which will holds the tasks to execute. This default implementation will return a
     * {@link LinkedBlockingQueue} but if your sub-class of {@link SingleThreadEventExecutor} will not do any blocking
     * calls on the this {@link Queue} it may make sense to {@code @Override} this and return some more performant
     * implementation that does not support blocking operations at all.
     */
    /** 默认 {@link LinkedBlockingQueue}；非阻塞子类可覆写为 MPSC 等。 */
    protected Queue<Runnable> newTaskQueue(int maxPendingTasks) {
        return new LinkedBlockingQueue<Runnable>(maxPendingTasks);
    }

    /**
     * Interrupt the current running {@link Thread}.
     */
    /** 中断 EventLoop 线程；线程尚未创建时仅置 interrupted 标志。 */
    protected void interruptThread() {
        Thread currentThread = thread;
        if (currentThread == null) {
            interrupted = true;
        } else {
            currentThread.interrupt();
        }
    }

    /**
     * @see Queue#poll()
     */
    /** 非阻塞取下一个任务，跳过内部 {@link #WAKEUP_TASK} 哨兵。 */
    protected Runnable pollTask() {
        assert inEventLoop();
        return pollTaskFrom(taskQueue);
    }

    protected static Runnable pollTaskFrom(Queue<Runnable> taskQueue) {
        for (;;) {
            Runnable task = taskQueue.poll();
            if (task != WAKEUP_TASK) {
                return task;
            }
        }
    }

    /**
     * Take the next {@link Runnable} from the task queue and so will block if no task is currently present.
     * <p>
     * Be aware that this method will throw an {@link UnsupportedOperationException} if the task queue, which was
     * created via {@link #newTaskQueue()}, does not implement {@link BlockingQueue}.
     * </p>
     *
     * @return {@code null} if the executor thread has been interrupted or waken up.
     */
    /** 阻塞取任务；结合最近定时任务 delay 带超时 poll，并 {@link #fetchFromScheduledTaskQueue()}。 */
    protected Runnable takeTask() {
        assert inEventLoop();
        if (!(taskQueue instanceof BlockingQueue)) {
            throw new UnsupportedOperationException();
        }

        BlockingQueue<Runnable> taskQueue = (BlockingQueue<Runnable>) this.taskQueue;
        for (;;) {
            ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
            if (scheduledTask == null) {
                Runnable task = null;
                try {
                    task = taskQueue.take();
                    if (task == WAKEUP_TASK) {
                        task = null;
                    }
                } catch (InterruptedException e) {
                    // Ignore
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
                    // 须及时把到期定时任务转入 taskQueue，否则 OIO 等场景可能饿死调度（#1614）
                    // scheduled tasks are never executed if there is always one task in the taskQueue.
                    // This is for example true for the read task of OIO Transport
                    // See https://github.com/netty/netty/issues/1614
                    fetchFromScheduledTaskQueue();
                    task = taskQueue.poll();
                }

                if (task != null) {
                    if (task == WAKEUP_TASK) {
                        return null;
                    }
                    return task;
                }
            }
        }
    }

    private boolean fetchFromScheduledTaskQueue() {
        return fetchFromScheduledTaskQueue(taskQueue);
    }

    /**
     * @return {@code true} if at least one scheduled task was executed.
     */
    /** 执行所有已到期的 {@link ScheduledFutureTask}，至少执行一个返回 true。 */
    private boolean executeExpiredScheduledTasks() {
        if (scheduledTaskQueue == null || scheduledTaskQueue.isEmpty()) {
            return false;
        }
        long nanoTime = getCurrentTimeNanos();
        Runnable scheduledTask = pollScheduledTask(nanoTime);
        if (scheduledTask == null) {
            return false;
        }
        do {
            safeExecute(scheduledTask);
        } while ((scheduledTask = pollScheduledTask(nanoTime)) != null);
        return true;
    }

    /**
     * @see Queue#peek()
     */
    protected Runnable peekTask() {
        assert inEventLoop();
        return taskQueue.peek();
    }

    /**
     * @see Queue#isEmpty()
     */
    protected boolean hasTasks() {
        assert inEventLoop();
        return !taskQueue.isEmpty();
    }

    /**
     * Return the number of tasks that are pending for processing.
     */
    public int pendingTasks() {
        return taskQueue.size();
    }

    /**
     * Add a task to the task queue, or throws a {@link RejectedExecutionException} if this instance was shutdown
     * before.
     */
    /** 入队；失败则 {@link #reject(Runnable)}。 */
    protected void addTask(Runnable task) {
        ObjectUtil.checkNotNull(task, "task");
        if (!offerTask(task)) {
            reject(task);
        }
    }

    /** 若已 shutdown 先 reject；否则 offer 到 taskQueue。 */
    final boolean offerTask(Runnable task) {
        if (isShutdown()) {
            reject();
        }
        return taskQueue.offer(task);
    }

    /**
     * @see Queue#remove(Object)
     */
    protected boolean removeTask(Runnable task) {
        return taskQueue.remove(ObjectUtil.checkNotNull(task, "task"));
    }

    /**
     * Poll all tasks from the task queue and run them via {@link Runnable#run()} method.
     *
     * @return {@code true} if and only if at least one task was run
     */
    /** 循环：转移定时任务 + 跑光 taskQueue，更新 lastExecutionTime 并调用 {@link #afterRunningAllTasks()}。 */
    protected boolean runAllTasks() {
        assert inEventLoop();
        boolean fetchedAll;
        boolean ranAtLeastOne = false;

        do {
            fetchedAll = fetchFromScheduledTaskQueue(taskQueue);
            if (runAllTasksFrom(taskQueue)) {
                ranAtLeastOne = true;
            }
        } while (!fetchedAll); // keep on processing until we fetched all scheduled tasks.

        if (ranAtLeastOne) {
            lastExecutionTime = getCurrentTimeNanos();
        }
        afterRunningAllTasks();
        return ranAtLeastOne;
    }

    /**
     * Execute all expired scheduled tasks and all current tasks in the executor queue until both queues are empty,
     * or {@code maxDrainAttempts} has been exceeded.
     * @param maxDrainAttempts The maximum amount of times this method attempts to drain from queues. This is to prevent
     *                         continuous task execution and scheduling from preventing the EventExecutor thread to
     *                         make progress and return to the selector mechanism to process inbound I/O events.
     * @return {@code true} if at least one task was run.
     */
    /** 有限次排空：先跑已有 taskQueue 任务再跑到期调度，防止长期占用 EventLoop 无法回到 selector（#4241 类问题）。 */
    protected final boolean runScheduledAndExecutorTasks(final int maxDrainAttempts) {
        assert inEventLoop();
        boolean ranAtLeastOneTask;
        int drainAttempt = 0;
        do {
            // We must run the taskQueue tasks first, because the scheduled tasks from outside the EventLoop are queued
            // here because the taskQueue is thread safe and the scheduledTaskQueue is not thread safe.
            ranAtLeastOneTask = runExistingTasksFrom(taskQueue) | executeExpiredScheduledTasks();
        } while (ranAtLeastOneTask && ++drainAttempt < maxDrainAttempts);

        if (drainAttempt > 0) {
            lastExecutionTime = getCurrentTimeNanos();
        }
        afterRunningAllTasks();

        return drainAttempt > 0;
    }

    /**
     * Runs all tasks from the passed {@code taskQueue}.
     *
     * @param taskQueue To poll and execute all tasks.
     *
     * @return {@code true} if at least one task was executed.
     */
    protected final boolean runAllTasksFrom(Queue<Runnable> taskQueue) {
        Runnable task = pollTaskFrom(taskQueue);
        if (task == null) {
            return false;
        }
        for (;;) {
            safeExecute(task);
            task = pollTaskFrom(taskQueue);
            if (task == null) {
                return true;
            }
        }
    }

    /**
     * What ever tasks are present in {@code taskQueue} when this method is invoked will be {@link Runnable#run()}.
     * @param taskQueue the task queue to drain.
     * @return {@code true} if at least {@link Runnable#run()} was called.
     */
    private boolean runExistingTasksFrom(Queue<Runnable> taskQueue) {
        Runnable task = pollTaskFrom(taskQueue);
        if (task == null) {
            return false;
        }
        int remaining = Math.min(maxPendingTasks, taskQueue.size());
        safeExecute(task);
        // 此处直接用 poll：pollTaskFrom 会吞掉 WAKEUP_TASK，可能多消费队列元素
        // silently consume more than one item from the queue (skips over WAKEUP_TASK instances)
        while (remaining-- > 0 && (task = taskQueue.poll()) != null) {
            safeExecute(task);
        }
        return true;
    }

    /**
     * Poll all tasks from the task queue and run them via {@link Runnable#run()} method.  This method stops running
     * the tasks in the task queue and returns if it ran longer than {@code timeoutNanos}.
     */
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    /** 在 timeoutNanos 预算内尽量执行任务；每 64 个任务检查一次超时；累计 active 时间。 */
    protected boolean runAllTasks(long timeoutNanos) {
        fetchFromScheduledTaskQueue(taskQueue);
        Runnable task = pollTask();
        if (task == null) {
            afterRunningAllTasks();
            return false;
        }

        final long deadline = timeoutNanos > 0 ? getCurrentTimeNanos() + timeoutNanos : 0;
        long runTasks = 0;
        long lastExecutionTime;

        long workStartTime = ticker().nanoTime();
        for (;;) {
            safeExecute(task);

            runTasks ++;

            // 每 64 个任务才读一次 nanoTime，平衡精度与开销
            // XXX: Hard-coded value - will make it configurable if it is really a problem.
            if ((runTasks & 0x3F) == 0) {
                lastExecutionTime = getCurrentTimeNanos();
                if (lastExecutionTime >= deadline) {
                    break;
                }
            }

            task = pollTask();
            if (task == null) {
                lastExecutionTime = getCurrentTimeNanos();
                break;
            }
        }

        long workEndTime = ticker().nanoTime();
        accumulatedActiveTimeNanos += workEndTime - workStartTime;
        lastActivityTimeNanos = workEndTime;

        afterRunningAllTasks();
        this.lastExecutionTime = lastExecutionTime;
        return true;
    }

    /**
     * Invoked before returning from {@link #runAllTasks()} and {@link #runAllTasks(long)}.
     */
    protected void afterRunningAllTasks() { }

    /**
     * Returns the amount of time left until the scheduled task with the closest dead line is executed.
     */
    /** 距下一个最近定时任务执行的剩余纳秒；无调度任务时返回 {@link #SCHEDULE_PURGE_INTERVAL}。 */
    protected long delayNanos(long currentTimeNanos) {
        currentTimeNanos -= ticker().initialNanoTime();

        ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
        if (scheduledTask == null) {
            return SCHEDULE_PURGE_INTERVAL;
        }

        return scheduledTask.delayNanos(currentTimeNanos);
    }

    /**
     * Returns the absolute point in time (relative to {@link #getCurrentTimeNanos()}) at which the next
     * closest scheduled task should run.
     */
    /** 下一个最近定时任务的绝对 deadline（纳秒）。 */
    protected long deadlineNanos() {
        ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
        if (scheduledTask == null) {
            return getCurrentTimeNanos() + SCHEDULE_PURGE_INTERVAL;
        }
        return scheduledTask.deadlineNanos();
    }

    /**
     * Updates the internal timestamp that tells when a submitted task was executed most recently.
     * {@link #runAllTasks()} and {@link #runAllTasks(long)} updates this timestamp automatically, and thus there's
     * usually no need to call this method.  However, if you take the tasks manually using {@link #takeTask()} or
     * {@link #pollTask()}, you have to call this method at the end of task execution loop for accurate quiet period
     * checks.
     */
    /** 手动 poll/take 任务后须调用，以便 {@link #confirmShutdown()} 正确判断 quiet period。 */
    protected void updateLastExecutionTime() {
        long now = getCurrentTimeNanos();
        lastExecutionTime = now;
        lastActivityTimeNanos = now;
    }

    /**
     * Returns the number of registered channels for auto-scaling related decisions.
     * This is intended to be used by {@link MultithreadEventExecutorGroup} for dynamic scaling.
     *
     * @return The number of registered channels, or {@code -1} if not applicable.
     */
    /** 子类可覆写返回注册 Channel 数，供 {@link MultithreadEventExecutorGroup} 扩缩容；默认 -1。 */
    protected int getNumOfRegisteredChannels() {
        return -1;
    }

    /**
     * Adds the given duration to the total active time for the current measurement window.
     * <p>
     * <strong>Note:</strong> This method is not thread-safe and must only be called from the
     * {@link #inEventLoop() event loop thread}.
     *
     * @param nanos The active time in nanoseconds to add.
     */
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    /** 在 EventLoop 内上报 I/O 活跃时间，累加到 {@link #accumulatedActiveTimeNanos}。 */
    protected void reportActiveIoTime(long nanos) {
        assert inEventLoop();
        if (nanos > 0) {
            accumulatedActiveTimeNanos += nanos;
            lastActivityTimeNanos = ticker().nanoTime();
        }
    }

    /**
     * Returns the accumulated active time since the last call and resets the counter.
     */
    /** 原子读取并重置累计活跃时间。 */
    protected long getAndResetAccumulatedActiveTimeNanos() {
        return ACCUMULATED_ACTIVE_TIME_NANOS_UPDATER.getAndSet(this, 0);
    }

    /**
     * Returns the timestamp of the last known activity (tasks + I/O).
     */
    protected long getLastActivityTimeNanos() {
        return lastActivityTimeNanos;
    }

    /**
     * Atomically increments the counter for consecutive monitor cycles where utilization was below the
     * scale-down threshold. This is used by the auto-scaling monitor to track sustained idleness.
     *
     * @return The number of consecutive idle cycles before the increment.
     */
    /** 连续低利用率监控周期 +1，供缩容决策。 */
    protected int getAndIncrementIdleCycles() {
        return CONSECUTIVE_IDLE_CYCLES_UPDATER.getAndIncrement(this);
    }

    /**
     * Resets the counter for consecutive idle cycles to zero. This is typically called when the
     * executor's utilization is no longer considered idle, breaking the streak.
     */
    protected void resetIdleCycles() {
        CONSECUTIVE_IDLE_CYCLES_UPDATER.set(this, 0);
    }

    /**
     * Atomically increments the counter for consecutive monitor cycles where utilization was above the
     * scale-up threshold. This is used by the auto-scaling monitor to track a sustained high load.
     *
     * @return The number of consecutive busy cycles before the increment.
     */
    /** 连续高利用率监控周期 +1，供扩容决策。 */
    protected int getAndIncrementBusyCycles() {
        return CONSECUTIVE_BUSY_CYCLES_UPDATER.getAndIncrement(this);
    }

    /**
     * Resets the counter for consecutive busy cycles to zero. This is typically called when the
     * executor's utilization is no longer considered busy, breaking the streak.
     */
    protected void resetBusyCycles() {
        CONSECUTIVE_BUSY_CYCLES_UPDATER.set(this, 0);
    }

    /**
     * Returns {@code true} if this {@link SingleThreadEventExecutor} supports suspension.
     */
    protected boolean isSuspensionSupported() {
        return supportSuspension;
    }

    /**
     * Runs the task-processing loop until {@link #confirmShutdown()} returns {@code true}.
     *
     * <p>Implementations <strong>must not let a {@link Throwable} thrown by a task escape this
     * method</strong>: any uncaught {@link Throwable} terminates the executor (logged at {@code WARN}
     * and surfaced via {@link #terminationFuture()}), at which point every {@code Channel}
     * registered with this executor stops processing I/O and new task submissions are rejected.
     * The supplied helpers - {@link #runAllTasks()}, {@link #runAllTasks(long)}, and
     * {@link #safeExecute(Runnable)} - catch {@code Throwable} for you; custom loops built on
     * {@link #pollTask()} or {@link #takeTask()} are responsible for wrapping each task
     * invocation accordingly.
     */
    /**
     * 子类实现的主循环，须反复调用 {@link #runAllTasks()} 等直至 {@link #confirmShutdown()} 为 true。
     * 任务 Throwable 不得逃逸，否则 Executor 终止、Channel 停止 I/O；可用 {@link #safeExecute(Runnable)}。
     */
    protected abstract void run();

    /**
     * Do nothing, sub-classes may override
     */
    protected void cleanup() {
        // NOOP
    }

    /** 非 EventLoop 线程向队列 offer {@link #WAKEUP_TASK} 以唤醒阻塞在 take/poll 的线程。 */
    protected void wakeup(boolean inEventLoop) {
        if (!inEventLoop) {
            // offer 失败也无妨，队列中已有任务会自然唤醒
            // is already something in the queue.
            taskQueue.offer(WAKEUP_TASK);
        }
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return thread == this.thread;
    }

    /**
     * Add a {@link Runnable} which will be executed on shutdown of this instance
     */
    /** 注册关闭钩子；非 EventLoop 线程则 execute 到 EventLoop 再添加。 */
    public void addShutdownHook(final Runnable task) {
        if (inEventLoop()) {
            shutdownHooks.add(task);
        } else {
            execute(new Runnable() {
                @Override
                public void run() {
                    shutdownHooks.add(task);
                }
            });
        }
    }

    /**
     * Remove a previous added {@link Runnable} as a shutdown hook
     */
    public void removeShutdownHook(final Runnable task) {
        if (inEventLoop()) {
            shutdownHooks.remove(task);
        } else {
            execute(new Runnable() {
                @Override
                public void run() {
                    shutdownHooks.remove(task);
                }
            });
        }
    }

    /** 执行全部 shutdown hooks（执行期间可再注册/移除）。 */
    private boolean runShutdownHooks() {
        boolean ran = false;
        // Note shutdown hooks can add / remove shutdown hooks.
        while (!shutdownHooks.isEmpty()) {
            List<Runnable> copy = new ArrayList<Runnable>(shutdownHooks);
            shutdownHooks.clear();
            for (Runnable task: copy) {
                try {
                    runTask(task);
                } catch (Throwable t) {
                    logger.warn("Shutdown hook raised an exception.", t);
                } finally {
                    ran = true;
                }
            }
        }

        if (ran) {
            lastExecutionTime = getCurrentTimeNanos();
        }

        return ran;
    }

    /** 内部关闭：CAS 更新 state、记录 quiet/timeout、必要时 wakeup 与启动线程。 */
    private void shutdown0(long quietPeriod, long timeout, int shutdownState) {
        if (isShuttingDown()) {
            return;
        }

        boolean inEventLoop = inEventLoop();
        boolean wakeup;
        int oldState;
        for (;;) {
            if (isShuttingDown()) {
                return;
            }
            int newState;
            wakeup = true;
            oldState = state;
            if (inEventLoop) {
                newState = shutdownState;
            } else {
                switch (oldState) {
                    case ST_NOT_STARTED:
                    case ST_STARTED:
                    case ST_SUSPENDING:
                    case ST_SUSPENDED:
                        newState = shutdownState;
                        break;
                    default:
                        newState = oldState;
                        wakeup = false;
                }
            }
            if (STATE_UPDATER.compareAndSet(this, oldState, newState)) {
                break;
            }
        }
        if (quietPeriod != -1) {
            gracefulShutdownQuietPeriod = quietPeriod;
        }
        if (timeout != -1) {
            gracefulShutdownTimeout = timeout;
        }

        if (ensureThreadStarted(oldState)) {
            return;
        }

        if (wakeup) {
            taskQueue.offer(WAKEUP_TASK);
            if (!addTaskWakesUp) {
                wakeup(inEventLoop);
            }
        }
    }

    @Override
    /** 进入 ST_SHUTTING_DOWN，在 quietPeriod 无新任务且超时后终止。 */
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
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
    public Future<?> terminationFuture() {
        return terminationFuture;
    }

    @Override
    @Deprecated
    public void shutdown() {
        shutdown0(-1, -1, ST_SHUTDOWN);
    }

    @Override
    public boolean isShuttingDown() {
        return state >= ST_SHUTTING_DOWN;
    }

    @Override
    public boolean isShutdown() {
        return state >= ST_SHUTDOWN;
    }

    @Override
    public boolean isTerminated() {
        return state == ST_TERMINATED;
    }

    @Override
    public boolean isSuspended() {
        int currentState = state;
        return currentState == ST_SUSPENDED || currentState == ST_SUSPENDING;
    }

    @Override
    /** 若支持挂起，CAS 到 SUSPENDING 并 wakeup，待 run 循环确认后进入 SUSPENDED 释放线程。 */
    public boolean trySuspend() {
        if (supportSuspension) {
            if (STATE_UPDATER.compareAndSet(this, ST_STARTED, ST_SUSPENDING)) {
                wakeup(inEventLoop());
                return true;
            } else if (STATE_UPDATER.compareAndSet(this, ST_NOT_STARTED, ST_SUSPENDED)) {
                return true;
            }
            int currentState = state;
            return currentState == ST_SUSPENDED || currentState == ST_SUSPENDING;
        }
        return false;
    }

    /**
     * Returns {@code true} if this {@link SingleThreadEventExecutor} can be suspended at the moment, {@code false}
     * otherwise.
     *
     * @return  if suspension is possible at the moment.
     */
    protected boolean canSuspend() {
        return canSuspend(state);
    }

    /**
     * Returns {@code true} if this {@link SingleThreadEventExecutor} can be suspended at the moment, {@code false}
     * otherwise.
     *
     * Subclasses might override this method to add extra checks.
     *
     * @param   state   the current internal state of the {@link SingleThreadEventExecutor}.
     * @return          if suspension is possible at the moment.
     */
    /** 无普通/定时任务且处于挂起相关状态时可真正 suspend。 */
    protected boolean canSuspend(int state) {
        assert inEventLoop();
        return supportSuspension && (state == ST_SUSPENDED || state == ST_SUSPENDING)
                && !hasTasks() && nextScheduledTaskDeadlineNanos() == -1;
    }

    /**
     * Confirm that the shutdown if the instance should be done now!
     */
    /** EventLoop 内调用：跑完任务与 hooks 后，根据 quietPeriod/timeout 判断是否可退出主循环。 */
    protected boolean confirmShutdown() {
        if (!isShuttingDown()) {
            return false;
        }

        if (!inEventLoop()) {
            throw new IllegalStateException("must be invoked from an event loop");
        }

        cancelScheduledTasks();

        if (gracefulShutdownStartTime == 0) {
            gracefulShutdownStartTime = getCurrentTimeNanos();
        }

        if (runAllTasks() || runShutdownHooks()) {
            if (isShutdown()) {
                // Executor shut down - no new tasks anymore.
                return true;
            }

            // 队列仍有任务，继续唤醒并等待 quiet period（#4241）
            // terminate if the quiet period is 0.
            // See https://github.com/netty/netty/issues/4241
            if (gracefulShutdownQuietPeriod == 0) {
                return true;
            }
            taskQueue.offer(WAKEUP_TASK);
            return false;
        }

        final long nanoTime = getCurrentTimeNanos();

        if (isShutdown() || nanoTime - gracefulShutdownStartTime > gracefulShutdownTimeout) {
            return true;
        }

        if (nanoTime - lastExecutionTime <= gracefulShutdownQuietPeriod) {
            // 静默期内每 100ms 唤醒检查是否有新任务入队
            // TODO: Change the behavior of takeTask() so that it returns on timeout.
            taskQueue.offer(WAKEUP_TASK);
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
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        ObjectUtil.checkNotNull(unit, "unit");
        if (inEventLoop()) {
            throw new IllegalStateException("cannot await termination of the current thread");
        }

        threadLock.await(timeout, unit);

        return isTerminated();
    }

    @Override
    public void execute(Runnable task) {
        execute0(task);
    }

    @Override
    public void lazyExecute(Runnable task) {
        lazyExecute0(task);
    }

    private void execute0(@Schedule Runnable task) {
        ObjectUtil.checkNotNull(task, "task");
        execute(task, wakesUpForTask(task));
    }

    private void lazyExecute0(@Schedule Runnable task) {
        execute(ObjectUtil.checkNotNull(task, "task"), false);
    }

    @Override
    /** 取消定时任务：SUSPENDED 时通过 execute 恢复挂起状态，否则直接 execute task 自移除。 */
    void scheduleRemoveScheduled(final ScheduledFutureTask<?> task) {
        ObjectUtil.checkNotNull(task, "task");
        int currentState = state;
        if (supportSuspension && currentState == ST_SUSPENDED) {
            // In the case of scheduling for removal we need to also ensure we will recover the "suspend" state
            // after it if it was set before. Otherwise we will always end up "unsuspending" things on cancellation
            // which is not optimal.
            execute(new Runnable() {
                @Override
                public void run() {
                    task.run();
                    if (canSuspend(ST_SUSPENDED)) {
                        // Try suspending again to recover the state before we submitted the new task that will
                        // handle cancellation itself.
                        trySuspend();
                    }
                }
            }, true);
        } else {
            // task will remove itself from scheduled task queue when it runs
            execute(task, false);
        }
    }

    /** 入队、必要时 startThread；shutdown 后尝试 remove 并 reject；按 addTaskWakesUp 与 immediate 决定是否 wakeup。 */
    private void execute(Runnable task, boolean immediate) {
        boolean inEventLoop = inEventLoop();
        addTask(task);
        if (!inEventLoop) {
            startThread();
            if (isShutdown()) {
                boolean reject = false;
                try {
                    if (removeTask(task)) {
                        reject = true;
                    }
                } catch (UnsupportedOperationException e) {
                    // 队列不支持 remove 时只能继续，最坏在终止时打日志
                    // hope we will be able to pick-up the task before its completely terminated.
                    // In worst case we will log on termination.
                }
                if (reject) {
                    reject();
                }
            }
        }

        if (!addTaskWakesUp && immediate) {
            wakeup(inEventLoop);
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throwIfInEventLoop("invokeAny");
        return super.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        throwIfInEventLoop("invokeAny");
        return super.invokeAny(tasks, timeout, unit);
    }

    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        throwIfInEventLoop("invokeAll");
        return super.invokeAll(tasks);
    }

    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        throwIfInEventLoop("invokeAll");
        return super.invokeAll(tasks, timeout, unit);
    }

    private void throwIfInEventLoop(String method) {
        if (inEventLoop()) {
            throw new RejectedExecutionException("Calling " + method + " from within the EventLoop is not allowed");
        }
    }

    /**
     * Returns the {@link ThreadProperties} of the {@link Thread} that powers the {@link SingleThreadEventExecutor}.
     * If the {@link SingleThreadEventExecutor} is not started yet, this operation will start it and block until
     * it is fully started.
     */
    /** 懒启动线程并缓存 {@link DefaultThreadProperties} 快照。 */
    public final ThreadProperties threadProperties() {
        ThreadProperties threadProperties = this.threadProperties;
        if (threadProperties == null) {
            Thread thread = this.thread;
            if (thread == null) {
                assert !inEventLoop();
                submit(NOOP_TASK).syncUninterruptibly();
                thread = this.thread;
                assert thread != null;
            }

            threadProperties = new DefaultThreadProperties(thread);
            if (!PROPERTIES_UPDATER.compareAndSet(this, null, threadProperties)) {
                threadProperties = this.threadProperties;
            }
        }

        return threadProperties;
    }

    /**
     * @deprecated override {@link SingleThreadEventExecutor#wakesUpForTask} to re-create this behaviour
     */
    @Deprecated
    protected interface NonWakeupRunnable extends LazyRunnable { }

    /**
     * Can be overridden to control which tasks require waking the {@link EventExecutor} thread
     * if it is waiting so that they can be run immediately.
     */
    /** 子类可覆写：该任务是否需要立即 wakeup 等待中的 EventLoop。 */
    protected boolean wakesUpForTask(Runnable task) {
        return true;
    }

    protected static void reject() {
        throw new RejectedExecutionException("event executor terminated");
    }

    /**
     * Offers the task to the associated {@link RejectedExecutionHandler}.
     *
     * @param task to reject.
     */
    /** 委托 {@link RejectedExecutionHandler#rejected}。 */
    protected final void reject(Runnable task) {
        rejectedExecutionHandler.rejected(task, this);
    }

    // ScheduledExecutorService implementation

    private static final long SCHEDULE_PURGE_INTERVAL = TimeUnit.SECONDS.toNanos(1);

    /** 从 NOT_STARTED/SUSPENDED CAS 到 STARTED 并 {@link #doStartThread()}。 */
    private void startThread() {
        int currentState = state;
        if (currentState == ST_NOT_STARTED || currentState == ST_SUSPENDED) {
            if (STATE_UPDATER.compareAndSet(this, currentState, ST_STARTED)) {
                resetIdleCycles();
                resetBusyCycles();
                boolean success = false;
                try {
                    doStartThread();
                    success = true;
                } finally {
                    if (!success) {
                        STATE_UPDATER.compareAndSet(this, ST_STARTED, ST_NOT_STARTED);
                    }
                }
            }
        }
    }

    private boolean ensureThreadStarted(int oldState) {
        if (oldState == ST_NOT_STARTED || oldState == ST_SUSPENDED) {
            try {
                doStartThread();
            } catch (Throwable cause) {
                STATE_UPDATER.set(this, ST_TERMINATED);
                terminationFuture.tryFailure(cause);

                if (!(cause instanceof Exception)) {
                    // Also rethrow as it may be an OOME for example
                    PlatformDependent.throwException(cause);
                }
                return true;
            }
        }
        return false;
    }

    /** 在 executor 上启动 run 循环：处理挂起、confirmShutdown 关闭序列、FastThreadLocal 清理与 terminationFuture。 */
    private void doStartThread() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                processingLock.lock();
                assert thread == null;
                thread = Thread.currentThread();
                if (interrupted) {
                    thread.interrupt();
                    interrupted = false;
                }
                boolean success = false;
                Throwable unexpectedException = null;
                updateLastExecutionTime();
                boolean suspend = false;
                try {
                    for (;;) {
                        SingleThreadEventExecutor.this.run();
                        success = true;

                        int currentState = state;
                        if (canSuspend(currentState)) {
                            if (!STATE_UPDATER.compareAndSet(SingleThreadEventExecutor.this,
                                    ST_SUSPENDING, ST_SUSPENDED)) {
                                // CAS 失败说明状态已变，重新进入 run 循环
                                continue;
                            }

                            if (!canSuspend(ST_SUSPENDED) && STATE_UPDATER.compareAndSet(SingleThreadEventExecutor.this,
                                        ST_SUSPENDED, ST_STARTED)) {
                                // 挂起期间又有任务入队，恢复 ST_STARTED 继续 event loop
                                // were able to re-engage this thread as the event loop thread.
                                continue;
                            }
                            suspend = true;
                        }
                        break;
                    }
                } catch (Throwable t) {
                    unexpectedException = t;
                    logger.warn("Unexpected exception from an event executor: ", t);
                } finally {
                    boolean shutdown = !suspend;
                    if (shutdown) {
                        for (;;) {
                            // We are re-fetching the state as it might have been shutdown in the meantime.
                            int oldState = state;
                            if (oldState >= ST_SHUTTING_DOWN || STATE_UPDATER.compareAndSet(
                                    SingleThreadEventExecutor.this, oldState, ST_SHUTTING_DOWN)) {
                                break;
                            }
                        }
                        if (success && gracefulShutdownStartTime == 0) {
                            // Check if confirmShutdown() was called at the end of the loop.
                            if (logger.isErrorEnabled()) {
                                logger.error("Buggy " + EventExecutor.class.getSimpleName() + " implementation; " +
                                        SingleThreadEventExecutor.class.getSimpleName() + ".confirmShutdown() must " +
                                        "be called before run() implementation terminates.");
                            }
                        }
                    }

                    try {
                        if (shutdown) {
                            // 仍处 ST_SHUTTING_DOWN，可继续接受任务以满足 graceful quietPeriod
                            // is in ST_SHUTTING_DOWN state still accepting tasks which is needed for
                            // graceful shutdown with quietPeriod.
                            for (;;) {
                                if (confirmShutdown()) {
                                    break;
                                }
                            }

                            // 切换到 ST_SHUTDOWN，此后新提交将被拒绝
                            // achieved by switching the state. Any new tasks beyond this point will be rejected.
                            for (;;) {
                                int currentState = state;
                                if (currentState >= ST_SHUTDOWN || STATE_UPDATER.compareAndSet(
                                        SingleThreadEventExecutor.this, currentState, ST_SHUTDOWN)) {
                                    break;
                                }
                            }

                            // We have the final set of tasks in the queue now, no more can be added, run all remaining.
                            // No need to loop here, this is the final pass.
                            confirmShutdown();
                        }
                    } finally {
                        try {
                            if (shutdown) {
                                try {
                                    cleanup();
                                } finally {
                                    // 终止前清理 FastThreadLocal，避免 JVM 卸载类时用户仍阻塞在 terminationFuture（#6596）
                                    // notify the future. The user may block on the future and once it unblocks the JVM
                                    // may terminate and start unloading classes.
                                    // See https://github.com/netty/netty/issues/6596.
                                    FastThreadLocal.removeAll();

                                    STATE_UPDATER.set(SingleThreadEventExecutor.this, ST_TERMINATED);
                                    threadLock.countDown();
                                    int numUserTasks = drainTasks();
                                    if (numUserTasks > 0 && logger.isWarnEnabled()) {
                                        logger.warn("An event executor terminated with " +
                                                "non-empty task queue (" + numUserTasks + ')');
                                    }
                                    if (unexpectedException == null) {
                                        terminationFuture.setSuccess(null);
                                    } else {
                                        terminationFuture.setFailure(unexpectedException);
                                    }
                                }
                            } else {
                                // Lets remove all FastThreadLocals for the Thread as we are about to terminate it.
                                FastThreadLocal.removeAll();

                                // 挂起释放线程时清空 threadProperties 缓存
                                threadProperties = null;
                            }
                        } finally {
                            thread = null;
                            // 释放 processingLock，允许下次 doStartThread 绑定新 thread
                            processingLock.unlock();
                        }
                    }
                }
            }
        });
    }

    /** 终止时排空队列并统计未执行的用户任务数（忽略 WAKEUP_TASK）。 */
    final int drainTasks() {
        int numTasks = 0;
        for (;;) {
            Runnable runnable = taskQueue.poll();
            if (runnable == null) {
                break;
            }
            // WAKEUP_TASK should be just discarded as these are added internally.
            // The important bit is that we not have any user tasks left.
            if (WAKEUP_TASK != runnable) {
                numTasks++;
            }
        }
        return numTasks;
    }

    /** {@link ThreadProperties} 的线程快照实现。 */
    private static final class DefaultThreadProperties implements ThreadProperties {
        private final Thread t;

        DefaultThreadProperties(Thread t) {
            this.t = t;
        }

        @Override
        public State state() {
            return t.getState();
        }

        @Override
        public int priority() {
            return t.getPriority();
        }

        @Override
        public boolean isInterrupted() {
            return t.isInterrupted();
        }

        @Override
        public boolean isDaemon() {
            return t.isDaemon();
        }

        @Override
        public String name() {
            return t.getName();
        }

        @Override
        public long id() {
            return t.getId();
        }

        @Override
        public StackTraceElement[] stackTrace() {
            return t.getStackTrace();
        }

        @Override
        public boolean isAlive() {
            return t.isAlive();
        }
    }
}
