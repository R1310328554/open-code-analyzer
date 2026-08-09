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

package io.reactivex.rxjava4.internal.schedulers;

import java.util.concurrent.*;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.core.Scheduler;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 管理单线程 {@link ScheduledExecutorService} 的 Worker 基类，
 * 提供 scheduleDirect/schedulePeriodicallyDirect/scheduleActual 等底层调度。
 */
public class NewThreadWorker extends Scheduler.Worker {
    private final ScheduledExecutorService executor;

    volatile boolean disposed;

    /** 通过 {@link SchedulerPoolFactory#create} 创建单线程 executor。 */
    public NewThreadWorker(ThreadFactory threadFactory) {
        executor = SchedulerPoolFactory.create(threadFactory);
    }

    @NonNull
    @Override
    public Disposable schedule(@NonNull final Runnable run) {
        return schedule(run, 0, null);
    }

    @NonNull
    @Override
    public Disposable schedule(@NonNull final Runnable action, long delayTime, @NonNull TimeUnit unit) {
        if (disposed) {
            return EmptyDisposable.INSTANCE;
        }
        return scheduleActual(action, delayTime, unit, null);
    }

    /**
     * 直接在底层 executor 调度 Runnable，返回 Disposable 包装的 Future。
     * @param run 待执行的 Runnable
     * @param delayTime 延迟时长
     * @param unit 时间单位
     * @return ScheduledDirectTask 实例
     */
    public Disposable scheduleDirect(final Runnable run, long delayTime, TimeUnit unit) {
        ScheduledDirectTask task = new ScheduledDirectTask(RxJavaPlugins.onSchedule(run), true);
        try {
            Future<?> f;
            if (delayTime <= 0L) {
                f = executor.submit(task);
            } else {
                f = executor.schedule(task, delayTime, unit);
            }
            task.setFuture(f);
            return task;
        } catch (RejectedExecutionException ex) {
            RxJavaPlugins.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }

    /**
     * 直接在底层 executor 周期调度 Runnable。
     * period<=0 时用 {@link InstantPeriodicTask} 链式重调度；
     * 否则 scheduleAtFixedRate + {@link ScheduledDirectPeriodicTask}。
     * @param run 周期执行的 Runnable
     * @param initialDelay 初始延迟
     * @param period 周期间隔
     * @param unit 时间单位
     * @return Disposable 包装的任务
     */
    public Disposable schedulePeriodicallyDirect(Runnable run, long initialDelay, long period, TimeUnit unit) {
        final Runnable decoratedRun = RxJavaPlugins.onSchedule(run);
        if (period <= 0L) {

            InstantPeriodicTask periodicWrapper = new InstantPeriodicTask(decoratedRun, executor);
            try {
                Future<?> f;
                if (initialDelay <= 0L) {
                    f = executor.submit(periodicWrapper);
                } else {
                    f = executor.schedule(periodicWrapper, initialDelay, unit);
                }
                periodicWrapper.setFirst(f);
            } catch (RejectedExecutionException ex) {
                RxJavaPlugins.onError(ex);
                return EmptyDisposable.INSTANCE;
            }

            return periodicWrapper;
        }
        ScheduledDirectPeriodicTask task = new ScheduledDirectPeriodicTask(decoratedRun, true);
        try {
            Future<?> f = executor.scheduleAtFixedRate(task, initialDelay, period, unit);
            task.setFuture(f);
            return task;
        } catch (RejectedExecutionException ex) {
            RxJavaPlugins.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }

    /**
     * 包装为 {@link ScheduledRunnable} 并在 executor 上调度；
     * parent 非 null 时先加入追踪容器。
     * @param run Runnable 实例
     * @param delayTime 延迟时长
     * @param unit 时间单位
     * @param parent 可选的任务追踪父容器
     * @return ScheduledRunnable 实例
     */
    @NonNull
    public ScheduledRunnable scheduleActual(final Runnable run, long delayTime, @NonNull TimeUnit unit, @Nullable DisposableContainer parent) {
        Runnable decoratedRun = RxJavaPlugins.onSchedule(run);

        ScheduledRunnable sr = new ScheduledRunnable(decoratedRun, parent);

        if (parent != null) {
            if (!parent.add(sr)) {
                return sr;
            }
        }

        Future<?> f;
        try {
            if (delayTime <= 0) {
                f = executor.submit((Callable<Object>)sr);
            } else {
                f = executor.schedule((Callable<Object>)sr, delayTime, unit);
            }
            sr.setFuture(f);
        } catch (RejectedExecutionException ex) {
            if (parent != null) {
                parent.remove(sr);
            }
            RxJavaPlugins.onError(ex);
        }

        return sr;
    }

    /** shutdownNow 并置 disposed。 */
    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            executor.shutdownNow();
        }
    }

    /**
     * 非中断方式 shutdown 底层 executor。
     */
    public void shutdown() {
        if (!disposed) {
            disposed = true;
            executor.shutdown();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
