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
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Scheduler;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.internal.functions.ObjectHelper;

/**
 * 固定大小的工作线程池 Scheduler：以轮询方式为
 * 请求的 {@link Scheduler.Worker} 分配底层 PoolWorker。
 * 支持 {@link SchedulerMultiWorkerSupport} 批量创建 Worker。
 */
public final class ComputationScheduler extends Scheduler implements SchedulerMultiWorkerSupport {
    /** 表示当前无活动线程池（已 shutdown）。 */
    static final FixedSchedulerPool NONE;
    /** 管理固定数量的 PoolWorker 事件循环。 */
    private static final String THREAD_NAME_PREFIX = "RxComputationThreadPool";
    static final RxThreadFactory THREAD_FACTORY;
    /**
     * 系统属性键：设置 computation 调度器最大线程数。
     * 0 或更小表示使用可用 CPU 数，且不超过 availableProcessors。
     */
    static final String KEY_MAX_THREADS = "rxjava4.computation-threads";
    /** computation 调度器线程池上限。 */
    static final int MAX_THREADS;

    static final PoolWorker SHUTDOWN_WORKER;

    final ThreadFactory threadFactory;
    final AtomicReference<FixedSchedulerPool> pool;
    /** 设置本 Scheduler 线程优先级的系统属性键。 */
    private static final String KEY_COMPUTATION_PRIORITY = "rxjava4.computation-priority";

    static {
        MAX_THREADS = cap(Runtime.getRuntime().availableProcessors(), Integer.getInteger(KEY_MAX_THREADS, 0));

        SHUTDOWN_WORKER = new PoolWorker(new RxThreadFactory("RxComputationShutdown"));
        SHUTDOWN_WORKER.dispose();

        int priority = Math.clamp(
                Integer.getInteger(KEY_COMPUTATION_PRIORITY, Thread.NORM_PRIORITY), Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);

        THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX, priority, true);

        NONE = new FixedSchedulerPool(0, THREAD_FACTORY);
        NONE.shutdown();
    }

    /** 将配置线程数限制在 (0, cpuCount] 范围内。 */
    static int cap(int cpuCount, int paramThreads) {
        return paramThreads <= 0 || paramThreads > cpuCount ? cpuCount : paramThreads;
    }

    static final class FixedSchedulerPool implements SchedulerMultiWorkerSupport {
        final int cores;

        final PoolWorker[] eventLoops;
        long n;

        FixedSchedulerPool(int maxThreads, ThreadFactory threadFactory) {
            // initialize event loops
            this.cores = maxThreads;
            this.eventLoops = new PoolWorker[maxThreads];
            for (int i = 0; i < maxThreads; i++) {
                this.eventLoops[i] = new PoolWorker(threadFactory);
            }
        }

        /** 轮询返回下一个 PoolWorker；cores==0 时返回 SHUTDOWN_WORKER。 */
        public PoolWorker getEventLoop() {
            int c = cores;
            if (c == 0) {
                return SHUTDOWN_WORKER;
            }
            // simple round-robin, improvements to come
            return eventLoops[(int)(n++ % c)];
        }

        /** 依次 dispose 所有 eventLoop Worker。 */
        public void shutdown() {
            for (PoolWorker w : eventLoops) {
                w.dispose();
            }
        }

        @Override
        public void createWorkers(int number, WorkerCallback callback) {
            int c = cores;
            if (c == 0) {
                for (int i = 0; i < number; i++) {
                    callback.onWorker(i, SHUTDOWN_WORKER);
                }
            } else {
                int index = (int)n % c;
                for (int i = 0; i < number; i++) {
                    callback.onWorker(i, new EventLoopWorker(eventLoops[index]));
                    if (++index == c) {
                        index = 0;
                    }
                }
                n = index;
            }
        }
    }

    /**
     * 使用默认 THREAD_FACTORY 创建 Scheduler，
     * 池大小等于可用处理器数。
     */
    public ComputationScheduler() {
        this(THREAD_FACTORY);
    }

    /**
     * 使用指定 ThreadFactory 创建 Scheduler，池大小等于可用处理器数。
     *
     * @param threadFactory 创建工作线程的 ThreadFactory，优先于系统属性配置，不可为 null
     */
    public ComputationScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.pool = new AtomicReference<>(NONE);
        start();
    }

    /** 创建绑定单个 PoolWorker 的 EventLoopWorker。 */
    @NonNull
    @Override
    public Worker createWorker() {
        return new EventLoopWorker(pool.get().getEventLoop());
    }

    @Override
    public void createWorkers(int number, WorkerCallback callback) {
        ObjectHelper.verifyPositive(number, "number > 0 required");
        pool.get().createWorkers(number, callback);
    }

    @NonNull
    @Override
    public Disposable scheduleDirect(@NonNull Runnable run, long delay, TimeUnit unit) {
        PoolWorker w = pool.get().getEventLoop();
        return w.scheduleDirect(run, delay, unit);
    }

    @NonNull
    @Override
    public Disposable schedulePeriodicallyDirect(@NonNull Runnable run, long initialDelay, long period, TimeUnit unit) {
        PoolWorker w = pool.get().getEventLoop();
        return w.schedulePeriodicallyDirect(run, initialDelay, period, unit);
    }

    /** CAS 将 NONE 替换为新 FixedSchedulerPool；失败则 shutdown 新建池。 */
    @Override
    public void start() {
        FixedSchedulerPool update = new FixedSchedulerPool(MAX_THREADS, threadFactory);
        if (!pool.compareAndSet(NONE, update)) {
            update.shutdown();
        }
    }

    /** getAndSet(NONE) 并 shutdown 当前 FixedSchedulerPool。 */
    @Override
    public void shutdown() {
        FixedSchedulerPool curr = pool.getAndSet(NONE);
        if (curr != NONE) {
            curr.shutdown();
        }
    }

    /** 包装单个 PoolWorker：serial/timed 分别追踪即时与延迟任务。 */
    static final class EventLoopWorker extends Scheduler.Worker {
        private final ListCompositeDisposable serial;
        private final CompositeDisposable timed;
        private final ListCompositeDisposable both;
        private final PoolWorker poolWorker;

        volatile boolean disposed;

        EventLoopWorker(PoolWorker poolWorker) {
            this.poolWorker = poolWorker;
            this.serial = new ListCompositeDisposable();
            this.timed = new CompositeDisposable();
            this.both = new ListCompositeDisposable();
            this.both.add(serial);
            this.both.add(timed);
        }

        /** 置 disposed 并 dispose serial+timed 容器。 */
        @Override
        public void dispose() {
            if (!disposed) {
                disposed = true;
                both.dispose();
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        /** 无延迟 schedule：委托 poolWorker.scheduleActual(..., serial)。 */
        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable action) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            return poolWorker.scheduleActual(action, 0, TimeUnit.MILLISECONDS, serial);
        }

        /** 延迟 schedule：委托 poolWorker.scheduleActual(..., timed)。 */
        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable action, long delayTime, @NonNull TimeUnit unit) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            return poolWorker.scheduleActual(action, delayTime, unit, timed);
        }
    }

    /** 基于 NewThreadWorker 的池内工作线程。 */
    static final class PoolWorker extends NewThreadWorker {
        PoolWorker(ThreadFactory threadFactory) {
            super(threadFactory);
        }
    }
}
