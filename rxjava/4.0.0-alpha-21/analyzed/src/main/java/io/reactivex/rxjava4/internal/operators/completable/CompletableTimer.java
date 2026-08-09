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

package io.reactivex.rxjava4.internal.operators.completable;

import java.io.Serial;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 在指定延迟后发出 {@code onComplete} 事件。
 */
public final class CompletableTimer extends Completable {

    final long delay;
    final TimeUnit unit;
    final Scheduler scheduler;

    /**
     * @param delay 延迟量
     * @param unit 时间单位
     * @param scheduler 调度 {@link Runnable} 的 Scheduler
     */
    public CompletableTimer(long delay, TimeUnit unit, Scheduler scheduler) {
        this.delay = delay;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    /** 在 scheduler 上调度 TimerDisposable，到期后 onComplete。 */
    @Override
    protected void subscribeActual(final CompletableObserver observer) {
        TimerDisposable parent = new TimerDisposable(observer);
        observer.onSubscribe(parent);
        parent.setFuture(scheduler.scheduleDirect(parent, delay, unit));
    }

    static final class TimerDisposable extends AtomicReference<Disposable> implements Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = 3167244060586201109L;
        final CompletableObserver downstream;

        TimerDisposable(final CompletableObserver downstream) {
            this.downstream = downstream;
        }

        /** 定时到期，向下游发出 onComplete。 */
        @Override
        public void run() {
            downstream.onComplete();
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        /** 替换当前 Disposable 为 scheduler 返回的任务。 */
        void setFuture(Disposable d) {
            DisposableHelper.replace(this, d);
        }
    }
}
