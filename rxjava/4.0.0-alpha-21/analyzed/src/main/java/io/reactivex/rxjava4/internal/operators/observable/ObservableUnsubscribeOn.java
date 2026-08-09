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

package io.reactivex.rxjava4.internal.operators.observable;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 在指定 {@link Scheduler} 上异步 dispose 上游，避免 dispose 与 onNext 同线程竞态。
 * @param <T> 元素类型
 */
public final class ObservableUnsubscribeOn<T> extends AbstractObservableWithUpstream<T, T> {
    final Scheduler scheduler;
    /**
     * @param source 上游 ObservableSource
     * @param scheduler 执行 upstream.dispose 的调度器
     */
    public ObservableUnsubscribeOn(ObservableSource<T> source, Scheduler scheduler) {
        super(source);
        this.scheduler = scheduler;
    }

    /** 订阅 UnsubscribeObserver。 */
    @Override
    public void subscribeActual(Observer<? super T> t) {
        source.subscribe(new UnsubscribeObserver<>(t, scheduler));
    }

    /** dispose 后忽略后续事件；在 scheduler 上 dispose 上游。 */
    static final class UnsubscribeObserver<T> extends AtomicBoolean implements Observer<T>, Disposable {

        @Serial
        private static final long serialVersionUID = 1015244841293359600L;

        final Observer<? super T> downstream;
        final Scheduler scheduler;

        Disposable upstream;

        UnsubscribeObserver(Observer<? super T> actual, Scheduler scheduler) {
            this.downstream = actual;
            this.scheduler = scheduler;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            if (!get()) {
                downstream.onNext(t);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (get()) {
                RxJavaPlugins.onError(t);
                return;
            }
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            if (!get()) {
                downstream.onComplete();
            }
        }

        /** CAS 置位后 scheduleDirect 执行 DisposeTask。 */
        @Override
        public void dispose() {
            if (compareAndSet(false, true)) {
                scheduler.scheduleDirect(new DisposeTask());
            }
        }

        @Override
        public boolean isDisposed() {
            return get();
        }

        /** 在 scheduler 线程上调用 upstream.dispose。 */
        final class DisposeTask implements Runnable {
            @Override
            public void run() {
                upstream.dispose();
            }
        }
    }
}
