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
 * 将上游元素映射为 {@link CompletableSource}，
 * 在前一个完成或终止后再串行订阅下一个（错误延迟模式下可延后终止）。
 * <p>History: 2.1.11 - experimental
 * @param <T> 上游元素类型
 * @since 2.2
 */
public final class FlowableConcatMapCompletable<T> extends Completable {

    final Flowable<T> source;

    final Function<? super T, ? extends CompletableSource> mapper;

    final ErrorMode errorMode;

    final int prefetch;

    /**
     * @param source 上游 Flowable
     * @param mapper 由 T 映射 CompletableSource 的函数
     * @param errorMode 错误处理模式
     * @param prefetch 预取队列容量
     */
    public FlowableConcatMapCompletable(Flowable<T> source,
            Function<? super T, ? extends CompletableSource> mapper,
            ErrorMode errorMode,
            int prefetch) {
        this.source = source;
        this.mapper = mapper;
        this.errorMode = errorMode;
        this.prefetch = prefetch;
    }

    /** 订阅 ConcatMapCompletableObserver 串行执行 inner Completable。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        source.subscribe(new ConcatMapCompletableObserver<>(observer, mapper, errorMode, prefetch));
    }

    /** 管理队列、背压与 inner Completable 串行 drain。 */
    static final class ConcatMapCompletableObserver<T>
    extends ConcatMapXMainSubscriber<T>
    implements Disposable {

        @Serial
        private static final long serialVersionUID = 3610901111000061034L;

        final CompletableObserver downstream;

        final Function<? super T, ? extends CompletableSource> mapper;

        final ConcatMapInnerObserver inner;

        volatile boolean active;

        int consumed;

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

        @Override
        public void dispose() {
            stop();
        }

        @Override
        public boolean isDisposed() {
            return cancelled;
        }

        /** inner onError：IMMEDIATE 立即终止，否则 active=false 继续 drain。 */
        void innerError(Throwable ex) {
            if (errors.tryAddThrowableOrReport(ex)) {
                if (errorMode == ErrorMode.IMMEDIATE) {
                    upstream.cancel();
                    errors.tryTerminateConsumer(downstream);
                    if (getAndIncrement() == 0) {
                        queue.clear();
                    }
                } else {
                    active = false;
                    drain();
                }
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

            ErrorMode errorMode = this.errorMode;
            SimpleQueue<T> queue = this.queue;
            AtomicThrowable errors = this.errors;
            boolean syncFused = this.syncFused;

            do {
                if (cancelled) {
                    queue.clear();
                    return;
                }

                if (errors.get() != null) {
                    if (errorMode == ErrorMode.IMMEDIATE
                            || (errorMode == ErrorMode.BOUNDARY && !active)) {
                        queue.clear();
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }
                }

                if (!active) {

                    boolean d = done;
                    T v;
                    try {
                        v = queue.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        upstream.cancel();
                        errors.tryAddThrowableOrReport(ex);
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }
                    boolean empty = v == null;

                    if (d && empty) {
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }

                    if (!empty) {

                        int limit = prefetch - (prefetch >> 1);

                        if (!syncFused) {
                            int c = consumed + 1;
                            if (c == limit) {
                                consumed = 0;
                                upstream.request(limit);
                            } else {
                                consumed = c;
                            }
                        }

                        CompletableSource cs;

                        try {
                            cs = Objects.requireNonNull(mapper.apply(v), "The mapper returned a null CompletableSource");
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            queue.clear();
                            upstream.cancel();
                            errors.tryAddThrowableOrReport(ex);
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }
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
