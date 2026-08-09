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
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.queue.MpscLinkedQueue;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;
import io.reactivex.rxjava4.observers.DisposableObserver;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;
import io.reactivex.rxjava4.subjects.UnicastSubject;

/**
 * 由 other 流每个 onNext 作为边界，切分主序列成多个窗口 Observable。
 * @param <T> 主序列元素类型
 * @param <B> 边界信号类型
 */
public final class ObservableWindowBoundary<T, B> extends AbstractObservableWithUpstream<T, Observable<T>> {
    final ObservableSource<B> other;
    final int capacityHint;

    /**
     * @param source 主序列 ObservableSource
     * @param other 边界 ObservableSource
     * @param capacityHint 每个 UnicastSubject 容量提示
     */
    public ObservableWindowBoundary(ObservableSource<T> source, ObservableSource<B> other, int capacityHint) {
        super(source);
        this.other = other;
        this.capacityHint = capacityHint;
    }

    /** 先 onSubscribe parent，再订阅 boundary 与主序列。 */
    @Override
    public void subscribeActual(Observer<? super Observable<T>> observer) {
        WindowBoundaryMainObserver<T, B> parent = new WindowBoundaryMainObserver<>(observer, capacityHint);

        observer.onSubscribe(parent);
        other.subscribe(parent.boundaryObserver);

        source.subscribe(parent);
    }

    /** MpscLinkedQueue 串行 drain；NEXT_WINDOW 标记开启新窗。 */
    static final class WindowBoundaryMainObserver<T, B>
    extends AtomicInteger
    implements Observer<T>, Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = 2233020065421370272L;

        final Observer<? super Observable<T>> downstream;

        final int capacityHint;

        final WindowBoundaryInnerObserver<T, B> boundaryObserver;

        final AtomicReference<Disposable> upstream;

        final AtomicInteger windows;

        final MpscLinkedQueue<Object> queue;

        final AtomicThrowable errors;

        final AtomicBoolean stopWindows;

        static final Object NEXT_WINDOW = new Object();

        volatile boolean done;

        UnicastSubject<T> window;

        WindowBoundaryMainObserver(Observer<? super Observable<T>> downstream, int capacityHint) {
            this.downstream = downstream;
            this.capacityHint = capacityHint;
            this.boundaryObserver = new WindowBoundaryInnerObserver<>(this);
            this.upstream = new AtomicReference<>();
            this.windows = new AtomicInteger(1);
            this.queue = new MpscLinkedQueue<>();
            this.errors = new AtomicThrowable();
            this.stopWindows = new AtomicBoolean();
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(upstream, d)) {

                innerNext();
            }
        }

        @Override
        public void onNext(T t) {
            queue.offer(t);
            drain();
        }

        @Override
        public void onError(Throwable e) {
            boundaryObserver.dispose();
            if (errors.tryAddThrowableOrReport(e)) {
                done = true;
                drain();
            }
        }

        @Override
        public void onComplete() {
            boundaryObserver.dispose();
            done = true;
            drain();
        }

        @Override
        public void dispose() {
            if (stopWindows.compareAndSet(false, true)) {
                boundaryObserver.dispose();
                if (windows.decrementAndGet() == 0) {
                    DisposableHelper.dispose(upstream);
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return stopWindows.get();
        }

        @Override
        public void run() {
            if (windows.decrementAndGet() == 0) {
                DisposableHelper.dispose(upstream);
            }
        }

        void innerNext() {
            queue.offer(NEXT_WINDOW);
            drain();
        }

        void innerError(Throwable e) {
            DisposableHelper.dispose(upstream);
            if (errors.tryAddThrowableOrReport(e)) {
                done = true;
                drain();
            }
        }

        void innerComplete() {
            DisposableHelper.dispose(upstream);
            done = true;
            drain();
        }

        @SuppressWarnings("unchecked")
        /** wip 门控：poll 元素入当前窗或 NEXT_WINDOW 时关闭旧窗开新窗。 */
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            Observer<? super Observable<T>> downstream = this.downstream;
            MpscLinkedQueue<Object> queue = this.queue;
            AtomicThrowable errors = this.errors;

            for (;;) {

                for (;;) {
                    if (windows.get() == 0) {
                        queue.clear();
                        window = null;
                        return;
                    }

                    UnicastSubject<T> w = window;

                    boolean d = done;

                    if (d && errors.get() != null) {
                        queue.clear();
                        Throwable ex = errors.terminate();
                        if (w != null) {
                            window = null;
                            w.onError(ex);
                        }
                        downstream.onError(ex);
                        return;
                    }

                    Object v = queue.poll();

                    boolean empty = v == null;

                    if (d && empty) {
                        Throwable ex = errors.terminate();
                        if (ex == null) {
                            if (w != null) {
                                window = null;
                                w.onComplete();
                            }
                            downstream.onComplete();
                        } else {
                            if (w != null) {
                                window = null;
                                w.onError(ex);
                            }
                            downstream.onError(ex);
                        }
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    if (v != NEXT_WINDOW) {
                        w.onNext((T)v);
                        continue;
                    }

                    if (w != null) {
                        window = null;
                        w.onComplete();
                    }

                    if (!stopWindows.get()) {
                        w = UnicastSubject.create(capacityHint, this);
                        window = w;
                        windows.getAndIncrement();

                        ObservableWindowSubscribeIntercept<T> intercept = new ObservableWindowSubscribeIntercept<>(w);
                        downstream.onNext(intercept);
                        if (intercept.tryAbandon()) {
                            w.onComplete();
                        }
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }

    /** 边界 onNext 触发 parent.innerNext；完成/错误终止主序列。 */
    static final class WindowBoundaryInnerObserver<T, B> extends DisposableObserver<B> {

        final WindowBoundaryMainObserver<T, B> parent;

        boolean done;

        WindowBoundaryInnerObserver(WindowBoundaryMainObserver<T, B> parent) {
            this.parent = parent;
        }

        @Override
        public void onNext(B t) {
            if (done) {
                return;
            }
            parent.innerNext();
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            parent.innerError(t);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            parent.innerComplete();
        }
    }
}
