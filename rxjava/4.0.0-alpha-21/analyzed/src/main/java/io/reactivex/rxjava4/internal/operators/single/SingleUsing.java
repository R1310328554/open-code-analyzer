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

package io.reactivex.rxjava4.internal.operators.single;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 资源管理模式：resourceSupplier 获取资源 U，
 * singleFunction 生成 SingleSource，终止时 disposer 释放资源；
 * eager 控制资源在信号前还是后释放。
 *
 * @param <T> Single 结果类型
 * @param <U> 资源类型
 */
public final class SingleUsing<T, U> extends Single<T> {

    final Supplier<U> resourceSupplier;
    final Function<? super U, ? extends SingleSource<? extends T>> singleFunction;
    final Consumer<? super U> disposer;
    final boolean eager;

    /**
     * @param resourceSupplier 提供资源 U 的 Supplier
     * @param singleFunction 将资源映射为 SingleSource 的函数
     * @param disposer 释放资源的 Consumer
     * @param eager true 时在 onSuccess/onError 前释放资源
     */
    public SingleUsing(Supplier<U> resourceSupplier,
                       Function<? super U, ? extends SingleSource<? extends T>> singleFunction,
                       Consumer<? super U> disposer,
                       boolean eager) {
        this.resourceSupplier = resourceSupplier;
        this.singleFunction = singleFunction;
        this.disposer = disposer;
        this.eager = eager;
    }

    /** 获取资源、应用 singleFunction 并订阅 UsingSingleObserver。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {

        final U resource; // NOPMD

        try {
            resource = resourceSupplier.get();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return;
        }

        SingleSource<? extends T> source;

        try {
            source = Objects.requireNonNull(singleFunction.apply(resource), "The singleFunction returned a null SingleSource");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);

            if (eager) {
                try {
                    disposer.accept(resource);
                } catch (Throwable exc) {
                    Exceptions.throwIfFatal(exc);
                    ex = new CompositeException(ex, exc);
                }
            }
            EmptyDisposable.error(ex, observer);
            if (!eager) {
                try {
                    disposer.accept(resource);
                } catch (Throwable exc) {
                    Exceptions.throwIfFatal(exc);
                    RxJavaPlugins.onError(exc);
                }
            }
            return;
        }

        source.subscribe(new UsingSingleObserver<T, U>(observer, resource, eager, disposer));
    }

    /** 持有资源引用：按 eager 标志在信号前后调用 disposer。 */
    static final class UsingSingleObserver<T, U> extends
    AtomicReference<Object> implements SingleObserver<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -5331524057054083935L;

        final SingleObserver<? super T> downstream;

        final Consumer<? super U> disposer;

        final boolean eager;

        Disposable upstream;

        UsingSingleObserver(SingleObserver<? super T> actual, U resource, boolean eager,
                Consumer<? super U> disposer) {
            super(resource);
            this.downstream = actual;
            this.eager = eager;
            this.disposer = disposer;
        }

        /** eager 时先 disposeResource 再 upstream.dispose，否则相反。 */
        @Override
        public void dispose() {
            if (eager) {
                disposeResource();
                upstream.dispose();
                upstream = DisposableHelper.DISPOSED;
            } else {
                upstream.dispose();
                upstream = DisposableHelper.DISPOSED;
                disposeResource();
            }
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** eager 时先释放资源再 onSuccess；否则先 onSuccess 再释放。 */
        @SuppressWarnings("unchecked")
        @Override
        public void onSuccess(T value) {
            upstream = DisposableHelper.DISPOSED;

            if (eager) {
                Object u = getAndSet(this);
                if (u != this) {
                    try {
                        disposer.accept((U)u);
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        downstream.onError(ex);
                        return;
                    }
                } else {
                    return;
                }
            }

            downstream.onSuccess(value);

            if (!eager) {
                disposeResource();
            }
        }

        /** eager 时先释放资源再 onError；disposer 异常合并为 CompositeException。 */
        @SuppressWarnings("unchecked")
        @Override
        public void onError(Throwable e) {
            upstream = DisposableHelper.DISPOSED;

            if (eager) {
                Object u = getAndSet(this);
                if (u != this) {
                    try {
                        disposer.accept((U)u);
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        e = new CompositeException(e, ex);
                    }
                } else {
                    return;
                }
            }

            downstream.onError(e);

            if (!eager) {
                disposeResource();
            }
        }

        /** getAndSet 取出资源并 disposer.accept；异常走 RxJavaPlugins.onError。 */
        @SuppressWarnings("unchecked")
        void disposeResource() {
            Object u = getAndSet(this);
            if (u != this) {
                try {
                    disposer.accept((U)u);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    RxJavaPlugins.onError(ex);
                }
            }
        }
    }
}
