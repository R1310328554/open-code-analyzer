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
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.*;

/**
 * 延迟 delay 后发射单个 0L 并 onComplete 的冷 Observable。
 */
public final class ObservableTimer extends Observable<Long> {
    final Scheduler scheduler;
    final long delay;
    final TimeUnit unit;
    /**
     * @param delay 延迟时间
     * @param unit 时间单位
     * @param scheduler 调度 run 的 Scheduler
     */
    public ObservableTimer(long delay, TimeUnit unit, Scheduler scheduler) {
        this.delay = delay;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    /** scheduleDirect 触发 TimerObserver.run 发射 0L。 */
    @Override
    public void subscribeActual(Observer<? super Long> observer) {
        TimerObserver ios = new TimerObserver(observer);
        observer.onSubscribe(ios);

        Disposable d = scheduler.scheduleDirect(ios, delay, unit);

        ios.setResource(d);
    }

    /** run 时 onNext(0L) 并 lazySet EmptyDisposable 后 onComplete。 */
    static final class TimerObserver extends AtomicReference<Disposable>
    implements Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = -2809475196591179431L;

        final Observer<? super Long> downstream;

        TimerObserver(Observer<? super Long> downstream) {
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

        @Override
        public void run() {
            if (!isDisposed()) {
                downstream.onNext(0L);
                lazySet(EmptyDisposable.INSTANCE);
                downstream.onComplete();
            }
        }

        public void setResource(Disposable d) {
            DisposableHelper.trySet(this, d);
        }
    }
}
