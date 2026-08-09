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
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.core.Scheduler.Worker;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.schedulers.TrampolineScheduler;

/**
 * 在 {@link Scheduler} 上按固定周期发射递增 Long（0, 1, 2, …）。
 * TrampolineScheduler 使用 Worker，其余用 schedulePeriodicallyDirect。
 */
public final class ObservableInterval extends Observable<Long> {
    final Scheduler scheduler;
    final long initialDelay;
    final long period;
    final TimeUnit unit;

    /**
     * @param initialDelay 首次发射前的延迟
     * @param period 相邻发射间隔
     * @param unit 时间单位
     * @param scheduler 调度周期任务的 Scheduler
     */
    public ObservableInterval(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    /** 创建 IntervalObserver 并 schedulePeriodically。 */
    @Override
    public void subscribeActual(Observer<? super Long> observer) {
        IntervalObserver is = new IntervalObserver(observer);
        observer.onSubscribe(is);

        Scheduler sch = scheduler;

        if (sch instanceof TrampolineScheduler) {
            Worker worker = sch.createWorker();
            is.setResource(worker);
            worker.schedulePeriodically(is, initialDelay, period, unit);
        } else {
            Disposable d = sch.schedulePeriodicallyDirect(is, initialDelay, period, unit);
            is.setResource(d);
        }
    }

    /** Runnable 每次 tick 发射 count++，dispose 后停止。 */
    static final class IntervalObserver
    extends AtomicReference<Disposable>
    implements Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = 346773832286157679L;

        final Observer<? super Long> downstream;

        long count;

        IntervalObserver(Observer<? super Long> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return get() == DisposableHelper.DISPOSED;
        }

        /** 未 dispose 时向下游 onNext(count++)。 */
        @Override
        public void run() {
            if (get() != DisposableHelper.DISPOSED) {
                downstream.onNext(count++);
            }
        }

        public void setResource(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }
    }
}
