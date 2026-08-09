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

Observable concatMapEager：预取并 eager 订阅多个 inner Observable，在 maxConcurrency 限制下仍保持 emission 顺序。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.internal.operators.observable;

import java.io.Serial;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.observers.*;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.QueueDisposable;
import io.reactivex.rxjava4.operators.SimpleQueue;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;

/* ===== [OCA 中文解析] =====
class ObservableConcatMapEager — 意图说明

Eager 预取 inner 并在 maxConcurrency 下顺序 drain。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Eager 预取 inner 并在 maxConcurrency 下顺序 drain。
 */
public final class ObservableConcatMapEager<T, R> extends AbstractObservableWithUpstream<T, R> {

    final Function<? super T, ? extends ObservableSource<? extends R>> mapper;

    final ErrorMode errorMode;

    final int maxConcurrency;

    final int prefetch;

    public ObservableConcatMapEager(ObservableSource<T> source,
            Function<? super T, ? extends ObservableSource<? extends R>> mapper,
            ErrorMode errorMode,
            int maxConcurrency, int prefetch) {
        super(source);
        this.mapper = mapper;
        this.errorMode = errorMode;
        this.maxConcurrency = maxConcurrency;
        this.prefetch = prefetch;
    }

    /** 组装内部 Observer 并订阅上游。 */


    @Override


    protected void subscribeActual(Observer<? super R> observer) {
        source.subscribe(new ConcatMapEagerMainObserver<>(observer, mapper, maxConcurrency, prefetch, errorMode));
    }

    /** 内部 ConcatMapEagerMainObserver。 */


    static final class ConcatMapEagerMainObserver<T, R>
    extends AtomicInteger
    implements Observer<T>, Disposable, InnerQueuedObserverSupport<R> {

        @Serial
        private static final long serialVersionUID = 8080567949447303262L;

        final Observer<? super R> downstream;

        final Function<? super T, ? extends ObservableSource<? extends R>> mapper;

        final int maxConcurrency;

        final int prefetch;

        final ErrorMode errorMode;

        final AtomicThrowable errors;

        final ArrayDeque<InnerQueuedObserver<R>> observers;

        SimpleQueue<T> queue;

        Disposable upstream;

        volatile boolean done;

        int sourceMode;

        volatile boolean cancelled;

        InnerQueuedObserver<R> current;

        int activeCount;

        ConcatMapEagerMainObserver(Observer<? super R> actual,
                Function<? super T, ? extends ObservableSource<? extends R>> mapper,
                int maxConcurrency, int prefetch, ErrorMode errorMode) {
            this.downstream = actual;
            this.mapper = mapper;
            this.maxConcurrency = maxConcurrency;
            this.prefetch = prefetch;
            this.errorMode = errorMode;
            this.errors = new AtomicThrowable();
            this.observers = new ArrayDeque<>();
        }

        @SuppressWarnings("unchecked")
        /** 校验 Disposable 并初始化内部状态。 */

        @Override

        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                if (d instanceof QueueDisposable) {
                    QueueDisposable<T> qd = (QueueDisposable<T>) d;

                    int m = qd.requestFusion(QueueDisposable.ANY);
                    if (m == QueueDisposable.SYNC) {
                        sourceMode = m;
                        queue = qd;
                        done = true;

                        downstream.onSubscribe(this);

                        drain();
                        return;
                    }
                    if (m == QueueDisposable.ASYNC) {
                        sourceMode = m;
                        queue = qd;

                        downstream.onSubscribe(this);

                        return;
                    }
                }

                queue = new SpscLinkedArrayQueue<>(prefetch);

                downstream.onSubscribe(this);
            }
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(T value) {
            if (sourceMode == QueueDisposable.NONE) {
                queue.offer(value);
            }
            drain();
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable e) {
            if (errors.tryAddThrowableOrReport(e)) {
                done = true;
                drain();
            }
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            done = true;
            drain();
        }

        /** dispose 连接/inner 并清理状态。 */


        @Override


        public void dispose() {
            if (cancelled) {
                return;
            }
            cancelled = true;
            upstream.dispose();
            errors.tryTerminateAndReport();

            drainAndDispose();
        }

        void drainAndDispose() {
            if (getAndIncrement() == 0) {
                do {
                    queue.clear();
                    disposeAll();
                } while (decrementAndGet() != 0);
            }
        }

        /** 返回是否已 dispose。 */


        @Override


        public boolean isDisposed() {
            return cancelled;
        }

        void disposeAll() {
            InnerQueuedObserver<R> inner = current;

            if (inner != null) {
                inner.dispose();
            }

            for (;;) {

                inner = observers.poll();

                if (inner == null) {
                    return;
                }

                inner.dispose();
            }
        }

        @Override
        public void innerNext(InnerQueuedObserver<R> inner, R value) {
            inner.queue().offer(value);
            drain();
        }

        @Override
        public void innerError(InnerQueuedObserver<R> inner, Throwable e) {
            if (errors.tryAddThrowableOrReport(e)) {
                if (errorMode == ErrorMode.IMMEDIATE) {
                    upstream.dispose();
                }
                inner.setDone();
                drain();
            }
        }

        @Override
        public void innerComplete(InnerQueuedObserver<R> inner) {
            inner.setDone();
            drain();
        }

        @Override
        public /** drain 循环：从队列取元素向下游发射。 */
 void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;

            SimpleQueue<T> q = queue;
            ArrayDeque<InnerQueuedObserver<R>> observers = this.observers;
            Observer<? super R> a = this.downstream;
            ErrorMode errorMode = this.errorMode;

            outer:
            for (;;) {

                int ac = activeCount;

                while (ac != maxConcurrency) {
                    if (cancelled) {
                        q.clear();
                        disposeAll();
                        return;
                    }

                    if (errorMode == ErrorMode.IMMEDIATE) {
                        Throwable ex = errors.get();
                        if (ex != null) {
                            q.clear();
                            disposeAll();

                            errors.tryTerminateConsumer(downstream);
                            return;
                        }
                    }

                    T v;
                    ObservableSource<? extends R> source;

                    try {
                        v = q.poll();

                        if (v == null) {
                            break;
                        }

                        source = Objects.requireNonNull(mapper.apply(v), "The mapper returned a null ObservableSource");
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        upstream.dispose();
                        q.clear();
                        disposeAll();
                        errors.tryAddThrowableOrReport(ex);
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }

                    InnerQueuedObserver<R> inner = new InnerQueuedObserver<>(this, prefetch);

                    observers.offer(inner);

                    source.subscribe(inner);

                    ac++;
                }

                activeCount = ac;

                if (cancelled) {
                    q.clear();
                    disposeAll();
                    return;
                }

                if (errorMode == ErrorMode.IMMEDIATE) {
                    Throwable ex = errors.get();
                    if (ex != null) {
                        q.clear();
                        disposeAll();

                        errors.tryTerminateConsumer(downstream);
                        return;
                    }
                }

                InnerQueuedObserver<R> active = current;

                if (active == null) {
                    if (errorMode == ErrorMode.BOUNDARY) {
                        Throwable ex = errors.get();
                        if (ex != null) {
                            q.clear();
                            disposeAll();

                            errors.tryTerminateConsumer(a);
                            return;
                        }
                    }
                    boolean d = done;

                    active = observers.poll();

                    boolean empty = active == null;

                    if (d && empty) {
                        Throwable ex = errors.get();
                        if (ex != null) {
                            q.clear();
                            disposeAll();

                            errors.tryTerminateConsumer(a);
                        } else {
                            a.onComplete();
                        }
                        return;
                    }

                    if (!empty) {
                        current = active;
                    }

                }

                if (active != null) {
                    SimpleQueue<R> aq = active.queue();

                    for (;;) {
                        if (cancelled) {
                            q.clear();
                            disposeAll();
                            return;
                        }

                        boolean d = active.isDone();

                        if (errorMode == ErrorMode.IMMEDIATE) {
                            Throwable ex = errors.get();
                            if (ex != null) {
                                q.clear();
                                disposeAll();

                                errors.tryTerminateConsumer(a);
                                return;
                            }
                        }

                        R w;

                        try {
                            w = aq.poll();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            errors.tryAddThrowableOrReport(ex);

                            current = null;
                            activeCount--;
                            continue outer;
                        }

                        boolean empty = w == null;

                        if (d && empty) {
                            current = null;
                            activeCount--;
                            continue outer;
                        }

                        if (empty) {
                            break;
                        }

                        a.onNext(w);
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }

}
