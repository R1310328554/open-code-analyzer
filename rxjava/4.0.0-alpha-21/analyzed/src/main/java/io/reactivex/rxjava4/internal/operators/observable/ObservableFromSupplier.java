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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.internal.observers.DeferredScalarDisposable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 调用 {@link Supplier} 并发射其返回的单个值，或转发异常。
 * 同时实现 {@link Supplier} 供标量融合路径使用。
 * @param <T> 元素类型
 * @since 3.0.0
 */
public final class ObservableFromSupplier<T> extends Observable<T> implements Supplier<T> {

    final Supplier<? extends T> supplier;

    /** @param supplier 提供单个元素的 Supplier */
    public ObservableFromSupplier(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    /** 经 DeferredScalarDisposable 调用 supplier.get() 并 complete 单值。 */
    @Override
    public void subscribeActual(Observer<? super T> observer) {
        DeferredScalarDisposable<T> d = new DeferredScalarDisposable<>(observer);
        observer.onSubscribe(d);
        if (d.isDisposed()) {
            return;
        }
        T value;
        try {
            value = ExceptionHelper.nullCheck(supplier.get(), "Supplier returned a null value.");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            if (!d.isDisposed()) {
                observer.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
            return;
        }
        d.complete(value);
    }

    /** Supplier 路径：直接返回 supplier.get()（null 检查）。 */
    @Override
    public T get() throws Throwable {
        return ExceptionHelper.nullCheck(supplier.get(), "The supplier returned a null value.");
    }
}
