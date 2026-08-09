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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.util.Objects;

/**
 * 订阅时调用 supplier.get() 获取值并 onSuccess，
 * 异常则 onError（已 dispose 时 RxJavaPlugins.onError）。
 * @param <T> 返回值类型
 * @since 3.0.0
 */
public final class SingleFromSupplier<T> extends Single<T> {

    final Supplier<? extends T> supplier;

    /** @param supplier 提供单值的 Supplier */
    public SingleFromSupplier(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    /** 先 onSubscribe(empty Disposable)，再 supplier.get() 并发射结果。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        Disposable d = Disposable.empty();
        observer.onSubscribe(d);

        if (d.isDisposed()) {
            return;
        }
        T value;

        try {
            value = Objects.requireNonNull(supplier.get(), "The supplier returned a null value");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            if (!d.isDisposed()) {
                observer.onError(ex);
            } else {
                RxJavaPlugins.onError(ex);
            }
            return;
        }

        if (!d.isDisposed()) {
            observer.onSuccess(value);
        }
    }
}
