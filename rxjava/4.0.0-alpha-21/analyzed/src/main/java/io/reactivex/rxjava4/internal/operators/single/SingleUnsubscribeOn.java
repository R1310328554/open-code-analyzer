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

package io.reactivex.rxjava4.internal.operators.single;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 确保下游 dispose() 在指定 scheduler 上执行 upstream.dispose()，
 * 避免在错误线程取消订阅。
 * 
 * @param <T> 元素类型
 */
public final class SingleUnsubscribeOn<T> extends Single<T> {

    final SingleSource<T> source;

    final Scheduler scheduler;

    /**
     * @param source 上游 SingleSource
     * @param scheduler 执行 dispose 的 Scheduler
     */
    public SingleUnsubscribeOn(SingleSource<T> source, Scheduler scheduler) {
        this.source = source;
        this.scheduler = scheduler;
    }

    /** 订阅 UnsubscribeOnSingleObserver 包装 dispose 调度。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new UnsubscribeOnSingleObserver<>(observer, scheduler));
    }

    /** dispose 时 scheduleDirect(this) 在 scheduler 上执行 ds.dispose()。 */
    static final class UnsubscribeOnSingleObserver<T> extends AtomicReference<Disposable>
    implements SingleObserver<T>, Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = 3256698449646456986L;

        final SingleObserver<? super T> downstream;

        final Scheduler scheduler;

        Disposable ds;

        UnsubscribeOnSingleObserver(SingleObserver<? super T> actual, Scheduler scheduler) {
            this.downstream = actual;
            this.scheduler = scheduler;
        }

        /** 缓存 upstream Disposable 并在 scheduler 上异步 dispose。 */
        @Override
        public void dispose() {
            Disposable d = getAndSet(DisposableHelper.DISPOSED);
            if (d != DisposableHelper.DISPOSED) {
                this.ds = d;
                scheduler.scheduleDirect(this);
            }
        }

        /** scheduler 线程上执行 ds.dispose()。 */
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
    }
}
