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

/* ===== [OCA 中文解析] =====
文件意图总览

延迟创建 Executor 的 Scheduler 包装：按需从 Supplier 获取 Executor，提供与 ExecutorScheduler 一致的 Worker 语义。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.internal.schedulers;

import java.io.Serial;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Scheduler;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.internal.functions.Functions;
import io.reactivex.rxjava4.internal.queue.MpscLinkedQueue;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;
import io.reactivex.rxjava4.schedulers.*;

/* ===== [OCA 中文解析] =====
class DeferredExecutorScheduler — 意图说明

Supplier<Executor> 延迟实例化的 Scheduler。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * 【说明】Wraps an Executor supplier and provides the Scheduler API over an instance of...
 * Wraps an Executor supplier and provides the Scheduler API over an instance of Executor
 * created on demand.
 */
public final class DeferredExecutorScheduler extends Scheduler {

    final boolean interruptibleWorker;

    final boolean fair;

    @NonNull
    final Supplier<? extends Executor> executorSupplier;

    /** 内部 SingleHolder。 */


    static final class SingleHolder {
        static final Scheduler HELPER = Schedulers.single();
    }

    public DeferredExecutorScheduler(@NonNull Supplier<? extends Executor> executorSupplier, boolean interruptibleWorker, boolean fair) {
        this.executorSupplier = executorSupplier;
        this.interruptibleWorker = interruptibleWorker;
        this.fair = fair;
    }

    @NonNull
    @Override
    /** 实例方法 Worker：测试断言或状态查询。 */

    public Worker createWorker() {
        try {
            return new ExecutorWorker(executorSupplier.get(), interruptibleWorker, fair);
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
            throw Exceptions.propagate(t);
        }
    }

    /* public: test support. */
    public /** 内部 ExecutorWorker。 */
 /** 内部 
  实现。 */

 static final class ExecutorWorker extends Scheduler.Worker implements Runnable {

        final boolean interruptibleWorker;

        final boolean fair;

        final Executor executor;

        final MpscLinkedQueue<Runnable> queue;

        volatile boolean disposed;

        final AtomicInteger wip = new AtomicInteger();

        final CompositeDisposable tasks = new CompositeDisposable();

        public ExecutorWorker(Executor executor, boolean interruptibleWorker, boolean fair) {
            this.executor = executor;
            this.queue = new MpscLinkedQueue<>();
            this.interruptibleWorker = interruptibleWorker;
            this.fair = fair;
        }

        @NonNull
        @Override
        /** 实例方法 Disposable：测试断言或状态查询。 */

        public Disposable schedule(@NonNull Runnable run) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            Runnable decoratedRun = RxJavaPlugins.onSchedule(run);

            Runnable task;
            Disposable disposable;

            if (interruptibleWorker) {
                InterruptibleRunnable interruptibleTask = new InterruptibleRunnable(decoratedRun, tasks);
                tasks.add(interruptibleTask);

                task = interruptibleTask;
                disposable = interruptibleTask;
            } else {
                BooleanRunnable runnableTask = new BooleanRunnable(decoratedRun);

                task = runnableTask;
                disposable = runnableTask;
            }

            queue.offer(task);

            if (wip.getAndIncrement() == 0) {
                try {
                    executor.execute(this);
                } catch (RejectedExecutionException ex) {
                    disposed = true;
                    queue.clear();
                    RxJavaPlugins.onError(ex);
                    return EmptyDisposable.INSTANCE;
                }
            }

            return disposable;
        }

        @NonNull
        @Override
        /** 实例方法 Disposable：测试断言或状态查询。 */

        public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
            if (delay <= 0) {
                return schedule(run);
            }
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            SequentialDisposable first = new SequentialDisposable();

            final SequentialDisposable mar = new SequentialDisposable(first);

            final Runnable decoratedRun = RxJavaPlugins.onSchedule(run);

            ScheduledRunnable sr = new ScheduledRunnable(new SequentialDispose(mar, decoratedRun), tasks, interruptibleWorker);
            tasks.add(sr);

            if (executor instanceof ScheduledExecutorService) {
                try {
                    Future<?> f = ((ScheduledExecutorService)executor).schedule((Callable<Object>)sr, delay, unit);
                    sr.setFuture(f);
                } catch (RejectedExecutionException ex) {
                    disposed = true;
                    RxJavaPlugins.onError(ex);
                    return EmptyDisposable.INSTANCE;
                }
            } else {
                final Disposable d = SingleHolder.HELPER.scheduleDirect(sr, delay, unit);
                sr.setFuture(new DisposeOnCancel(d));
            }

            first.replace(sr);

            return mar;
        }

        @Override
        /** 实例方法 void：测试断言或状态查询。 */

        public void dispose() {
            if (!disposed) {
                disposed = true;
                try {
                    tasks.dispose();
                    if (wip.getAndIncrement() == 0) {
                        queue.clear();
                    }
                } finally {
                    if (executor instanceof ExecutorService exec) {
                        exec.shutdown();
                    }
                }
            }
        }

        @Override
        /** 实例方法 boolean：测试断言或状态查询。 */

        public boolean isDisposed() {
            return disposed;
        }

        @Override
        /** 实例方法 void：测试断言或状态查询。 */

        public void run() {
            if (fair) {
                runFair();
            } else {
                runEager();
            }
        }

        void runFair() {
            final MpscLinkedQueue<Runnable> q = queue;
            if (disposed) {
                q.clear();
                return;
            }

            Runnable run = q.poll();
            run.run(); // never null because of offer + increment happens first

            if (disposed) {
                q.clear();
                return;
            }

            if (wip.decrementAndGet() != 0) {
                executor.execute(this);
            }
        }

        void runEager() {
            int missed = 1;
            final MpscLinkedQueue<Runnable> q = queue;
            for (;;) {

                if (disposed) {
                    q.clear();
                    return;
                }

                for (;;) {
                    Runnable run = q.poll();
                    if (run == null) {
                        break;
                    }
                    run.run();

                    if (disposed) {
                        q.clear();
                        return;
                    }
                }

                if (disposed) {
                    q.clear();
                    return;
                }

                missed = wip.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        /** 内部 BooleanRunnable。 */


        /** 内部 


         实现。 */



        static final class BooleanRunnable extends AtomicBoolean implements Runnable, Disposable {

            @Serial
            private static final long serialVersionUID = -2421395018820541164L;

            final Runnable actual;
            BooleanRunnable(Runnable actual) {
                this.actual = actual;
            }

            @Override
            /** 实例方法 void：测试断言或状态查询。 */

            public void run() {
                if (get()) {
                    return;
                }
                try {
                    actual.run();
                } catch (Throwable ex) {
                    // Exceptions.throwIfFatal(ex); nowhere to go
                    RxJavaPlugins.onError(ex);
                    throw ex;
                } finally {
                    lazySet(true);
                }
            }

            @Override
            /** 实例方法 void：测试断言或状态查询。 */

            public void dispose() {
                lazySet(true);
            }

            @Override
            /** 实例方法 boolean：测试断言或状态查询。 */

            public boolean isDisposed() {
                return get();
            }
        }

        final class SequentialDispose implements Runnable {
            private final SequentialDisposable mar;
            private final Runnable decoratedRun;

            SequentialDispose(SequentialDisposable mar, Runnable decoratedRun) {
                this.mar = mar;
                this.decoratedRun = decoratedRun;
            }

            @Override
            /** 实例方法 void：测试断言或状态查询。 */

            public void run() {
                mar.replace(schedule(decoratedRun));
            }
        }

        /**
 * 【说明】Wrapper for a {@link Runnable} with additional logic for handling interruptio...
         * Wrapper for a {@link Runnable} with additional logic for handling interruption on
         * a shared thread, similar to how Java Executors do it.
         */
        /** 内部 InterruptibleRunnable。 */

        /** 内部 

         实现。 */


        static final class InterruptibleRunnable extends AtomicInteger implements Runnable, Disposable {

            @Serial
            private static final long serialVersionUID = -3603436687413320876L;

            final Runnable run;

            final DisposableContainer tasks;

            volatile Thread thread;

            static final int READY = 0;

            static final int RUNNING = 1;

            static final int FINISHED = 2;

            static final int INTERRUPTING = 3;

            static final int INTERRUPTED = 4;

            InterruptibleRunnable(Runnable run, DisposableContainer tasks) {
                this.run = run;
                this.tasks = tasks;
            }

            @Override
            /** 实例方法 void：测试断言或状态查询。 */

            public void run() {
                if (get() == READY) {
                    thread = Thread.currentThread();
                    if (compareAndSet(READY, RUNNING)) {
                        try {
                            try {
                                run.run();
                            } catch (Throwable ex) {
                                // Exceptions.throwIfFatal(ex); nowhere to go
                                RxJavaPlugins.onError(ex);
                                throw ex;
                            }
                        } finally {
                            thread = null;
                            if (compareAndSet(RUNNING, FINISHED)) {
                                cleanup();
                            } else {
                                while (get() == INTERRUPTING) {
                                    Thread.yield();
                                }
                                Thread.interrupted();
                            }
                        }
                    } else {
                        thread = null;
                    }
                }
            }

            @Override
            /** 实例方法 void：测试断言或状态查询。 */

            public void dispose() {
                for (;;) {
                    int state = get();
                    if (state >= FINISHED) {
                        break;
                    } else if (state == READY) {
                        if (compareAndSet(READY, INTERRUPTED)) {
                            cleanup();
                            break;
                        }
                    } else {
                        if (compareAndSet(RUNNING, INTERRUPTING)) {
                            Thread t = thread;
                            if (t != null) {
                                t.interrupt();
                                thread = null;
                            }
                            set(INTERRUPTED);
                            cleanup();
                            break;
                        }
                    }
                }
            }

            void cleanup() {
                if (tasks != null) {
                    tasks.delete(this);
                }
            }

            @Override
            /** 实例方法 boolean：测试断言或状态查询。 */

            public boolean isDisposed() {
                return get() >= FINISHED;
            }
        }
    }

    /** 内部 DelayedRunnable。 */


    /** 内部 


     实现。 */



    static final class DelayedRunnable extends AtomicReference<Runnable>
            implements Runnable, Disposable, SchedulerRunnableIntrospection {

        @Serial
        private static final long serialVersionUID = -4101336210206799084L;

        final SequentialDisposable timed;

        final SequentialDisposable direct;

        DelayedRunnable(Runnable run) {
            super(run);
            this.timed = new SequentialDisposable();
            this.direct = new SequentialDisposable();
        }

        @Override
        /** 实例方法 void：测试断言或状态查询。 */

        public void run() {
            Runnable r = get();
            if (r != null) {
                try {
                    try {
                        r.run();
                    } finally {
                        lazySet(null);
                        timed.lazySet(DisposableHelper.DISPOSED);
                        direct.lazySet(DisposableHelper.DISPOSED);
                    }
                } catch (Throwable ex) {
                    // Exceptions.throwIfFatal(ex); nowhere to go
                    RxJavaPlugins.onError(ex);
                    throw ex;
                }
            }
        }

        @Override
        /** 实例方法 boolean：测试断言或状态查询。 */

        public boolean isDisposed() {
            return get() == null;
        }

        @Override
        /** 实例方法 void：测试断言或状态查询。 */

        public void dispose() {
            if (getAndSet(null) != null) {
                timed.dispose();
                direct.dispose();
            }
        }

        @Override
        /** 实例方法 Runnable：测试断言或状态查询。 */

        public Runnable getWrappedRunnable() {
            Runnable r = get();
            return r != null ? r : Functions.EMPTY_RUNNABLE;
        }
    }

    final class DelayedDispose implements Runnable {
        private final DelayedRunnable dr;

        DelayedDispose(DelayedRunnable dr) {
            this.dr = dr;
        }

        @Override
        /** 实例方法 void：测试断言或状态查询。 */

        public void run() {
            dr.direct.replace(scheduleDirect(dr));
        }
    }
}
