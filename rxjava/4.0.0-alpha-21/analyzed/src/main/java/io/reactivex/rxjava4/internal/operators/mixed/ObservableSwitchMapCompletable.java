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
import io.reactivex.rxjava4.internal.util.AtomicThrowable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将上游值映射为 {@link CompletableSource}，订阅较新内部源同时 dispose 旧订阅，
 * 保证至多一个内部 Completable 处于活动状态。
 * <p>History: 2.1.11 - experimental
 * @param <T> 上游元素类型
 * @since 2.2
 */
public final class ObservableSwitchMapCompletable<T> extends Completable {

    final Observable<T> source;

    final Function<? super T, ? extends CompletableSource> mapper;

    final boolean delayErrors;

    /**
     * @param source 上游 Observable
     * @param mapper 将元素映射为 CompletableSource 的函数
     * @param delayErrors 是否延迟合并错误
     */
    public ObservableSwitchMapCompletable(Observable<T> source,
            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {
        this.source = source;
        this.mapper = mapper;
        this.delayErrors = delayErrors;
    }

    /** 标量源走 {@link ScalarXMapZHelper#tryAsCompletable} 快速路径，否则订阅 SwitchMapCompletableObserver。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        if (!ScalarXMapZHelper.tryAsCompletable(source, mapper, observer)) {
            source.subscribe(new SwitchMapCompletableObserver<>(observer, mapper, delayErrors));
        }
    }

    /** 主流 Observer：onNext 时切换内部 Completable 订阅。 */
    static final class SwitchMapCompletableObserver<T> implements Observer<T>, Disposable {

        final CompletableObserver downstream;

        final Function<? super T, ? extends CompletableSource> mapper;

        final boolean delayErrors;

        final AtomicThrowable errors;

        final AtomicReference<SwitchMapInnerObserver> inner;

        static final SwitchMapInnerObserver INNER_DISPOSED = new SwitchMapInnerObserver(null);

        volatile boolean done;

        Disposable upstream;

        SwitchMapCompletableObserver(CompletableObserver downstream,
                Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {
            this.downstream = downstream;
            this.mapper = mapper;
            this.delayErrors = delayErrors;
            this.errors = new AtomicThrowable();
            this.inner = new AtomicReference<>();
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            CompletableSource c;

            try {
                c = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null CompletableSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.dispose();
                onError(ex);
                return;
            }

            SwitchMapInnerObserver o = new SwitchMapInnerObserver(this);

            for (;;) {
                SwitchMapInnerObserver current = inner.get();
                if (current == INNER_DISPOSED) {
                    break;
                }
                if (inner.compareAndSet(current, o)) {
                    if (current != null) {
                        current.dispose();
                    }
                    c.subscribe(o);
                    break;
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (errors.tryAddThrowableOrReport(t)) {
                if (delayErrors) {
                    onComplete();
                } else {
                    disposeInner();
                    errors.tryTerminateConsumer(downstream);
                }
            }
        }

        @Override
        public void onComplete() {
            done = true;
            if (inner.get() == null) {
                errors.tryTerminateConsumer(downstream);
            }
        }

        void disposeInner() {
            SwitchMapInnerObserver o = inner.getAndSet(INNER_DISPOSED);
            if (o != null && o != INNER_DISPOSED) {
                o.dispose();
            }
        }

        @Override
        public void dispose() {
            upstream.dispose();
            disposeInner();
            errors.tryTerminateAndReport();
        }

        @Override
        public boolean isDisposed() {
            return inner.get() == INNER_DISPOSED;
        }

        /** 内部 onError：按 delayErrors 决定立即终止或延迟合并。 */
        void innerError(SwitchMapInnerObserver sender, Throwable error) {
            if (inner.compareAndSet(sender, null)) {
                if (errors.tryAddThrowableOrReport(error)) {
                    if (delayErrors) {
                        if (done) {
                            errors.tryTerminateConsumer(downstream);
                        }
                    } else {
                        upstream.dispose();
                        disposeInner();
                        errors.tryTerminateConsumer(downstream);
                    }
                }
            } else {
                RxJavaPlugins.onError(error);
            }
        }

        /** 内部 onComplete：若主流已完成则向下游 onComplete。 */
        void innerComplete(SwitchMapInnerObserver sender) {
            if (inner.compareAndSet(sender, null)) {
                if (done) {
                    errors.tryTerminateConsumer(downstream);
                }
            }
        }

        /** 单个内部 Completable 的 observer，将信号转发给 parent。 */
        static final class SwitchMapInnerObserver extends AtomicReference<Disposable>
        implements CompletableObserver {

            @Serial
            private static final long serialVersionUID = -8003404460084760287L;

            final SwitchMapCompletableObserver<?> parent;

            SwitchMapInnerObserver(SwitchMapCompletableObserver<?> parent) {
                this.parent = parent;
            }

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onError(Throwable e) {
                parent.innerError(this, e);
            }

            @Override
            public void onComplete() {
                parent.innerComplete(this);
            }

            void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
