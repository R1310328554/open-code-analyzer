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

package io.reactivex.rxjava4.internal.operators.maybe;

import java.io.Serial;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 在指定延迟后以 onSuccess 发射 {@code 0L}。
 */
public final class MaybeTimer extends Maybe<Long> {

    final long delay;

    final TimeUnit unit;

    final Scheduler scheduler;

    /**
     * @param delay 延迟量
     * @param unit 时间单位
     * @param scheduler 调度定时任务的 Scheduler
     */
    public MaybeTimer(long delay, TimeUnit unit, Scheduler scheduler) {
        this.delay = delay;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    /** 在 scheduler 上调度 TimerDisposable，到期后 onSuccess(0L)。 */
    @Override
    protected void subscribeActual(final MaybeObserver<? super Long> observer) {
        TimerDisposable parent = new TimerDisposable(observer);
        observer.onSubscribe(parent);
        parent.setFuture(scheduler.scheduleDirect(parent, delay, unit));
    }

    /** 定时 Runnable，到期向 downstream 发射 0L。 */
    static final class TimerDisposable extends AtomicReference<Disposable> implements Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = 2875964065294031672L;
        final MaybeObserver<? super Long> downstream;

        TimerDisposable(final MaybeObserver<? super Long> downstream) {
            this.downstream = downstream;
        }

        /** 定时到期，向 downstream 发射 0L。 */
        @Override
        public void run() {
            downstream.onSuccess(0L);
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        void setFuture(Disposable d) {
            DisposableHelper.replace(this, d);
        }
    }
}
