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

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.schedulers.Timed;

/**
 * 测量订阅到上游 onSuccess 的耗时，封装为 {@link Timed} 发射。
 * start 为 true 时在 onSubscribe 时记录起点；否则从 0 起算。
 * @param <T> 上游元素类型
 * @since 3.0.0
 */
public final class SingleTimeInterval<T> extends Single<Timed<T>> {

    final SingleSource<T> source;

    final TimeUnit unit;

    final Scheduler scheduler;

    final boolean start;

    /**
     * @param source 上游 SingleSource
     * @param unit 时间单位
     * @param scheduler 提供 now(unit) 的调度器
     * @param start 是否在 onSubscribe 时记录 startTime
     */
    public SingleTimeInterval(SingleSource<T> source, TimeUnit unit, Scheduler scheduler, boolean start) {
        this.source = source;
        this.unit = unit;
        this.scheduler = scheduler;
        this.start = start;
    }

    /** 订阅 TimeIntervalSingleObserver 在 onSuccess 时包装 Timed。 */
    @Override
    protected void subscribeActual(@NonNull SingleObserver<? super @NonNull Timed<T>> observer) {
        source.subscribe(new TimeIntervalSingleObserver<>(observer, unit, scheduler, start));
    }

    /** onSuccess 时发射 Timed(value, now-startTime, unit)。 */
    static final class TimeIntervalSingleObserver<T> implements SingleObserver<T>, Disposable {

        final SingleObserver<? super Timed<T>> downstream;

        final TimeUnit unit;

        final Scheduler scheduler;

        final long startTime;

        Disposable upstream;

        TimeIntervalSingleObserver(SingleObserver<? super Timed<T>> downstream, TimeUnit unit, Scheduler scheduler, boolean start) {
            this.downstream = downstream;
            this.unit = unit;
            this.scheduler = scheduler;
            this.startTime = start ? scheduler.now(unit) : 0L;
        }

        @Override
        public void onSubscribe(@NonNull Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** 计算 elapsed 并 downstream.onSuccess(new Timed<>(t, elapsed, unit))。 */
        @Override
        public void onSuccess(@NonNull T t) {
            downstream.onSuccess(new Timed<>(t, scheduler.now(unit) - startTime, unit));
        }

        @Override
        public void onError(@NonNull Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }
    }
}
