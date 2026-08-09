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

import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.functions.Functions;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 即时重调度周期任务包装：每次 run 完成后立即 submit 下一次，
 * 用于 period<=0 时的“连续执行”语义。
 */
final class InstantPeriodicTask implements Callable<Void>, Disposable {

    final Runnable task;

    final AtomicReference<Future<?>> rest;

    final AtomicReference<Future<?>> first;

    final ExecutorService executor;

    Thread runner;

    static final FutureTask<Void> CANCELLED = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);

    InstantPeriodicTask(Runnable task, ExecutorService executor) {
        super();
        this.task = task;
        this.first = new AtomicReference<>();
        this.rest = new AtomicReference<>();
        this.executor = executor;
    }

    /** 执行 task 后在 executor 上 submit(this) 实现链式重调度。 */
    @Override
    public Void call() {
        runner = Thread.currentThread();
        try {
            task.run();
            runner = null;
            setRest(executor.submit(this));
        } catch (Throwable ex) {
            // Exceptions.throwIfFatal(ex); nowhere to go
            runner = null;
            RxJavaPlugins.onError(ex);
            throw ex;
        }
        return null;
    }

    /** 将 first/rest Future 置为 CANCELLED 并 cancel 运行中任务。 */
    @Override
    public void dispose() {
        Future<?> current = first.getAndSet(CANCELLED);
        if (current != null && current != CANCELLED) {
            current.cancel(runner != Thread.currentThread());
        }
        current = rest.getAndSet(CANCELLED);
        if (current != null && current != CANCELLED) {
            current.cancel(runner != Thread.currentThread());
        }
    }

    @Override
    public boolean isDisposed() {
        return first.get() == CANCELLED;
    }

    /** CAS 设置首次 Future；已 CANCELLED 则 cancel 新 Future。 */
    void setFirst(Future<?> f) {
        for (;;) {
            Future<?> current = first.get();
            if (current == CANCELLED) {
                f.cancel(runner != Thread.currentThread());
                return;
            }
            if (first.compareAndSet(current, f)) {
                return;
            }
        }
    }

    /** CAS 设置后续 Future；已 CANCELLED 则 cancel 新 Future。 */
    void setRest(Future<?> f) {
        for (;;) {
            Future<?> current = rest.get();
            if (current == CANCELLED) {
                f.cancel(runner != Thread.currentThread());
                return;
            }
            if (rest.compareAndSet(current, f)) {
                return;
            }
        }
    }
}
