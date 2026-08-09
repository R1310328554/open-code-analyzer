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

package io.reactivex.rxjava4.internal.operators.mixed;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.SimpleQueue;

/**
 * 将上游 Observable 元素映射为 {@link CompletableSource}，
 * 在前一个完成或终止后再串行订阅下一个（错误延迟模式下可延后终止）。
 * <p>History: 2.1.11 - experimental
 * @param <T> 上游元素类型
 * @since 2.2
 */
public final class ObservableConcatMapCompletable<T> extends Completable {

    final Observable<T> source;

    final Function<? super T, ? extends CompletableSource> mapper;

    final ErrorMode errorMode;

    final int prefetch;

    /**
     * @param source 上游 Observable
     * @param mapper 由 T 映射 CompletableSource 的函数
     * @param errorMode 错误处理模式
     * @param prefetch 预取队列容量
     */
    public ObservableConcatMapCompletable(Observable<T> source,
            Function<? super T, ? extends CompletableSource> mapper,
            ErrorMode errorMode,
            int prefetch) {
        this.source = source;
        this.mapper = mapper;
        this.errorMode = errorMode;
        this.prefetch = prefetch;
    }

    /** 标量优化失败时订阅 ConcatMapCompletableObserver。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        if (!ScalarXMapZHelper.tryAsCompletable(source, mapper, observer)) {
            source.subscribe(new ConcatMapCompletableObserver<>(observer, mapper, errorMode, prefetch));
        }
    }

    /** 管理队列与 inner Completable 串行 drain（Observable 版）。 */
    static final class ConcatMapCompletableObserver<T>
    extends ConcatMapXMainObserver<T> {

        @Serial
        private static final long serialVersionUID = 3610901111000061034L;

        final CompletableObserver downstream;

        final Function<? super T, ? extends CompletableSource> mapper;

        final ConcatMapInnerObserver inner;

        volatile boolean active;

        ConcatMapCompletableObserver(CompletableObserver downstream,
                Function<? super T, ? extends CompletableSource> mapper,
                ErrorMode errorMode, int prefetch) {
            super(prefetch, errorMode);
            this.downstream = downstream;
            this.mapper = mapper;
            this.inner = new ConcatMapInnerObserver(this);
        }

        @Override
        void onSubscribeDownstream() {
            downstream.onSubscribe(this);
        }

        @Override
        void disposeInner() {
            inner.dispose();
        }

        /** inner onError：非 END 模式 dispose 上游并继续 drain。 */
        void innerError(Throwable ex) {
            if (errors.tryAddThrowableOrReport(ex)) {
                if (errorMode != ErrorMode.END) {
                    upstream.dispose();
                }
                active = false;
                drain();
            }
        }

        /** inner onComplete 后 active=false 并继续 drain 下一项。 */
        void innerComplete() {
            active = false;
            drain();
        }

        @Override
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            AtomicThrowable errors = this.errors;
            ErrorMode errorMode = this.errorMode;
            SimpleQueue<T> queue = this.queue;

            do {
                if (disposed) {
                    queue.clear();
                    return;
                }

                if (errors.get() != null) {
                    if (errorMode == ErrorMode.IMMEDIATE
                            || (errorMode == ErrorMode.BOUNDARY && !active)) {
                        disposed = true;
                        queue.clear();
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }
                }

                if (!active) {

                    boolean d = done;
                    boolean empty = true;
                    CompletableSource cs = null;
                    try {
                        T v = queue.poll();
                        if (v != null) {
                            cs = Objects.requireNonNull(mapper.apply(v), "The mapper returned a null CompletableSource");
                            empty = false;
                        }
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        disposed = true;
                        queue.clear();
                        upstream.dispose();
                        errors.tryAddThrowableOrReport(ex);
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }

                    if (d && empty) {
                        disposed = true;
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }

                    if (!empty) {
                        active = true;
                        cs.subscribe(inner);
                    }
                }
            } while (decrementAndGet() != 0);
        }

        /** 订阅单个 inner Completable 并将信号 relay 到 parent。 */
        static final class ConcatMapInnerObserver extends AtomicReference<Disposable>
        implements CompletableObserver {

            @Serial
            private static final long serialVersionUID = 5638352172918776687L;

            final ConcatMapCompletableObserver<?> parent;

            ConcatMapInnerObserver(ConcatMapCompletableObserver<?> parent) {
                this.parent = parent;
            }

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.replace(this, d);
            }

            @Override
            public void onError(Throwable e) {
                parent.innerError(e);
            }

            @Override
            public void onComplete() {
                parent.innerComplete();
            }

            void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
