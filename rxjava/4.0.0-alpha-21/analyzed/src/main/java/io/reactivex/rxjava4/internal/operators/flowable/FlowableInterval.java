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

package io.reactivex.rxjava4.internal.operators.flowable;

import java.io.Serial;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.core.Scheduler.Worker;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.MissingBackpressureException;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.schedulers.TrampolineScheduler;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.BackpressureHelper;

/**
 * 在指定 {@link Scheduler} 上按固定周期发射递增 Long 计数（0, 1, 2, …）。
 */
public final class FlowableInterval extends Flowable<Long> {
    final Scheduler scheduler;
    final long initialDelay;
    final long period;
    final TimeUnit unit;

    /**
     * @param initialDelay 首次发射前的延迟
     * @param period 后续发射间隔
     * @param unit 时间单位
     * @param scheduler 执行周期任务的 Scheduler
     */
    public FlowableInterval(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    /** 创建 IntervalSubscriber 并按 Trampoline 与否选择调度方式。 */
    @Override
    public void subscribeActual(Subscriber<? super Long> s) {
        IntervalSubscriber is = new IntervalSubscriber(s);
        s.onSubscribe(is);

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

    /** 背压感知的周期计数 Subscription。 */
    static final class IntervalSubscriber extends AtomicLong
    implements Subscription, Runnable {

        @Serial
        private static final long serialVersionUID = -2809475196591179431L;

        final Subscriber<? super Long> downstream;

        long count;

        final AtomicReference<Disposable> resource = new AtomicReference<>();

        IntervalSubscriber(Subscriber<? super Long> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this, n);
            }
        }

        @Override
        public void cancel() {
            DisposableHelper.dispose(resource);
        }

        /** 有背压许可时发射 count 并递增，否则 MissingBackpressureException。 */
        @Override
        public void run() {
            if (resource.get() != DisposableHelper.DISPOSED) {
                long r = get();

                if (r != 0L) {
                    downstream.onNext(count++);
                    BackpressureHelper.produced(this, 1);
                } else {
                    downstream.onError(new MissingBackpressureException("Could not emit value " + count + " due to lack of requests"));
                    DisposableHelper.dispose(resource);
                }
            }
        }

        public void setResource(Disposable d) {
            DisposableHelper.setOnce(resource, d);
        }
    }
}
