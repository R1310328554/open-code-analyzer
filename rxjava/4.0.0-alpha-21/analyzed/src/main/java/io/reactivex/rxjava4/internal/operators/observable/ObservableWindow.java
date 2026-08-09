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
import java.util.ArrayDeque;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.subjects.UnicastSubject;

/**
 * 将上游按 count/skip 切分为多个窗口 Observable。
 * count==skip 时用 WindowExactObserver；否则用滑动 WindowSkipObserver。
 * @param <T> 元素类型
 */
public final class ObservableWindow<T> extends AbstractObservableWithUpstream<T, Observable<T>> {
    final long count;
    final long skip;
    final int capacityHint;

    /**
     * @param source 上游 ObservableSource
     * @param count 每个窗口最多元素数
     * @param skip 窗口起始间隔（与 count 相等时为不重叠窗口）
     * @param capacityHint UnicastSubject 容量提示
     */
    public ObservableWindow(ObservableSource<T> source, long count, long skip, int capacityHint) {
        super(source);
        this.count = count;
        this.skip = skip;
        this.capacityHint = capacityHint;
    }

    /** count==skip 选 Exact，否则选 Skip 策略。 */
    @Override
    public void subscribeActual(Observer<? super Observable<T>> t) {
        if (count == skip) {
            source.subscribe(new WindowExactObserver<>(t, count, capacityHint));
        } else {
            source.subscribe(new WindowSkipObserver<>(t, count, skip, capacityHint));
        }
    }

    /** 固定大小不重叠窗口；refCount 归零时 dispose 上游。 */
    static final class WindowExactObserver<T>
    extends AtomicInteger
    implements Observer<T>, Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = -7481782523886138128L;
        final Observer<? super Observable<T>> downstream;
        final long count;
        final int capacityHint;

        final AtomicBoolean cancelled;

        long size;

        Disposable upstream;

        UnicastSubject<T> window;

        WindowExactObserver(Observer<? super Observable<T>> actual, long count, int capacityHint) {
            this.downstream = actual;
            this.count = count;
            this.capacityHint = capacityHint;
            this.cancelled = new AtomicBoolean();
            this.lazySet(1);
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** 懒创建 UnicastSubject 并下发 intercept；满 count 时 onComplete 当前窗口。 */
        @Override
        public void onNext(T t) {
            UnicastSubject<T> w = window;
            ObservableWindowSubscribeIntercept<T> intercept = null;
            if (w == null && !cancelled.get()) {
                getAndIncrement();

                w = UnicastSubject.create(capacityHint, this);
                window = w;
                intercept = new ObservableWindowSubscribeIntercept<>(w);
                downstream.onNext(intercept);
            }

            if (w != null) {
                w.onNext(t);

                if (++size >= count) {
                    size = 0;
                    window = null;
                    w.onComplete();
                }

                if (intercept != null && intercept.tryAbandon()) {
                    window = null;
                    w.onComplete();
                    w = null;
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            UnicastSubject<T> w = window;
            if (w != null) {
                window = null;
                w.onError(t);
            }
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            UnicastSubject<T> w = window;
            if (w != null) {
                window = null;
                w.onComplete();
            }
            downstream.onComplete();
        }

        @Override
        public void dispose() {
            if (cancelled.compareAndSet(false, true)) {
                run();
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled.get();
        }

        @Override
        public void run() {
            if (decrementAndGet() == 0) {
                upstream.dispose();
            }
        }
    }

    /** 滑动窗口：ArrayDeque 维护多个 UnicastSubject，按 skip 开启新窗。 */
    static final class WindowSkipObserver<T> extends AtomicInteger
    implements Observer<T>, Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = 3366976432059579510L;
        final Observer<? super Observable<T>> downstream;
        final long count;
        final long skip;
        final int capacityHint;
        final ArrayDeque<UnicastSubject<T>> windows;

        final AtomicBoolean cancelled;

        long index;

        /** 记录已发射到 deque 首窗口的元素计数。 */
        long firstEmission;

        Disposable upstream;

        WindowSkipObserver(Observer<? super Observable<T>> actual, long count, long skip, int capacityHint) {
            this.downstream = actual;
            this.count = count;
            this.skip = skip;
            this.capacityHint = capacityHint;
            this.windows = new ArrayDeque<>();
            this.cancelled = new AtomicBoolean();
            this.lazySet(1);
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            final ArrayDeque<UnicastSubject<T>> ws = windows;

            long i = index;

            long s = skip;

            ObservableWindowSubscribeIntercept<T> intercept = null;

            if (i % s == 0 && !cancelled.get()) {
                getAndIncrement();
                UnicastSubject<T> w = UnicastSubject.create(capacityHint, this);
                intercept = new ObservableWindowSubscribeIntercept<>(w);
                ws.offer(w);
                downstream.onNext(intercept);
            }

            long c = firstEmission + 1;

            for (UnicastSubject<T> w : ws) {
                w.onNext(t);
            }

            if (c >= count) {
                ws.poll().onComplete();
                if (ws.isEmpty() && cancelled.get()) {
                    return;
                }
                firstEmission = c - s;
            } else {
                firstEmission = c;
            }

            index = i + 1;

            if (intercept != null && intercept.tryAbandon()) {
                intercept.window.onComplete();
            }
        }

        @Override
        public void onError(Throwable t) {
            final ArrayDeque<UnicastSubject<T>> ws = windows;
            while (!ws.isEmpty()) {
                ws.poll().onError(t);
            }
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            final ArrayDeque<UnicastSubject<T>> ws = windows;
            while (!ws.isEmpty()) {
                ws.poll().onComplete();
            }
            downstream.onComplete();
        }

        @Override
        public void dispose() {
            if (cancelled.compareAndSet(false, true)) {
                run();
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled.get();
        }

        @Override
        public void run() {
            if (decrementAndGet() == 0) {
                upstream.dispose();
            }
        }
    }
}
