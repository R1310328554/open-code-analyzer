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
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 确保下游的 dispose() 在指定 {@link Scheduler} 上执行。
 *
 * @param <T> 元素类型
 */
public final class MaybeUnsubscribeOn<T> extends AbstractMaybeWithUpstream<T, T> {

    final Scheduler scheduler;

    /**
     * @param source 上游 Maybe
     * @param scheduler 执行 dispose 的 Scheduler
     */
    public MaybeUnsubscribeOn(MaybeSource<T> source, Scheduler scheduler) {
        super(source);
        this.scheduler = scheduler;
    }

    /** 订阅 UnsubscribeOnMaybeObserver，在 scheduler 上调度上游 dispose。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new UnsubscribeOnMaybeObserver<>(observer, scheduler));
    }

    /** dispose 时在 scheduler 上调度 run() 以 dispose 上游。 */
    static final class UnsubscribeOnMaybeObserver<T> extends AtomicReference<Disposable>
    implements MaybeObserver<T>, Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = 3256698449646456986L;

        final MaybeObserver<? super T> downstream;

        final Scheduler scheduler;

        Disposable ds;

        UnsubscribeOnMaybeObserver(MaybeObserver<? super T> actual, Scheduler scheduler) {
            this.downstream = actual;
            this.scheduler = scheduler;
        }

        /** 在 scheduler 上调度上游 dispose。 */
        @Override
        public void dispose() {
            Disposable d = getAndSet(DisposableHelper.DISPOSED);
            if (d != DisposableHelper.DISPOSED) {
                this.ds = d;
                scheduler.scheduleDirect(this);
            }
        }

        /** 在 scheduler 线程上 dispose 上游 Disposable。 */
        @Override
        public void run() {
            ds.dispose();
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
            downstream.onSuccess(value);
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
