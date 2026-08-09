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

/* ===== [OCA 中文解析] =====
文件意图总览

window(selector)：上游每个元素经 selector 映射为 boundary Observable，boundary 完成时关闭当前窗口并开启新 UnicastSubject 窗口。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.internal.operators.observable;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.Observable;
import io.reactivex.rxjava4.core.ObservableSource;
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.queue.MpscLinkedQueue;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.SimplePlainQueue;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;
import io.reactivex.rxjava4.subjects.UnicastSubject;

/* ===== [OCA 中文解析] =====
class ObservableWindowBoundarySelector — 意图说明

WindowBoundaryMainObserver 管理 selector 边界与窗口切换。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * WindowBoundaryMainObserver 管理 selector 边界与窗口切换。
 */
public final class ObservableWindowBoundarySelector<T, B, V> extends AbstractObservableWithUpstream<T, Observable<T>> {
    final ObservableSource<B> open;
    final Function<? super B, ? extends ObservableSource<V>> closingIndicator;
    final int bufferSize;

    public ObservableWindowBoundarySelector(
            ObservableSource<T> source,
            ObservableSource<B> open, Function<? super B, ? extends ObservableSource<V>> closingIndicator,
            int bufferSize) {
        super(source);
        this.open = open;
        this.closingIndicator = closingIndicator;
        this.bufferSize = bufferSize;
    }

    /** 组装内部 Observer 并订阅上游。 */


    @Override


    public void subscribeActual(Observer<? super Observable<T>> t) {
        source.subscribe(new WindowBoundaryMainObserver<>(
                t, open, closingIndicator, bufferSize));
    }

    /** 内部 WindowBoundaryMainObserver。 */


    static final class WindowBoundaryMainObserver<T, B, V>
    extends AtomicInteger
    implements Observer<T>, Disposable, Runnable {
        @Serial
        private static final long serialVersionUID = 8646217640096099753L;

        final Observer<? super Observable<T>> downstream;
        final ObservableSource<B> open;
        final Function<? super B, ? extends ObservableSource<V>> closingIndicator;
        final int bufferSize;
        final CompositeDisposable resources;

        final WindowStartObserver<B> startObserver;

        final List<UnicastSubject<T>> windows;

        final SimplePlainQueue<Object> queue;

        final AtomicLong windowCount;

        final AtomicBoolean downstreamDisposed;

        final AtomicLong requested;
        long emitted;

        volatile boolean upstreamCancelled;

        volatile boolean upstreamDone;
        volatile boolean openDone;
        final AtomicThrowable error;

        Disposable upstream;

        WindowBoundaryMainObserver(Observer<? super Observable<T>> downstream,
                ObservableSource<B> open, Function<? super B, ? extends ObservableSource<V>> closingIndicator, int bufferSize) {
            this.downstream = downstream;
            this.queue = new MpscLinkedQueue<>();
            this.open = open;
            this.closingIndicator = closingIndicator;
            this.bufferSize = bufferSize;
            this.resources = new CompositeDisposable();
            this.windows = new ArrayList<>();
            this.windowCount = new AtomicLong(1L);
            this.downstreamDisposed = new AtomicBoolean();
            this.error = new AtomicThrowable();
            this.startObserver = new WindowStartObserver<>(this);
            this.requested = new AtomicLong();
        }

        /** 校验 Disposable 并初始化内部状态。 */


        @Override


        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);

                open.subscribe(startObserver);
            }
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(T t) {
            queue.offer(t);
            drain();
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable t) {
            startObserver.dispose();
            resources.dispose();
            if (error.tryAddThrowableOrReport(t)) {
                upstreamDone = true;
                drain();
            }
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            startObserver.dispose();
            resources.dispose();
            upstreamDone = true;
            drain();
        }

        /** dispose 连接/inner 并清理状态。 */


        @Override


        public void dispose() {
            if (downstreamDisposed.compareAndSet(false, true)) {
                if (windowCount.decrementAndGet() == 0) {
                    upstream.dispose();
                    startObserver.dispose();
                    resources.dispose();
                    error.tryTerminateAndReport();
                    upstreamCancelled = true;
                    drain();
                } else {
                    startObserver.dispose();
                }
            }
        }

        /** 返回是否已 dispose。 */


        @Override


        public boolean isDisposed() {
            return downstreamDisposed.get();
        }

        @Override
        public void run() {
            if (windowCount.decrementAndGet() == 0) {
                upstream.dispose();
                startObserver.dispose();
                resources.dispose();
                error.tryTerminateAndReport();
                upstreamCancelled = true;
                drain();
            }
        }

        void open(B startValue) {
            queue.offer(new WindowStartItem<>(startValue));
            drain();
        }

        void openError(Throwable t) {
            upstream.dispose();
            resources.dispose();
            if (error.tryAddThrowableOrReport(t)) {
                upstreamDone = true;
                drain();
            }
        }

        void openComplete() {
            openDone = true;
            drain();
        }

        void close(WindowEndObserverIntercept<T, V> what) {
            queue.offer(what);
            drain();
        }

        void closeError(Throwable t) {
            upstream.dispose();
            startObserver.dispose();
            resources.dispose();
            if (error.tryAddThrowableOrReport(t)) {
                upstreamDone = true;
                drain();
            }
        }

        /** drain 循环：从队列取元素向下游发射。 */


        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            final Observer<? super Observable<T>> downstream = this.downstream;
            final SimplePlainQueue<Object> queue = this.queue;
            final List<UnicastSubject<T>> windows = this.windows;

            for (;;) {
                if (upstreamCancelled) {
                    queue.clear();
                    windows.clear();
                } else {
                    boolean isDone = upstreamDone;
                    Object o = queue.poll();
                    boolean isEmpty = o == null;

                    if (isDone) {
                        if (isEmpty || error.get() != null) {
                            terminateDownstream(downstream);
                            upstreamCancelled = true;
                            continue;
                        }
                    }

                    if (!isEmpty) {
                        if (o instanceof WindowStartItem) {
                            if (!downstreamDisposed.get()) {
                                @SuppressWarnings("unchecked")
                                B startItem = ((WindowStartItem<B>)o).item;

                                ObservableSource<V> endSource;
                                try {
                                    endSource = Objects.requireNonNull(closingIndicator.apply(startItem), "The closingIndicator returned a null ObservableSource");
                                } catch (Throwable ex) {
                                    Exceptions.throwIfFatal(ex);
                                    upstream.dispose();
                                    startObserver.dispose();
                                    resources.dispose();
                                    Exceptions.throwIfFatal(ex);
                                    error.tryAddThrowableOrReport(ex);
                                    upstreamDone = true;
                                    continue;
                                }

                                windowCount.getAndIncrement();
                                UnicastSubject<T> newWindow = UnicastSubject.create(bufferSize, this);
                                WindowEndObserverIntercept<T, V> endObserver = new WindowEndObserverIntercept<>(this, newWindow);

                                downstream.onNext(endObserver);

                                if (endObserver.tryAbandon()) {
                                    newWindow.onComplete();
                                } else {
                                    windows.add(newWindow);
                                    resources.add(endObserver);
                                    endSource.subscribe(endObserver);
                                }
                            }
                        }
                        else if (o instanceof WindowEndObserverIntercept) {
                            @SuppressWarnings("unchecked")
                            UnicastSubject<T> w = ((WindowEndObserverIntercept<T, V>)o).window;

                            windows.remove(w);
                            resources.delete((Disposable)o);
                            w.onComplete();
                        } else {
                            @SuppressWarnings("unchecked")
                            T item = (T)o;

                            for (UnicastSubject<T> w : windows) {
                                w.onNext(item);
                            }
                        }

                        continue;
                    }
                    else if (openDone && windows.isEmpty()) {
                        upstream.dispose();
                        startObserver.dispose();
                        resources.dispose();
                        terminateDownstream(downstream);
                        upstreamCancelled = true;
                        continue;
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        void terminateDownstream(Observer<?> downstream) {
            Throwable ex = error.terminate();
            if (ex == null) {
                for (UnicastSubject<T> w : windows) {
                    w.onComplete();
                }
                downstream.onComplete();
            } else if (ex != ExceptionHelper.TERMINATED) {
                for (UnicastSubject<T> w : windows) {
                    w.onError(ex);
                }
                downstream.onError(ex);
            }
        }

        record WindowStartItem<B>(B item) {

        }

        /** 内部 WindowStartObserver。 */


        static final class WindowStartObserver<B> extends AtomicReference<Disposable>
        implements Observer<B> {

            @Serial
            private static final long serialVersionUID = -3326496781427702834L;

            final WindowBoundaryMainObserver<?, B, ?> parent;

            WindowStartObserver(WindowBoundaryMainObserver<?, B, ?> parent) {
                this.parent = parent;
            }

            /** 校验 Disposable 并初始化内部状态。 */


            @Override


            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            /** 处理上游 onNext 并转发或缓存。 */


            @Override


            public void onNext(B t) {
                parent.open(t);
            }

            /** 处理上游/onError 并按策略终止或延迟错误。 */


            @Override


            public void onError(Throwable t) {
                parent.openError(t);
            }

            /** 上游完成：清理资源并向下游发送 onComplete。 */


            @Override


            public void onComplete() {
                parent.openComplete();
            }

            void dispose() {
                DisposableHelper.dispose(this);
            }
        }

        /** 内部 WindowEndObserverIntercept。 */


        static final class WindowEndObserverIntercept<T, V> extends Observable<T>
        implements Observer<V>, Disposable {

            final WindowBoundaryMainObserver<T, ?, V> parent;

            final UnicastSubject<T> window;

            final AtomicReference<Disposable> upstream;

            final AtomicBoolean once;

            WindowEndObserverIntercept(WindowBoundaryMainObserver<T, ?, V> parent, UnicastSubject<T> window) {
                this.parent = parent;
                this.window = window;
                this.upstream = new AtomicReference<>();
                this.once = new AtomicBoolean();
            }

            /** 校验 Disposable 并初始化内部状态。 */


            @Override


            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(upstream, d);
            }

            /** 处理上游 onNext 并转发或缓存。 */


            @Override


            public void onNext(V t) {
                if (DisposableHelper.dispose(upstream)) {
                    parent.close(this);
                }
            }

            /** 处理上游/onError 并按策略终止或延迟错误。 */


            @Override


            public void onError(Throwable t) {
                if (isDisposed()) {
                    RxJavaPlugins.onError(t);
                } else {
                    parent.closeError(t);
                }
            }

            /** 上游完成：清理资源并向下游发送 onComplete。 */


            @Override


            public void onComplete() {
                parent.close(this);
            }

            /** dispose 连接/inner 并清理状态。 */


            @Override


            public void dispose() {
                DisposableHelper.dispose(upstream);
            }

            /** 返回是否已 dispose。 */


            @Override


            public boolean isDisposed() {
                return upstream.get() == DisposableHelper.DISPOSED;
            }

            /** 组装内部 Observer 并订阅上游。 */


            @Override


            protected void subscribeActual(Observer<? super T> o) {
                window.subscribe(o);
                once.set(true);
            }

            boolean tryAbandon() {
                return !once.get() && once.compareAndSet(false, true);
            }
        }
    }
}
