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
import java.util.Objects;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.fuseable.FuseToObservable;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * {@link ObservableFlatMapCompletable} 的 {@link Completable} 变体：
 * 映射上游元素为 CompletableSource 并等待全部终止后 onComplete。
 * @param <T> 上游元素类型
 */
public final class ObservableFlatMapCompletableCompletable<T> extends Completable implements FuseToObservable<T> {

    final ObservableSource<T> source;

    final Function<? super T, ? extends CompletableSource> mapper;

    final boolean delayErrors;

    final int bufferSize;

    /**
     * @param source 上游 ObservableSource
     * @param mapper 将元素映射为 CompletableSource 的函数
     * @param delayErrors 为 true 时延迟报告错误
     * @param bufferSize 缓冲大小（保留参数，与 Observable 版语义对齐）
     */
    public ObservableFlatMapCompletableCompletable(ObservableSource<T> source,
            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors, int bufferSize) {
        this.source = source;
        this.mapper = mapper;
        this.delayErrors = delayErrors;
        this.bufferSize = bufferSize;
    }

    /** 订阅 FlatMapCompletableMainObserver 并映射为 Completable 终止信号。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        source.subscribe(new FlatMapCompletableMainObserver<>(observer, mapper, delayErrors, bufferSize));
    }

    /** 融合为 {@link ObservableFlatMapCompletable} 实例。 */
    @Override
    public Observable<T> fuseToObservable() {
        return RxJavaPlugins.onAssembly(new ObservableFlatMapCompletable<>(source, mapper, delayErrors));
    }

    /** Completable 版 main observer：active 归零时 tryTerminateConsumer 下游。 */
    static final class FlatMapCompletableMainObserver<T> extends AtomicInteger implements Disposable, Observer<T> {
        @Serial
        private static final long serialVersionUID = 8443155186132538303L;

        final CompletableObserver downstream;

        final AtomicThrowable errors;

        final Function<? super T, ? extends CompletableSource> mapper;

        final boolean delayErrors;

        final int bufferSize;

        final CompositeDisposable set;

        Disposable upstream;

        volatile boolean disposed;

        FlatMapCompletableMainObserver(CompletableObserver observer, Function<? super T, ? extends CompletableSource> mapper,
                boolean delayErrors, int bufferSize) {
            this.downstream = observer;
            this.mapper = mapper;
            this.delayErrors = delayErrors;
            this.bufferSize = bufferSize;
            this.errors = new AtomicThrowable();
            this.set = new CompositeDisposable();
            this.lazySet(1);
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** 映射并并行订阅 inner Completable。 */
        @Override
        public void onNext(T value) {
            CompletableSource cs;

            try {
                cs = Objects.requireNonNull(mapper.apply(value), "The mapper returned a null CompletableSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.dispose();
                onError(ex);
                return;
            }

            getAndIncrement();

            InnerObserver inner = new InnerObserver();

            if (!disposed && set.add(inner)) {
                cs.subscribe(inner);
            }
        }

        @Override
        public void onError(Throwable e) {
            if (errors.tryAddThrowableOrReport(e)) {
                if (delayErrors) {
                    if (decrementAndGet() == 0) {
                        errors.tryTerminateConsumer(downstream);
                    }
                } else {
                    disposed = true;
                    upstream.dispose();
                    set.dispose();
                    errors.tryTerminateConsumer(downstream);
                }
            }
        }

        @Override
        public void onComplete() {
            if (decrementAndGet() == 0) {
                errors.tryTerminateConsumer(downstream);
            }
        }

        @Override
        public void dispose() {
            disposed = true;
            upstream.dispose();
            set.dispose();
            errors.tryTerminateAndReport();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        void innerComplete(InnerObserver inner) {
            set.delete(inner);
            onComplete();
        }

        void innerError(InnerObserver inner, Throwable e) {
            set.delete(inner);
            onError(e);
        }

        /** 单个 inner Completable 的 CompletableObserver 包装。 */
        final class InnerObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable {
            @Serial
            private static final long serialVersionUID = 8606673141535671828L;

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onComplete() {
                innerComplete(this);
            }

            @Override
            public void onError(Throwable e) {
                innerError(this, e);
            }

            @Override
            public void dispose() {
                DisposableHelper.dispose(this);
            }

            @Override
            public boolean isDisposed() {
                return DisposableHelper.isDisposed(get());
            }
        }
    }
}
