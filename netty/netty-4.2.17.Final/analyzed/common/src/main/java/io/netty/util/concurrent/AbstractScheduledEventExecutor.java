/*
 * Copyright 2015 The Netty Project
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

import io.netty.util.internal.DefaultPriorityQueue;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PriorityQueue;

import java.util.Comparator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for {@link EventExecutor}s that want to support scheduling.
 *
 * <p>支持定时/周期调度的 {@link EventExecutor} 抽象基类。内部用按 deadline 排序的
 * {@link PriorityQueue} 维护 {@link ScheduledFutureTask}；事件循环通过
 * {@link #pollScheduledTask(long)}、{@link #nextScheduledTaskNano()} 等与 I/O 等待协同。
 * 跨线程提交时通过 {@link #beforeScheduledTaskSubmitted}/{@link #afterScheduledTaskSubmitted}
 * 决定是否唤醒 EventLoop 线程。</p>
 */
public abstract class AbstractScheduledEventExecutor extends AbstractEventExecutor {
    /** 调度任务在优先级队列中的比较器（按 deadline 排序）。 */
    private static final Comparator<ScheduledFutureTask<?>> SCHEDULED_FUTURE_TASK_COMPARATOR =
            new Comparator<ScheduledFutureTask<?>>() {
                @Override
                public int compare(ScheduledFutureTask<?> o1, ScheduledFutureTask<?> o2) {
                    return o1.compareTo(o2);
                }
            };

    /** 空唤醒任务：仅用于在非 EventLoop 线程提交调度任务后唤醒事件循环。 */
    static final Runnable WAKEUP_TASK = new Runnable() {
       @Override
       public void run() { } // Do nothing
    };

    /** 按 deadline 排序的调度任务队列；懒初始化。 */
    PriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue;

    /** 为在 EventLoop 内提交的调度任务分配唯一 id（用于 tie-break）。 */
    long nextTaskId;

    protected AbstractScheduledEventExecutor() {
    }

    protected AbstractScheduledEventExecutor(EventExecutorGroup parent) {
        super(parent);
    }

    @Override
    public Ticker ticker() {
        return Ticker.systemTicker();
    }

    /**
     * Get the current time in nanoseconds by this executor's clock. This is not the same as {@link System#nanoTime()}
     * for two reasons:
     *
     * <ul>
     *     <li>We apply a fixed offset to the {@link System#nanoTime() nanoTime}</li>
     *     <li>Implementations (in particular EmbeddedEventLoop) may use their own time source so they can control time
     *     for testing purposes.</li>
     * </ul>
     *
     * @deprecated Please use (or override) {@link #ticker()} instead. This method delegates to {@link #ticker()}. Old
     * code may still call this method for compatibility.
     *
     * <p>返回执行器时钟的当前纳秒时间，可能与 {@link System#nanoTime()} 不同（固定偏移或测试用虚拟时钟）。
     * 已废弃，请改用 {@link #ticker()}。</p>
     */
    @Deprecated
    protected long getCurrentTimeNanos() {
        return ticker().nanoTime();
    }

    /**
     * @deprecated Use the non-static {@link #ticker()} instead.
     *
     * <p>静态版纳秒时间，已废弃。</p>
     */
    @Deprecated
    protected static long nanoTime() {
        return Ticker.systemTicker().nanoTime();
    }

    /**
     * @deprecated Use the non-static {@link #ticker()} instead.
     */
    @Deprecated
    static long defaultCurrentTimeNanos() {
        return Ticker.systemTicker().nanoTime();
    }

    /** 由当前时间与 delay 计算 deadline；溢出时钳制为 {@link Long#MAX_VALUE}。 */
    static long deadlineNanos(long nanoTime, long delay) {
        long deadlineNanos = nanoTime + delay;
        // Guard against overflow
        return deadlineNanos < 0 ? Long.MAX_VALUE : deadlineNanos;
    }

    /**
     * Given an arbitrary deadline {@code deadlineNanos}, calculate the number of nano seconds from now
     * {@code deadlineNanos} would expire.
     * @param deadlineNanos An arbitrary deadline in nano seconds.
     * @return the number of nano seconds from now {@code deadlineNanos} would expire.
     * @deprecated Use {@link #ticker()} instead
     *
     * <p>计算从现在到给定 deadline 的剩余纳秒数。</p>
     */
    @Deprecated
    protected static long deadlineToDelayNanos(long deadlineNanos) {
        return ScheduledFutureTask.deadlineToDelayNanos(defaultCurrentTimeNanos(), deadlineNanos);
    }

    /**
     * Returns the amount of time left until the scheduled task with the closest dead line is executed.
     *
     * <p>返回距最近到期调度任务的等待纳秒数；无任务时返回 {@code scheduledPurgeInterval}。</p>
     */
    protected long delayNanos(long currentTimeNanos, long scheduledPurgeInterval) {
        currentTimeNanos -= ticker().initialNanoTime();

        ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
        if (scheduledTask == null) {
            return scheduledPurgeInterval;
        }

        return scheduledTask.delayNanos(currentTimeNanos);
    }

    /**
     * The initial value used for delay and computations based upon a monatomic time source.
     * @return initial value used for delay and computations based upon a monatomic time source.
     * @deprecated Use {@link #ticker()} instead
     *
     * <p>单调时钟的初始纳秒偏移。</p>
     */
    @Deprecated
    protected static long initialNanoTime() {
        return Ticker.systemTicker().initialNanoTime();
    }

    /** 懒初始化调度队列，初始容量 11（与 {@code java.util.PriorityQueue} 一致）。 */
    PriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue() {
        if (scheduledTaskQueue == null) {
            scheduledTaskQueue = new DefaultPriorityQueue<ScheduledFutureTask<?>>(
                    SCHEDULED_FUTURE_TASK_COMPARATOR,
                    // Use same initial capacity as java.util.PriorityQueue
                    11);
        }
        return scheduledTaskQueue;
    }

    private static boolean isNullOrEmpty(Queue<ScheduledFutureTask<?>> queue) {
        return queue == null || queue.isEmpty();
    }

    /**
     * Cancel all scheduled tasks.
     *
     * This method MUST be called only when {@link #inEventLoop()} is {@code true}.
     *
     * <p>取消所有已调度任务；必须在 EventLoop 线程内调用。</p>
     */
    protected void cancelScheduledTasks() {
        assert inEventLoop();
        PriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
        if (isNullOrEmpty(scheduledTaskQueue)) {
            return;
        }

        final ScheduledFutureTask<?>[] scheduledTasks =
                scheduledTaskQueue.toArray(new ScheduledFutureTask<?>[0]);

        for (ScheduledFutureTask<?> task: scheduledTasks) {
            task.cancelWithoutRemove(false);
        }

        scheduledTaskQueue.clearIgnoringIndexes();
    }

    /**
     * @see #pollScheduledTask(long)
     *
     * <p>使用当前时钟取出已到期的调度任务。</p>
     */
    protected final Runnable pollScheduledTask() {
        return pollScheduledTask(getCurrentTimeNanos());
    }

    /**
     * Fetch scheduled tasks from the internal queue and add these to the given {@link Queue}.
     *
     * @param taskQueue the task queue into which the fetched scheduled tasks should be transferred.
     * @return {@code true} if we were able to transfer everything, {@code false} if we need to call this method again
     *         as soon as there is space again in {@code taskQueue}.
     *
     * <p>将已到期的调度任务移入普通任务队列；若 {@code taskQueue} 满则把任务放回并返回 {@code false}。</p>
     */
    protected boolean fetchFromScheduledTaskQueue(Queue<Runnable> taskQueue) {
        assert inEventLoop();
        Objects.requireNonNull(taskQueue, "taskQueue");
        if (scheduledTaskQueue == null || scheduledTaskQueue.isEmpty()) {
            return true;
        }
        long nanoTime = getCurrentTimeNanos();
        for (;;) {
            ScheduledFutureTask scheduledTask = (ScheduledFutureTask) pollScheduledTask(nanoTime);
            if (scheduledTask == null) {
                return true;
            }
            if (scheduledTask.isCancelled()) {
                continue;
            }
            if (!taskQueue.offer(scheduledTask)) {
                // No space left in the task queue add it back to the scheduledTaskQueue so we pick it up again.
                // 普通队列已满，任务放回调度队列，待下次再试
                scheduledTaskQueue.add(scheduledTask);
                return false;
            }
        }
    }

    /**
     * Return the {@link Runnable} which is ready to be executed with the given {@code nanoTime}.
     * You should use {@link #getCurrentTimeNanos()} to retrieve the correct {@code nanoTime}.
     *
     * <p>若队首任务 deadline 已到期则出队并返回；否则返回 {@code null}。</p>
     */
    protected final Runnable pollScheduledTask(long nanoTime) {
        assert inEventLoop();

        ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
        if (scheduledTask == null || scheduledTask.deadlineNanos() - nanoTime > 0) {
            return null;
        }
        scheduledTaskQueue.remove();
        scheduledTask.setConsumed();
        return scheduledTask;
    }

    /**
     * Return the nanoseconds until the next scheduled task is ready to be run or {@code -1} if no task is scheduled.
     *
     * <p>距下一调度任务到期的纳秒数；无任务时 {@code -1}。</p>
     */
    protected final long nextScheduledTaskNano() {
        ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
        return scheduledTask != null ? scheduledTask.delayNanos() : -1;
    }

    /**
     * Return the deadline (in nanoseconds) when the next scheduled task is ready to be run or {@code -1}
     * if no task is scheduled.
     *
     * <p>下一任务的绝对 deadline（纳秒）；无任务时 {@code -1}。</p>
     */
    protected final long nextScheduledTaskDeadlineNanos() {
        ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
        return scheduledTask != null ? scheduledTask.deadlineNanos() : -1;
    }

    final ScheduledFutureTask<?> peekScheduledTask() {
        Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
        return scheduledTaskQueue != null ? scheduledTaskQueue.peek() : null;
    }

    /**
     * Returns {@code true} if a scheduled task is ready for processing.
     *
     * <p>队首调度任务是否已到期可执行。</p>
     */
    protected final boolean hasScheduledTasks() {
        ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
        return scheduledTask != null && scheduledTask.deadlineNanos() <= getCurrentTimeNanos();
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        ObjectUtil.checkNotNull(command, "command");
        ObjectUtil.checkNotNull(unit, "unit");
        if (delay < 0) {
            delay = 0;
        }
        validateScheduled0(delay, unit);

        return schedule(new ScheduledFutureTask<Void>(
                this,
                command,
                deadlineNanos(getCurrentTimeNanos(), unit.toNanos(delay))));
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        ObjectUtil.checkNotNull(callable, "callable");
        ObjectUtil.checkNotNull(unit, "unit");
        if (delay < 0) {
            delay = 0;
        }
        validateScheduled0(delay, unit);

        return schedule(new ScheduledFutureTask<V>(
                this, callable, deadlineNanos(getCurrentTimeNanos(), unit.toNanos(delay))));
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ObjectUtil.checkNotNull(command, "command");
        ObjectUtil.checkNotNull(unit, "unit");
        if (initialDelay < 0) {
            throw new IllegalArgumentException(
                    String.format("initialDelay: %d (expected: >= 0)", initialDelay));
        }
        if (period <= 0) {
            throw new IllegalArgumentException(
                    String.format("period: %d (expected: > 0)", period));
        }
        validateScheduled0(initialDelay, unit);
        validateScheduled0(period, unit);

        return schedule(new ScheduledFutureTask<Void>(
                this, command, deadlineNanos(getCurrentTimeNanos(), unit.toNanos(initialDelay)), unit.toNanos(period)));
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        ObjectUtil.checkNotNull(command, "command");
        ObjectUtil.checkNotNull(unit, "unit");
        if (initialDelay < 0) {
            throw new IllegalArgumentException(
                    String.format("initialDelay: %d (expected: >= 0)", initialDelay));
        }
        if (delay <= 0) {
            throw new IllegalArgumentException(
                    String.format("delay: %d (expected: > 0)", delay));
        }

        validateScheduled0(initialDelay, unit);
        validateScheduled0(delay, unit);

        // 固定延迟：period 为负表示两次执行间隔而非固定频率
        return schedule(new ScheduledFutureTask<Void>(
                this, command, deadlineNanos(getCurrentTimeNanos(), unit.toNanos(initialDelay)), -unit.toNanos(delay)));
    }

    @SuppressWarnings("deprecation")
    private void validateScheduled0(long amount, TimeUnit unit) {
        validateScheduled(amount, unit);
    }

    /**
     * Sub-classes may override this to restrict the maximal amount of time someone can use to schedule a task.
     *
     * @deprecated will be removed in the future.
     *
     * <p>子类可限制最大调度延迟；默认无校验。</p>
     */
    @Deprecated
    protected void validateScheduled(long amount, TimeUnit unit) {
        // NOOP
    }

    /** 在 EventLoop 线程内直接入队；为任务分配递增 id。 */
    final void scheduleFromEventLoop(final ScheduledFutureTask<?> task) {
        // nextTaskId a long and so there is no chance it will overflow back to 0
        if (task.getId() == 0L) {
            task.setId(++nextTaskId);
        }
        scheduledTaskQueue().add(task);
    }

    /**
     * 提交调度任务：EventLoop 内直接入队；否则 execute/lazyExecute 并在需要时投递 WAKEUP_TASK。
     */
    private <V> ScheduledFuture<V> schedule(final ScheduledFutureTask<V> task) {
        if (inEventLoop()) {
            scheduleFromEventLoop(task);
        } else {
            final long deadlineNanos = task.deadlineNanos();
            // task will add itself to scheduled task queue when run if not expired
            if (beforeScheduledTaskSubmitted(deadlineNanos)) {
                execute(task);
            } else {
                lazyExecute(task);
                // Second hook after scheduling to facilitate race-avoidance
                if (afterScheduledTaskSubmitted(deadlineNanos)) {
                    execute(WAKEUP_TASK);
                }
            }
        }

        return task;
    }

    /** 取消后从调度队列移除；非 EventLoop 线程则 lazyExecute 延迟移除。 */
    final void removeScheduled(final ScheduledFutureTask<?> task) {
        assert task.isCancelled();
        if (inEventLoop()) {
            scheduledTaskQueue().removeTyped(task);
        } else {
            // task will remove itself from scheduled task queue when it runs
            scheduleRemoveScheduled(task);
        }
    }

    void scheduleRemoveScheduled(final ScheduledFutureTask<?> task) {
        // task will remove itself from scheduled task queue when it runs
        lazyExecute(task);
    }

    /**
     * Called from arbitrary non-{@link EventExecutor} threads prior to scheduled task submission.
     * Returns {@code true} if the {@link EventExecutor} thread should be woken immediately to
     * process the scheduled task (if not already awake).
     * <p>
     * If {@code false} is returned, {@link #afterScheduledTaskSubmitted(long)} will be called with
     * the same value <i>after</i> the scheduled task is enqueued, providing another opportunity
     * to wake the {@link EventExecutor} thread if required.
     *
     * @param deadlineNanos deadline of the to-be-scheduled task
     *     relative to {@link AbstractScheduledEventExecutor#getCurrentTimeNanos()}
     * @return {@code true} if the {@link EventExecutor} thread should be woken, {@code false} otherwise
     *
     * <p>跨线程提交前钩子：返回 {@code true} 时用 {@link #execute} 立即唤醒 EventLoop；
     * 返回 {@code false} 时先 lazyExecute，再由 {@link #afterScheduledTaskSubmitted} 二次决定是否唤醒。</p>
     */
    protected boolean beforeScheduledTaskSubmitted(long deadlineNanos) {
        return true;
    }

    /**
     * See {@link #beforeScheduledTaskSubmitted(long)}. Called only after that method returns false.
     *
     * @param deadlineNanos relative to {@link AbstractScheduledEventExecutor#getCurrentTimeNanos()}
     * @return  {@code true} if the {@link EventExecutor} thread should be woken, {@code false} otherwise
     *
     * <p>跨线程提交后钩子：在 lazyExecute 之后提供第二次唤醒机会，避免竞态。</p>
     */
    protected boolean afterScheduledTaskSubmitted(long deadlineNanos) {
        return true;
    }
}
