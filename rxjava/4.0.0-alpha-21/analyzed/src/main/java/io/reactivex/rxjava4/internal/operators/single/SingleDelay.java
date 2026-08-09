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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;

/**
 * 在指定 {@link Scheduler} 上延迟转发上游 Single 的 onSuccess/onError。
 * delayError 为 true 时错误同样延迟；否则错误立即转发。
 * @param <T> 元素类型
 */
public final class SingleDelay<T> extends Single<T> {

    final SingleSource<? extends T> source;
    final long time;
    final TimeUnit unit;
    final Scheduler scheduler;
    final boolean delayError;

    /**
     * @param source 上游 SingleSource
     * @param time 延迟时长
     * @param unit 时间单位
     * @param scheduler 执行延迟任务的调度器
     * @param delayError 是否在 onError 时也应用延迟
     */
    public SingleDelay(SingleSource<? extends T> source, long time, TimeUnit unit, Scheduler scheduler, boolean delayError) {
        this.source = source;
        this.time = time;
        this.unit = unit;
        this.scheduler = scheduler;
        this.delayError = delayError;
    }

    /** 用 SequentialDisposable 串联上游与调度任务。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {

        final SequentialDisposable sd = new SequentialDisposable();
        observer.onSubscribe(sd);
        source.subscribe(new Delay(sd, observer));
    }

    /** 接收上游事件后 scheduleDirect 延迟 Runnable 转发。 */
    final class Delay implements SingleObserver<T> {
        private final SequentialDisposable sd;
        final SingleObserver<? super T> downstream;

        Delay(SequentialDisposable sd, SingleObserver<? super T> observer) {
            this.sd = sd;
            this.downstream = observer;
        }

        @Override
        public void onSubscribe(Disposable d) {
            sd.replace(d);
        }

        /** 调度 OnSuccess 在 time/unit 后 downstream.onSuccess。 */
        @Override
        public void onSuccess(final T value) {
            sd.replace(scheduler.scheduleDirect(new OnSuccess(value), time, unit));
        }

        /** delayError 时延迟转发；否则 time=0 立即 onError。 */
        @Override
        public void onError(final Throwable e) {
            sd.replace(scheduler.scheduleDirect(new OnError(e), delayError ? time : 0, unit));
        }

        /** 延迟任务：向 downstream 发射成功值。 */
        final class OnSuccess implements Runnable {
            private final T value;

            OnSuccess(T value) {
                this.value = value;
            }

            @Override
            public void run() {
                downstream.onSuccess(value);
            }
        }

        /** 延迟任务：向 downstream 转发错误。 */
        final class OnError implements Runnable {
            private final Throwable e;

            OnError(Throwable e) {
                this.e = e;
            }

            @Override
            public void run() {
                downstream.onError(e);
            }
        }
    }
}
