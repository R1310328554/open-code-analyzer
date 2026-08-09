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
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.observers.BasicIntQueueDisposable;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;

/**
 * 将上游每个元素映射为 {@link CompletableSource} 并等待其全部终止；
 * 不向 downstream 发射元素。
 * @param <T> 上游元素类型
 */
public final class ObservableFlatMapCompletable<T> extends AbstractObservableWithUpstream<T, T> {

    final Function<? super T, ? extends CompletableSource> mapper;

    final boolean delayErrors;

    /**
     * @param source 上游 ObservableSource
     * @param mapper 将元素映射为 CompletableSource 的函数
     * @param delayErrors 为 true 时收集所有 inner 错误后再终止
     */
    public ObservableFlatMapCompletable(ObservableSource<T> source,
            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {
        super(source);
        this.mapper = mapper;
        this.delayErrors = delayErrors;
    }

    /** 并行订阅 inner Completable 并在全部完成后终止 downstream。 */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new FlatMapCompletableMainObserver<>(observer, mapper, delayErrors));
    }

    /** 维护 CompositeDisposable 与 active 计数，协调 inner 完成/错误。 */
    static final class FlatMapCompletableMainObserver<T> extends BasicIntQueueDisposable<T>
    implements Observer<T> {
        @Serial
        private static final long serialVersionUID = 8443155186132538303L;

        final Observer<? super T> downstream;

        final AtomicThrowable errors;

        final Function<? super T, ? extends CompletableSource> mapper;

        final boolean delayErrors;

        final CompositeDisposable set;

        Disposable upstream;

        volatile boolean disposed;

        FlatMapCompletableMainObserver(Observer<? super T> observer, Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {
            this.downstream = observer;
            this.mapper = mapper;
            this.delayErrors = delayErrors;
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

        /** 映射并订阅 inner Completable；null mapper 结果转 onError。 */
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

        @Nullable
        @Override
        public T poll() {
            return null; // always empty
        }

        @Override
        public boolean isEmpty() {
            return true; // always empty
        }

        @Override
        public void clear() {
            // nothing to clear
        }

        @Override
        public int requestFusion(int mode) {
            return mode & ASYNC;
        }

        /** inner 完成时从 set 移除并递减 active 计数。 */
        void innerComplete(InnerObserver inner) {
            set.delete(inner);
            onComplete();
        }

        void innerError(InnerObserver inner, Throwable e) {
            set.delete(inner);
            onError(e);
        }

        /** 订阅单个 inner Completable 并将完成/错误回传 main observer。 */
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
