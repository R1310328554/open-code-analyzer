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

package io.reactivex.rxjava4.internal.operators.maybe;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

import java.util.Objects;

/**
 * 主源 onError 时调用 {@link Function} 生成替代值并以 onSuccess 向下游发射。
 * @param <T> 元素类型
 */
public final class MaybeOnErrorReturn<T> extends AbstractMaybeWithUpstream<T, T> {

    final Function<? super Throwable, ? extends T> itemSupplier;

    /**
     * @param source 上游 MaybeSource
     * @param itemSupplier 由异常生成替代 T
     */
    public MaybeOnErrorReturn(MaybeSource<T> source,
            Function<? super Throwable, ? extends T> itemSupplier) {
        super(source);
        this.itemSupplier = itemSupplier;
    }

    /** 包装为 OnErrorReturnMaybeObserver。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new OnErrorReturnMaybeObserver<>(observer, itemSupplier));
    }

    /** onError 时 itemSupplier.apply 得值并 onSuccess。 */
    static final class OnErrorReturnMaybeObserver<T> implements MaybeObserver<T>, Disposable {

        final MaybeObserver<? super T> downstream;

        final Function<? super Throwable, ? extends T> itemSupplier;

        Disposable upstream;

        OnErrorReturnMaybeObserver(MaybeObserver<? super T> actual,
                Function<? super Throwable, ? extends T> valueSupplier) {
            this.downstream = actual;
            this.itemSupplier = valueSupplier;
        }

        @Override
        public void dispose() {
            upstream.dispose();
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

        @Override
        public void onSuccess(T value) {
            downstream.onSuccess(value);
        }

        /** null 或 supplier 异常则 CompositeException。 */
        @Override
        public void onError(Throwable e) {
            T v;

            try {
                v = Objects.requireNonNull(itemSupplier.apply(e), "The itemSupplier returned a null value");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(new CompositeException(e, ex));
                return;
            }

            downstream.onSuccess(v);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
