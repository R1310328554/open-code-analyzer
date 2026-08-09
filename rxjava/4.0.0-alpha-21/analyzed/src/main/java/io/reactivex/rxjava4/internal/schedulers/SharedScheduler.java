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

import java.io.Serial;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Scheduler;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.schedulers.SchedulerRunnableIntrospection;

/**
 * 共享底层 Worker 的 Scheduler：多个 SharedWorker 共用同一 worker，
 * dispose SharedWorker 不会释放共享 worker，需 {@link #shutdown()}。
 * 不支持 {@link #start()}，shutdown 后不可恢复。
 * @since 4.0.0
 */
public final class SharedScheduler extends Scheduler {

    final Worker worker;

    /**
     * 使用给定 Worker 构造 SharedScheduler。
     * @param worker 共享的 Worker，不可为 null
     */
    public SharedScheduler(Worker worker) {
        this.worker = worker;
    }

    /** dispose 共享 worker。 */
    @Override
    public void shutdown() {
        worker.dispose();
    }

    @Override
    public Disposable scheduleDirect(Runnable run) {
        return worker.schedule(run);
    }

    @Override
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        return worker.schedule(run, delay, unit);
    }

    @Override
    public Disposable schedulePeriodicallyDirect(Runnable run, long initialDelay, long period, TimeUnit unit) {
        return worker.schedulePeriodically(run, initialDelay, period, unit);
    }

    @Override
    public long now(TimeUnit unit) {
        return worker.now(unit);
    }

    /** 创建绑定同一 worker 的 SharedWorker。 */
    @Override
    public Worker createWorker() {
        return new SharedWorker(worker);
    }

    /** 在共享 worker 上调度任务，tasks 容器追踪本 Worker 的 SharedAction。 */
    static final class SharedWorker extends Worker {

        final Worker worker;

        final CompositeDisposable tasks;

        SharedWorker(Worker worker) {
            this.worker = worker;
            this.tasks = new CompositeDisposable();
        }

        @Override
        public void dispose() {
            tasks.dispose();
        }

        @Override
        public boolean isDisposed() {
            return tasks.isDisposed();
        }

        /** 创建 SharedAction 加入 tasks，委托 worker.schedule 并 setFuture。 */
        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            if (isDisposed() || worker.isDisposed()) {
                return Disposable.disposed();
            }
            SharedAction sa = new SharedAction(run, tasks);
            tasks.add(sa);

            Disposable task;
            if (delay <= 0L) {
                task = worker.schedule(sa);
            } else {
                task = worker.schedule(sa, delay, unit);
            }
            sa.setFuture(task);

            return sa;
        }

        @Override
        public long now(TimeUnit unit) {
            return worker.now(unit);
        }

        /** 可取消的共享任务：run 后 complete 从 parent 移除。 */
        static final class SharedAction
        extends AtomicReference<DisposableContainer>
        implements Runnable, Disposable, SchedulerRunnableIntrospection {
            @Serial
            private static final long serialVersionUID = 4949851341419870956L;

            final AtomicReference<Disposable> future;

            final Runnable actual;

            SharedAction(Runnable actual, DisposableContainer parent) {
                this.actual = actual;
                this.lazySet(parent);
                this.future = new AtomicReference<>();
            }

            /** 执行 actual.run()，finally 调用 complete()。 */
            @Override
            public void run() {
                try {
                    actual.run();
                } finally {
                    complete();
                }
            }

            /** 从 parent 删除自身并将 future 置为 this。 */
            void complete() {
                DisposableContainer cd = get();
                if (cd != null && compareAndSet(cd, null)) {
                    cd.delete(this);
                }
                for (;;) {
                    Disposable f = future.get();
                    if (f == DisposableHelper.DISPOSED || future.compareAndSet(f, this)) {
                        break;
                    }
                }
            }

            /** 从 parent 移除并 DisposableHelper.dispose(future)。 */
            @Override
            public void dispose() {
                DisposableContainer cd = getAndSet(null);
                if (cd != null) {
                    cd.delete(this);
                }
                DisposableHelper.dispose(future);
            }

            @Override
            public boolean isDisposed() {
                return get() == null;
            }

            void setFuture(Disposable d) {
                Disposable f = future.get();
                if (f != this) {
                    if (f == DisposableHelper.DISPOSED) {
                        d.dispose();
                    } else
                    if (!future.compareAndSet(f, d)) {
                        f = future.get();
                        if (f == DisposableHelper.DISPOSED) {
                            d.dispose();
                        }
                    }
                }
            }

            @Override
            public @NonNull Runnable getWrappedRunnable() {
                return actual;
            }
        }
    }
}
