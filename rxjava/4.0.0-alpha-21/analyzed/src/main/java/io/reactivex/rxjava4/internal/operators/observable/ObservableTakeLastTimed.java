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
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;

/**
 * 按时间窗口与 count 限制缓存元素；上游终止后 drain 发射未过期项。
 * @param <T> 元素类型
 */
public final class ObservableTakeLastTimed<T> extends AbstractObservableWithUpstream<T, T> {
    final long count;
    final long time;
    final TimeUnit unit;
    final Scheduler scheduler;
    final int bufferSize;
    final boolean delayError;

    /**
     * @param source 上游 ObservableSource
     * @param count 最大保留元素数（Long.MAX_VALUE 表示不限）
     * @param time 时间窗口长度
     * @param unit 时间单位
     * @param scheduler 提供 now 时间戳的 Scheduler
     * @param bufferSize 队列容量
     * @param delayError 为 true 时延迟报告错误直至 drain
     */
    public ObservableTakeLastTimed(ObservableSource<T> source,
            long count, long time, TimeUnit unit, Scheduler scheduler, int bufferSize, boolean delayError) {
        super(source);
        this.count = count;
        this.time = time;
        this.unit = unit;
        this.scheduler = scheduler;
        this.bufferSize = bufferSize;
        this.delayError = delayError;
    }

    @Override
    public void subscribeActual(Observer<? super T> t) {
        source.subscribe(new TakeLastTimedObserver<>(t, count, time, unit, scheduler, bufferSize, delayError));
    }

    /** SpscLinkedArrayQueue 存 (timestamp, value) 对；drain 过滤过期项。 */
    static final class TakeLastTimedObserver<T>
    extends AtomicBoolean implements Observer<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -5677354903406201275L;
        final Observer<? super T> downstream;
        final long count;
        final long time;
        final TimeUnit unit;
        final Scheduler scheduler;
        final SpscLinkedArrayQueue<Object> queue;
        final boolean delayError;

        Disposable upstream;

        volatile boolean cancelled;

        Throwable error;

        TakeLastTimedObserver(Observer<? super T> actual, long count, long time, TimeUnit unit, Scheduler scheduler, int bufferSize, boolean delayError) {
            this.downstream = actual;
            this.count = count;
            this.time = time;
            this.unit = unit;
            this.scheduler = scheduler;
            this.queue = new SpscLinkedArrayQueue<>(bufferSize);
            this.delayError = delayError;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        /** offer (now, t) 并剔除超出时间窗口或 count 的旧项。 */
        @Override
        public void onNext(T t) {
            final SpscLinkedArrayQueue<Object> q = queue;

            long now = scheduler.now(unit);
            long time = this.time;
            long c = count;
            boolean unbounded = c == Long.MAX_VALUE;

            q.offer(now, t);

            while (!q.isEmpty()) {
                long ts = (Long)q.peek();
                if (ts <= now - time || (!unbounded && (q.size() >> 1) > c)) {
                    q.poll();
                    q.poll();
                } else {
                    break;
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            error = t;
            drain();
        }

        @Override
        public void onComplete() {
            drain();
        }

        @Override
        public void dispose() {
            if (!cancelled) {
                cancelled = true;
                upstream.dispose();

                if (compareAndSet(false, true)) {
                    queue.clear();
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled;
        }

        /** compareAndSet 门控：poll 未过期项 onNext，空队列时终止。 */
        void drain() {
            if (!compareAndSet(false, true)) {
                return;
            }

            final Observer<? super T> a = downstream;
            final SpscLinkedArrayQueue<Object> q = queue;
            final boolean delayError = this.delayError;
            final long timestampLimit = scheduler.now(unit) - time;

            for (;;) {
                if (cancelled) {
                    q.clear();
                    return;
                }

                if (!delayError) {
                    Throwable ex = error;
                    if (ex != null) {
                        q.clear();
                        a.onError(ex);
                        return;
                    }
                }

                Object ts = q.poll(); // the timestamp long
                boolean empty = ts == null;

                if (empty) {
                    Throwable ex = error;
                    if (ex != null) {
                        a.onError(ex);
                    } else {
                        a.onComplete();
                    }
                    return;
                }

                @SuppressWarnings("unchecked")
                T o = (T)q.poll();

                if ((Long)ts < timestampLimit) {
                    continue;
                }

                a.onNext(o);
            }
        }
    }
}
