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
 * 将上游各元素映射为 {@link MaybeSource}，待前一个内部源终止后再订阅下一个，
 * 并转发其 onSuccess 值；可按 {@link ErrorMode} 延迟错误至主流与内部源均终止。
 * <p>History: 2.1.11 - experimental
 * @param <T> 上游元素类型
 * @param <R> 下游元素类型
 * @since 2.2
 */
public final class ObservableConcatMapMaybe<T, R> extends Observable<R> {

    final Observable<T> source;

    final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

    final ErrorMode errorMode;

    final int prefetch;

    /**
     * @param source 上游 Observable
     * @param mapper 将元素映射为 MaybeSource 的函数
     * @param errorMode 错误处理模式
     * @param prefetch 预取队列容量
     */
    public ObservableConcatMapMaybe(Observable<T> source,
            Function<? super T, ? extends MaybeSource<? extends R>> mapper,
                    ErrorMode errorMode, int prefetch) {
        this.source = source;
        this.mapper = mapper;
        this.errorMode = errorMode;
        this.prefetch = prefetch;
    }

    /** 标量源走 {@link ScalarXMapZHelper#tryAsMaybe} 快速路径，否则订阅 ConcatMapMaybeMainObserver。 */
    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        if (!ScalarXMapZHelper.tryAsMaybe(source, mapper, observer)) {
            source.subscribe(new ConcatMapMaybeMainObserver<>(observer, mapper, prefetch, errorMode));
        }
    }

    /** 串行 concatMap Maybe：管理内部 Maybe 订阅与 drain 状态机。 */
    static final class ConcatMapMaybeMainObserver<T, R>
    extends ConcatMapXMainObserver<T> {

        @Serial
        private static final long serialVersionUID = -9140123220065488293L;

        final Observer<? super R> downstream;

        final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

        final ConcatMapMaybeObserver<R> inner;

        R item;

        volatile int state;

        /** 无内部 MaybeSource 在运行。 */
        static final int STATE_INACTIVE = 0;
        /** 内部 MaybeSource 运行中，尚无结果。 */
        static final int STATE_ACTIVE = 1;
        /** 内部 MaybeSource 已成功，值在 {@link #item} 中。 */
        static final int STATE_RESULT_VALUE = 2;

        ConcatMapMaybeMainObserver(Observer<? super R> downstream,
                Function<? super T, ? extends MaybeSource<? extends R>> mapper,
                        int prefetch, ErrorMode errorMode) {
            super(prefetch, errorMode);
            this.downstream = downstream;
            this.mapper = mapper;
            this.inner = new ConcatMapMaybeObserver<>(this);
        }

        @Override
        void onSubscribeDownstream() {
            downstream.onSubscribe(this);
        }

        @Override
        void clearValue() {
            item = null;
        }

        /** 内部 onSuccess：缓存值并触发 drain。 */
        void innerSuccess(R item) {
            this.item = item;
            this.state = STATE_RESULT_VALUE;
            drain();
        }

        /** 内部 onComplete：重置为 INACTIVE 并 drain。 */
        void innerComplete() {
            this.state = STATE_INACTIVE;
            drain();
        }

        /** 内部 onError：按 errorMode 记录错误并 drain。 */
        void innerError(Throwable ex) {
            if (errors.tryAddThrowableOrReport(ex)) {
                if (errorMode != ErrorMode.END) {
                    upstream.dispose();
                }
                this.state = STATE_INACTIVE;
                drain();
            }
        }

        @Override
        void disposeInner() {
            inner.dispose();
        }

        @Override
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            Observer<? super R> downstream = this.downstream;
            ErrorMode errorMode = this.errorMode;
            SimpleQueue<T> queue = this.queue;
            AtomicThrowable errors = this.errors;

            for (;;) {

                for (;;) {
                    if (disposed) {
                        queue.clear();
                        item = null;
                        break;
                    }

                    int s = state;

                    if (errors.get() != null) {
                        if (errorMode == ErrorMode.IMMEDIATE
                                || (errorMode == ErrorMode.BOUNDARY && s == STATE_INACTIVE)) {
                            queue.clear();
                            item = null;
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }
                    }

                    if (s == STATE_INACTIVE) {
                        boolean d = done;
                        T v;

                        try {
                            v = queue.poll();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            disposed = true;
                            upstream.dispose();
                            errors.tryAddThrowableOrReport(ex);
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }
                        boolean empty = v == null;

                        if (d && empty) {
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }

                        if (empty) {
                            break;
                        }

                        MaybeSource<? extends R> ms;

                        try {
                            ms = Objects.requireNonNull(mapper.apply(v), "The mapper returned a null MaybeSource");
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            upstream.dispose();
                            queue.clear();
                            errors.tryAddThrowableOrReport(ex);
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }

                        state = STATE_ACTIVE;
                        ms.subscribe(inner);
                        break;
                    } else if (s == STATE_RESULT_VALUE) {
                        R w = item;
                        item = null;
                        downstream.onNext(w);

                        state = STATE_INACTIVE;
                    } else {
                        break;
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        /** 订阅单个内部 MaybeSource 并将信号转发给 parent。 */
        static final class ConcatMapMaybeObserver<R>
        extends AtomicReference<Disposable>
        implements MaybeObserver<R> {

            @Serial
            private static final long serialVersionUID = -3051469169682093892L;

            final ConcatMapMaybeMainObserver<?, R> parent;

            ConcatMapMaybeObserver(ConcatMapMaybeMainObserver<?, R> parent) {
                this.parent = parent;
            }

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.replace(this, d);
            }

            @Override
            public void onSuccess(R t) {
                parent.innerSuccess(t);
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
