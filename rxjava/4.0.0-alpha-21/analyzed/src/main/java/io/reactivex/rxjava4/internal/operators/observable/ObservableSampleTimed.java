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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Consumer;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.observers.SerializedObserver;

/**
 * 按固定周期采样上游最新值：定时 run 发射缓存值，
 * 新值覆盖旧值时可经 onDropped 通知；emitLast 控制完成时是否发射末值。
 *
 * @param <T> 元素类型
 */
    final long period;
    final TimeUnit unit;
    final Scheduler scheduler;
    final Consumer<? super T> onDropped;
    final boolean emitLast;

    /**
     * @param source 上游 ObservableSource
     * @param period 采样周期
     * @param unit 时间单位
     * @param scheduler 调度定时任务的 Scheduler
     * @param emitLast 完成时是否发射最后一次缓存值
     * @param onDropped 被覆盖的旧值回调（可为 null）
     */
    public ObservableSampleTimed(ObservableSource<T> source,
                                 long period,
                                 TimeUnit unit,
                                 Scheduler scheduler,
                                 boolean emitLast,
                                 Consumer<? super T> onDropped) {
        super(source);
        this.period = period;
        this.unit = unit;
        this.scheduler = scheduler;
        this.emitLast = emitLast;
        this.onDropped = onDropped;
    }

    /** 按 emitLast 选择 SampleTimedEmitLast 或 SampleTimedNoLast。 */
    @Override
    public void subscribeActual(Observer<? super T> t) {
        SerializedObserver<T> serial = new SerializedObserver<>(t);
        if (emitLast) {
            source.subscribe(new SampleTimedEmitLast<>(serial, period, unit, scheduler, onDropped));
        } else {
            source.subscribe(new SampleTimedNoLast<>(serial, period, unit, scheduler, onDropped));
        }
    }

    /** 缓存最新值，schedulePeriodicallyDirect 触发 emit；onDropped 处理被覆盖值。 */
    abstract static class SampleTimedObserver<T> extends AtomicReference<T> implements Observer<T>, Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = -3517602651313910099L;

        final Observer<? super T> downstream;
        final long period;
        final TimeUnit unit;
        final Scheduler scheduler;
        final Consumer<? super T> onDropped;

        final AtomicReference<Disposable> timer = new AtomicReference<>();

        Disposable upstream;

        SampleTimedObserver(Observer<? super T> actual, long period, TimeUnit unit, Scheduler scheduler, Consumer<? super T> onDropped) {
            this.downstream = actual;
            this.period = period;
            this.unit = unit;
            this.scheduler = scheduler;
            this.onDropped = onDropped;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);

                Disposable task = scheduler.schedulePeriodicallyDirect(this, period, period, unit);
                DisposableHelper.replace(timer, task);
            }
        }

        /** getAndSet 新值；旧值非 null 时可选 onDropped.accept。 */
        @Override
        public void onNext(T t) {
            T oldValue = getAndSet(t);
            if (oldValue != null && onDropped != null) {
                try {
                    onDropped.accept(oldValue);
                } catch (Throwable throwable) {
                    Exceptions.throwIfFatal(throwable);
                    cancelTimer();
                    upstream.dispose();
                    downstream.onError(throwable);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            cancelTimer();
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            cancelTimer();
            complete();
        }

        void cancelTimer() {
            DisposableHelper.dispose(timer);
        }

        @Override
        public void dispose() {
            cancelTimer();
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        /** 取出并清空缓存值，非 null 则 downstream.onNext。 */
        void emit() {
            T value = getAndSet(null);
            if (value != null) {
                downstream.onNext(value);
            }
        }

        abstract void complete();
    }

    /** emitLast=false：定时 emit，完成时不强制发射末值。 */
    static final class SampleTimedNoLast<T> extends SampleTimedObserver<T> {

        @Serial
        private static final long serialVersionUID = -7139995637533111443L;

        SampleTimedNoLast(Observer<? super T> actual, long period, TimeUnit unit, Scheduler scheduler, Consumer<? super T> onDropped) {
            super(actual, period, unit, scheduler, onDropped);
        }

        @Override
        void complete() {
            downstream.onComplete();
        }

        @Override
        public void run() {
            emit();
        }
    }

    /** emitLast=true：完成与定时 tick 均尝试 emit 末值，wip 协调 onComplete。 */
    static final class SampleTimedEmitLast<T> extends SampleTimedObserver<T> {

        @Serial
        private static final long serialVersionUID = -7139995637533111443L;

        final AtomicInteger wip;

        SampleTimedEmitLast(Observer<? super T> actual, long period, TimeUnit unit, Scheduler scheduler, Consumer<? super T> onDropped) {
            super(actual, period, unit, scheduler, onDropped);
            this.wip = new AtomicInteger(1);
        }

        @Override
        void complete() {
            emit();
            if (wip.decrementAndGet() == 0) {
                downstream.onComplete();
            }
        }

        @Override
        public void run() {
            if (wip.incrementAndGet() == 2) {
                emit();
                if (wip.decrementAndGet() == 0) {
                    downstream.onComplete();
                }
            }
        }
    }
}
