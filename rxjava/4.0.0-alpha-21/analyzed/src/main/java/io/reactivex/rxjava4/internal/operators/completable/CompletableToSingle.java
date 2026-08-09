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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;

/**
 * 将 {@link CompletableSource} 转为 {@link Single}；完成时发出 completionValue 或 Supplier 提供的值。
 * @param <T> Single 值类型
 */
public final class CompletableToSingle<T> extends Single<T> {
    final CompletableSource source;

    final Supplier<? extends T> completionValueSupplier;

    final T completionValue;

    /**
     * @param source 上游 CompletableSource
     * @param completionValueSupplier 完成时提供值的 Supplier（可为 null）
     * @param completionValue Supplier 为 null 时使用的完成值
     */
    public CompletableToSingle(CompletableSource source,
            Supplier<? extends T> completionValueSupplier, T completionValue) {
        this.source = source;
        this.completionValue = completionValue;
        this.completionValueSupplier = completionValueSupplier;
    }

    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {
        source.subscribe(new ToSingle(observer));
    }

    final class ToSingle implements CompletableObserver {

        private final SingleObserver<? super T> observer;

        ToSingle(SingleObserver<? super T> observer) {
            this.observer = observer;
        }

        /** 从 Supplier 或 completionValue 取值，非 null 则 onSuccess，否则 onError。 */
        @Override
        public void onComplete() {
            T v;

            if (completionValueSupplier != null) {
                try {
                    v = completionValueSupplier.get();
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    observer.onError(e);
                    return;
                }
            } else {
                v = completionValue;
            }

            if (v == null) {
                observer.onError(new NullPointerException("The value supplied is null"));
            } else {
                observer.onSuccess(v);
            }
        }

        /** 转发 onError。 */
        @Override
        public void onError(Throwable e) {
            observer.onError(e);
        }

        /** 转发 onSubscribe。 */
        @Override
        public void onSubscribe(Disposable d) {
            observer.onSubscribe(d);
        }

    }
}
