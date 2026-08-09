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
 * 在 Scheduler 上按周期发射 [start, end] 范围内的 Long，
 * 到达 end 后 onComplete 并 dispose。
 */
public final class ObservableIntervalRange extends Observable<Long> {
    final Scheduler scheduler;
    final long start;
    final long end;
    final long initialDelay;
    final long period;
    final TimeUnit unit;

    /**
     * @param start 首个发射值（含）
     * @param end 末个发射值（含），到达后 onComplete
     * @param initialDelay 首次发射前的延迟
     * @param period 相邻发射间隔
     * @param unit 时间单位
     * @param scheduler 调度周期任务的 Scheduler
     */
    public ObservableIntervalRange(long start, long end, long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
        this.scheduler = scheduler;
        this.start = start;
        this.end = end;
    }

    /** 创建 IntervalRangeObserver 并 schedulePeriodically。 */
    @Override
    public void subscribeActual(Observer<? super Long> observer) {
        IntervalRangeObserver is = new IntervalRangeObserver(observer, start, end);
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

    /** 从 start 递增发射至 end，到达 end 后 onComplete。 */
    static final class IntervalRangeObserver
    extends AtomicReference<Disposable>
    implements Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = 1891866368734007884L;

        final Observer<? super Long> downstream;
        final long end;

        long count;

        IntervalRangeObserver(Observer<? super Long> actual, long start, long end) {
            this.downstream = actual;
            this.count = start;
            this.end = end;
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return get() == DisposableHelper.DISPOSED;
        }

        /** 发射 count；等于 end 时 onComplete 并 dispose。 */
        @Override
        public void run() {
            if (!isDisposed()) {
                long c = count;
                downstream.onNext(c);

                if (c == end) {
                    if (!isDisposed()) {
                        downstream.onComplete();
                    }
                    DisposableHelper.dispose(this);
                    return;
                }

                count = c + 1;

            }
        }

        public void setResource(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }
    }
}
