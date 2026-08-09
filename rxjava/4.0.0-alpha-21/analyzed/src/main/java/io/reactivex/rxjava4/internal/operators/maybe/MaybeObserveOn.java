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
 * 在指定 {@link Scheduler} 上调度并向下游转发
 * onSuccess、onError 或 onComplete 事件。
 *
 * @param <T> 元素类型
 */
public final class MaybeObserveOn<T> extends AbstractMaybeWithUpstream<T, T> {

    final Scheduler scheduler;

    /**
     * @param source 上游 MaybeSource
     * @param scheduler 事件调度器
     */
    public MaybeObserveOn(MaybeSource<T> source, Scheduler scheduler) {
        super(source);
        this.scheduler = scheduler;
    }

    /** 包装为 ObserveOnMaybeObserver，在 scheduler 上 run 转发。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new ObserveOnMaybeObserver<>(observer, scheduler));
    }

    /** 缓存 value/error 后 scheduleDirect(this) 在目标线程转发。 */
    static final class ObserveOnMaybeObserver<T>
    extends AtomicReference<Disposable>
    implements MaybeObserver<T>, Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = 8571289934935992137L;

        final MaybeObserver<? super T> downstream;

        final Scheduler scheduler;

        T value;
        Throwable error;

        ObserveOnMaybeObserver(MaybeObserver<? super T> actual, Scheduler scheduler) {
            this.downstream = actual;
            this.scheduler = scheduler;
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
            DisposableHelper.replace(this, scheduler.scheduleDirect(this));
        }

        @Override
        public void onError(Throwable e) {
            this.error = e;
            DisposableHelper.replace(this, scheduler.scheduleDirect(this));
        }

        @Override
        public void onComplete() {
            DisposableHelper.replace(this, scheduler.scheduleDirect(this));
        }

        /** 按 error → success → complete 顺序向下游 relay。 */
        @Override
        public void run() {
            Throwable ex = error;
            if (ex != null) {
                error = null;
                downstream.onError(ex);
            } else {
                T v = value;
                if (v != null) {
                    value = null;
                    downstream.onSuccess(v);
                } else {
                    downstream.onComplete();
                }
            }
        }
    }
}
