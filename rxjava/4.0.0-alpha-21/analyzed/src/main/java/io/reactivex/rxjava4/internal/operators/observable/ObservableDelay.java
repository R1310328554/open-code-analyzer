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

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.core.Scheduler.Worker;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.observers.SerializedObserver;

/**
 * 在 {@link Scheduler.Worker} 上延迟转发 onNext/onError/onComplete。
 * delayError 为 true 时 onError 也延迟相同时长。
 *
 * @param <T> 上游元素类型
 */
public final class ObservableDelay<T> extends AbstractObservableWithUpstream<T, T> {
    final long delay;
    final TimeUnit unit;
    final Scheduler scheduler;
    final boolean delayError;

    /**
     * @param source 上游 ObservableSource
     * @param delay 延迟量
     * @param unit 时间单位
     * @param scheduler 调度延迟任务的 Scheduler
     * @param delayError 是否同样延迟 onError
     */
    public ObservableDelay(ObservableSource<T> source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {
        super(source);
        this.delay = delay;
        this.unit = unit;
        this.scheduler = scheduler;
        this.delayError = delayError;
    }

    /** delayError 时下游不序列化，否则用 SerializedObserver 包装。 */
    @Override
    @SuppressWarnings("unchecked")
    public void subscribeActual(Observer<? super T> t) {
        Observer<T> observer;
        if (delayError) {
            observer = (Observer<T>)t;
        } else {
            observer = new SerializedObserver<>(t);
        }

        Scheduler.Worker w = scheduler.createWorker();

        source.subscribe(new DelayObserver<>(observer, delay, unit, w, delayError));
    }

    /** 将各信号 schedule 到 Worker 上延迟执行。 */
    static final class DelayObserver<T> implements Observer<T>, Disposable {
        final Observer<? super T> downstream;
        final long delay;
        final TimeUnit unit;
        final Scheduler.Worker w;
        final boolean delayError;

        Disposable upstream;

        DelayObserver(Observer<? super T> actual, long delay, TimeUnit unit, Worker w, boolean delayError) {
            super();
            this.downstream = actual;
            this.delay = delay;
            this.unit = unit;
            this.w = w;
            this.delayError = delayError;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(final T t) {
            w.schedule(new OnNext(t), delay, unit);
        }

        @Override
        public void onError(final Throwable t) {
            w.schedule(new OnError(t), delayError ? delay : 0, unit);
        }

        @Override
        public void onComplete() {
            w.schedule(new OnComplete(), delay, unit);
        }

        @Override
        public void dispose() {
            upstream.dispose();
            w.dispose();
        }

        @Override
        public boolean isDisposed() {
            return w.isDisposed();
        }

        /** 延迟到期后转发 onNext（Worker 未 dispose）。 */
        final class OnNext implements Runnable {
            private final T t;

            OnNext(T t) {
                this.t = t;
            }

            @Override
            public void run() {
                if (!w.isDisposed()) {
                    downstream.onNext(t);
                }
            }
        }

        /** 延迟到期后转发 onError 并 dispose Worker。 */
        final class OnError implements Runnable {
            private final Throwable throwable;

            OnError(Throwable throwable) {
                this.throwable = throwable;
            }

            @Override
            public void run() {
                try {
                    downstream.onError(throwable);
                } finally {
                    w.dispose();
                }
            }
        }

        /** 延迟到期后转发 onComplete 并 dispose Worker。 */
        final class OnComplete implements Runnable {
            @Override
            public void run() {
                try {
                    downstream.onComplete();
                } finally {
                    w.dispose();
                }
            }
        }
    }
}
