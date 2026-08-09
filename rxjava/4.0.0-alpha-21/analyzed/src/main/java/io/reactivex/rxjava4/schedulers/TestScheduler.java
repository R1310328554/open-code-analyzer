/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava4.schedulers;

import java.io.Serial;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.core.Scheduler;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 专用于测试的、非线程安全调度器：在不引入真实并发的前提下满足算子对调度器的需求，
 * 并允许手动推进虚拟时间。
 * <p>
 * 默认情况下，经各 {@code schedule} 方法提交的任务不会经 {@link RxJavaPlugins#onSchedule(Runnable)} 包装；
 * 若需启用，请通过 {@link #TestScheduler(boolean)} 或 {@link #TestScheduler(long, TimeUnit, boolean)} 创建。
 */
public final class TestScheduler extends Scheduler {
    /** 按序存放 Runnable 任务的队列。 */
    final Queue<TimedRunnable> queue = new PriorityBlockingQueue<>(11);
    /** 调度任务时是否使用 {@link RxJavaPlugins#onSchedule(Runnable)} 钩子。 */
    final boolean useOnScheduleHook;
    /** 本调度器内全局顺序计数器。 */
    long counter;
    // Storing time in nanoseconds internally.
    volatile long time;

    /**
     * 创建虚拟时间初始为零的 TestScheduler。
     */
    public TestScheduler() {
        this(false);
    }

    /**
     * 创建 TestScheduler，可选是否在调度时使用 {@link RxJavaPlugins#onSchedule(Runnable)} 包装任务。
     * <p>History: 3.0.10 - experimental
     * @param useOnScheduleHook 为 {@code true} 时经 {@link RxJavaPlugins#onSchedule(Runnable)} 包装提交任务
     * @since 3.1.0
     */
    public TestScheduler(boolean useOnScheduleHook) {
        this.useOnScheduleHook = useOnScheduleHook;
    }

    /**
     * 以指定初始虚拟时间创建 TestScheduler。
     *
     * @param delayTime 调度器时钟要移动到的时刻
     * @param unit {@code delayTime} 的时间单位
     */
    public TestScheduler(long delayTime, TimeUnit unit) {
        this(delayTime, unit, false);
    }

    /**
     * 以指定初始虚拟时间创建 TestScheduler，并可选是否使用
     * {@link RxJavaPlugins#onSchedule(Runnable)} 包装任务。
     * <p>History: 3.0.10 - experimental
     * @param delayTime 调度器时钟要移动到的时刻
     * @param unit {@code delayTime} 的时间单位
     * @param useOnScheduleHook 为 {@code true} 时经 {@link RxJavaPlugins#onSchedule(Runnable)} 包装提交任务
     * @since 3.1.0
     */
    public TestScheduler(long delayTime, TimeUnit unit, boolean useOnScheduleHook) {
        time = unit.toNanos(delayTime);
        this.useOnScheduleHook = useOnScheduleHook;
    }

    /**
     * @param count 区分同一时刻任务的序号
     */
    record TimedRunnable(TestWorker scheduler, long time, Runnable run,
                         long count) implements Comparable<TimedRunnable> {

        @Override
            public String toString() {
                return String.format("TimedRunnable(time = %d, run = %s)", time, run.toString());
            }

            @Override
            public int compareTo(TimedRunnable o) {
                if (time == o.time) {
                    return Long.compare(count, o.count);
                }
                return Long.compare(time, o.time);
            }
        }

    @Override
    public long now(@NonNull TimeUnit unit) {
        return unit.convert(time, TimeUnit.NANOSECONDS);
    }

    /**
     * 将调度器时钟向前推进指定时长。
     *
     * @param delayTime 推进量
     * @param unit {@code delayTime} 的时间单位
     */
    public void advanceTimeBy(long delayTime, TimeUnit unit) {
        advanceTimeTo(time + unit.toNanos(delayTime), TimeUnit.NANOSECONDS);
    }

    /**
     * 将调度器时钟移动到指定时刻。
     *
     * @param delayTime 目标时刻
     * @param unit {@code delayTime} 的时间单位
     */
    public void advanceTimeTo(long delayTime, TimeUnit unit) {
        long targetTime = unit.toNanos(delayTime);
        triggerActions(targetTime);
    }

    /**
     * 触发所有尚未执行、且计划在本调度器当前时刻或之前执行的动作。
     */
    public void triggerActions() {
        triggerActions(time);
    }

    private void triggerActions(long targetTimeInNanoseconds) {
        for (;;) {
            TimedRunnable current = queue.peek();
            if (current == null || current.time > targetTimeInNanoseconds) {
                break;
            }
            // if scheduled time is 0 (immediate) use current virtual time
            time = current.time == 0 ? time : current.time;
            queue.remove(current);

            // Only execute if not unsubscribed
            if (!current.scheduler.disposed) {
                current.run.run();
            }
        }
        time = targetTimeInNanoseconds;
    }

    @NonNull
    @Override
    public Worker createWorker() {
        return new TestWorker();
    }

    final class TestWorker extends Worker {

        volatile boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable run, long delayTime, @NonNull TimeUnit unit) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }
            if (useOnScheduleHook) {
                run = RxJavaPlugins.onSchedule(run);
            }
            final TimedRunnable timedAction = new TimedRunnable(this, time + unit.toNanos(delayTime), run, counter++);
            queue.add(timedAction);

            return new QueueRemove(timedAction);
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable run) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }
            if (useOnScheduleHook) {
                run = RxJavaPlugins.onSchedule(run);
            }
            final TimedRunnable timedAction = new TimedRunnable(this, 0, run, counter++);
            queue.add(timedAction);
            return new QueueRemove(timedAction);
        }

        @Override
        public long now(@NonNull TimeUnit unit) {
            return TestScheduler.this.now(unit);
        }

        final class QueueRemove extends AtomicReference<TimedRunnable> implements Disposable {

            @Serial
            private static final long serialVersionUID = -7874968252110604360L;

            QueueRemove(TimedRunnable timedAction) {
                this.lazySet(timedAction);
            }

            @Override
            public void dispose() {
                TimedRunnable tr = getAndSet(null);
                if (tr != null) {
                    queue.remove(tr);
                }
            }

            @Override
            public boolean isDisposed() {
                return get() == null;
            }
        }
    }
}
