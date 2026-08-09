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
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.schedulers.Timed;

/**
 * 将上游每个元素包装为 {@link Timed}，
 * value() 为元素，time() 为与上一项的时间间隔。
 * @param <T> 元素类型
 */
public final class ObservableTimeInterval<T> extends AbstractObservableWithUpstream<T, Timed<T>> {
    final Scheduler scheduler;
    final TimeUnit unit;

    /**
     * @param source 上游 ObservableSource
     * @param unit 时间间隔单位
     * @param scheduler 提供 now 时间戳的 Scheduler
     */
    public ObservableTimeInterval(ObservableSource<T> source, TimeUnit unit, Scheduler scheduler) {
        super(source);
        this.scheduler = scheduler;
        this.unit = unit;
    }

    @Override
    public void subscribeActual(Observer<? super Timed<T>> t) {
        source.subscribe(new TimeIntervalObserver<>(t, unit, scheduler));
    }

    /** 记录 lastTime；onNext 时计算 delta 并发射 Timed。 */
    static final class TimeIntervalObserver<T> implements Observer<T>, Disposable {
        final Observer<? super Timed<T>> downstream;
        final TimeUnit unit;
        final Scheduler scheduler;

        long lastTime;

        Disposable upstream;

        TimeIntervalObserver(Observer<? super Timed<T>> actual, TimeUnit unit, Scheduler scheduler) {
            this.downstream = actual;
            this.scheduler = scheduler;
            this.unit = unit;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                lastTime = scheduler.now(unit);
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        /** delta = now - lastTime，发射 new Timed(t, delta, unit)。 */
        @Override
        public void onNext(T t) {
            long now = scheduler.now(unit);
            long last = lastTime;
            lastTime = now;
            long delta = now - last;
            downstream.onNext(new Timed<>(t, delta, unit));
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
