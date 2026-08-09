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
 * 将所有信号类型延迟指定时长，
 * 并在给定 {@link Scheduler} 上重新发射。
 *
 * @param <T> 元素类型
 */
public final class MaybeDelay<T> extends AbstractMaybeWithUpstream<T, T> {

    final long delay;

    final TimeUnit unit;

    final Scheduler scheduler;

    final boolean delayError;

    /**
     * @param source 上游 Maybe
     * @param delay 延迟时长
     * @param unit 时间单位
     * @param scheduler 调度延迟任务的 Scheduler
     * @param delayError true 时 onError 也延迟 delay 时长
     */
    public MaybeDelay(MaybeSource<T> source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {
        super(source);
        this.delay = delay;
        this.unit = unit;
        this.scheduler = scheduler;
        this.delayError = delayError;
    }

    /** 订阅 DelayMaybeObserver 并在 Scheduler 上调度信号。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new DelayMaybeObserver<>(observer, delay, unit, scheduler, delayError));
    }

    /** 缓存信号后在 Scheduler 上 scheduleDirect 延迟转发。 */
    static final class DelayMaybeObserver<T>
    extends AtomicReference<Disposable>
    implements MaybeObserver<T>, Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = 5566860102500855068L;

        final MaybeObserver<? super T> downstream;

        final long delay;

        final TimeUnit unit;

        final Scheduler scheduler;

        final boolean delayError;

        T value;

        Throwable error;

        DelayMaybeObserver(MaybeObserver<? super T> actual, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {
            this.downstream = actual;
            this.delay = delay;
            this.unit = unit;
            this.scheduler = scheduler;
            this.delayError = delayError;
        }

        /** 延迟到期后转发缓存的 onSuccess/onError/onComplete。 */
        @Override
        public void run() {
            Throwable ex = error;
            if (ex != null) {
                downstream.onError(ex);
            } else {
                T v = value;
                if (v != null) {
                    downstream.onSuccess(v);
                } else {
                    downstream.onComplete();
                }
            }
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d)) {
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onSuccess(T value) {
            this.value = value;
            schedule(delay);
        }

        @Override
        public void onError(Throwable e) {
            this.error = e;
            schedule(delayError ? delay : 0);
        }

        @Override
        public void onComplete() {
            schedule(delay);
        }

        /** 在 scheduler 上 scheduleDirect 延迟执行 run。 */
        void schedule(long delay) {
            DisposableHelper.replace(this, scheduler.scheduleDirect(this, delay, unit));
        }
    }
}
