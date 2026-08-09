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

package io.reactivex.rxjava4.internal.operators.completable;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

import java.util.Objects;

/**
 * 当主源发出 onError 时，通过函数生成并返回一个值。
 * @param <T> 值类型
 * @since 3.0.0
 */
public final class CompletableOnErrorReturn<T> extends Maybe<T> {

    final CompletableSource source;

    final Function<? super Throwable, ? extends T> valueSupplier;

    /**
     * @param source 上游 CompletableSource
     * @param valueSupplier 错误发生时生成替代值的函数
     */
    public CompletableOnErrorReturn(CompletableSource source,
            Function<? super Throwable, ? extends T> valueSupplier) {
        this.source = source;
        this.valueSupplier = valueSupplier;
    }

    /** 订阅 source；错误时调用 valueSupplier 并发出 onSuccess。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new OnErrorReturnMaybeObserver<>(observer, valueSupplier));
    }

    /** 错误时将 supplier 返回值作为 Maybe 成功值转发的内部 observer。 */
    static final class OnErrorReturnMaybeObserver<T> implements CompletableObserver, Disposable {

        final MaybeObserver<? super T> downstream;

        final Function<? super Throwable, ? extends T> itemSupplier;

        Disposable upstream;

        OnErrorReturnMaybeObserver(MaybeObserver<? super T> actual,
                Function<? super Throwable, ? extends T> itemSupplier) {
            this.downstream = actual;
            this.itemSupplier = itemSupplier;
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

        /** 调用 itemSupplier 并将结果作为 onSuccess 转发。 */
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
