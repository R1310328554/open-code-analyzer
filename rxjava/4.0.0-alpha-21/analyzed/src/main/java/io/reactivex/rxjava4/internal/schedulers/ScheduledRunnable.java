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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReferenceArray;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;
import io.reactivex.rxjava4.schedulers.SchedulerRunnableIntrospection;

/**
 * 可追踪的调度任务：AtomicReferenceArray 存 parent/future/thread，
 * 支持同步/异步 dispose 与 {@link SchedulerRunnableIntrospection}。
 */
public final class ScheduledRunnable extends AtomicReferenceArray<Object>
implements Runnable, Callable<Object>, Disposable, SchedulerRunnableIntrospection {

    @Serial
    private static final long serialVersionUID = -6120223772001106981L;
    final Runnable actual;
    final boolean interruptOnCancel;

    /** 父追踪容器已收到本任务完成通知。 */
    static final Object PARENT_DISPOSED = new Object();
    /** dispose() 在 run/call 内部同线程调用。 */
    static final Object SYNC_DISPOSED = new Object();
    /** dispose() 从其他线程异步调用。 */
    static final Object ASYNC_DISPOSED = new Object();

    static final Object DONE = new Object();

    static final int PARENT_INDEX = 0;
    static final int FUTURE_INDEX = 1;
    static final int THREAD_INDEX = 2;

    /**
     * 包装 Runnable 并设置可选 parent；默认 interruptOnCancel=true。
     * @param actual 待包装的 Runnable（未校验非 null）
     * @param parent 任务追踪容器，可为 null
     */
    public ScheduledRunnable(Runnable actual, DisposableContainer parent) {
        this(actual, parent, true);
    }

    /**
     * 包装 Runnable 并设置 parent 与 interruptOnCancel 策略。
     * @param actual 待包装的 Runnable（未校验非 null）
     * @param parent 任务追踪容器，可为 null
     * @param interruptOnCancel 异步 dispose 时是否 interrupt 底层 Future
     */
    public ScheduledRunnable(Runnable actual, DisposableContainer parent, boolean interruptOnCancel) {
        super(3);
        this.actual = actual;
        this.interruptOnCancel = interruptOnCancel;
        this.lazySet(0, parent);
    }

    /** Callable 入口：调用 run() 以节省 ThreadPoolExecutor 分配。 */
    @Override
    public Object call() {
        // Being Callable saves an allocation in ThreadPoolExecutor
        run();
        return null;
    }

    /** 执行 actual.run()，finally 中通知 parent 并清理 future/thread 槽位。 */
    @Override
    public void run() {
        lazySet(THREAD_INDEX, Thread.currentThread());
        try {
            try {
                actual.run();
            } catch (Throwable e) {
                // Exceptions.throwIfFatal(e); nowhere to go
                RxJavaPlugins.onError(e);
                throw e;
            }
        } finally {
            Object o = get(PARENT_INDEX);
            if (o != PARENT_DISPOSED && compareAndSet(PARENT_INDEX, o, DONE) && o != null) {
                ((DisposableContainer)o).delete(this);
            }

            for (;;) {
                o = get(FUTURE_INDEX);
                if (o == SYNC_DISPOSED || o == ASYNC_DISPOSED || compareAndSet(FUTURE_INDEX, o, DONE)) {
                    break;
                }
            }
            lazySet(THREAD_INDEX, null);
        }
    }

    /** CAS 设置 Future；已 dispose 则按策略 cancel。 */
    public void setFuture(Future<?> f) {
        for (;;) {
            Object o = get(FUTURE_INDEX);
            if (o == DONE) {
                return;
            }
            if (o == SYNC_DISPOSED) {
                f.cancel(false);
                return;
            }
            if (o == ASYNC_DISPOSED) {
                f.cancel(interruptOnCancel);
                return;
            }
            if (compareAndSet(FUTURE_INDEX, o, f)) {
                return;
            }
        }
    }

    /** 标记 SYNC/ASYNC_DISPOSED 并 cancel Future；通知 parent 删除本任务。 */
    @Override
    public void dispose() {
        for (;;) {
            Object o = get(FUTURE_INDEX);
            if (o == DONE || o == SYNC_DISPOSED || o == ASYNC_DISPOSED) {
                break;
            }
            boolean async = get(THREAD_INDEX) != Thread.currentThread();
            if (compareAndSet(FUTURE_INDEX, o, async ? ASYNC_DISPOSED : SYNC_DISPOSED)) {
                if (o != null) {
                    ((Future<?>)o).cancel(async && interruptOnCancel);
                }
                break;
            }
        }

        for (;;) {
            Object o = get(PARENT_INDEX);
            if (o == DONE || o == PARENT_DISPOSED || o == null) {
                return;
            }
            if (compareAndSet(PARENT_INDEX, o, PARENT_DISPOSED)) {
                ((DisposableContainer)o).delete(this);
                return;
            }
        }
    }

    @Override
    public boolean isDisposed() {
        Object o = get(PARENT_INDEX);
        return o == PARENT_DISPOSED || o == DONE;
    }

    /** 返回 Waiting/Running/Finished/Disposed 等状态摘要。 */
    @Override
    public String toString() {
        String state;
        Object o = get(FUTURE_INDEX);
        if (o == DONE) {
            state = "Finished";
        } else if (o == SYNC_DISPOSED) {
            state = "Disposed(Sync)";
        } else if (o == ASYNC_DISPOSED) {
            state = "Disposed(Async)";
        } else {
            o = get(THREAD_INDEX);
            if (o == null) {
                state = "Waiting";
            } else {
                state = "Running on " + o;
            }
        }

        return getClass().getSimpleName() + "[" + state + "]";
    }

    @Override
    public @NonNull Runnable getWrappedRunnable() {
        return actual;
    }
}
