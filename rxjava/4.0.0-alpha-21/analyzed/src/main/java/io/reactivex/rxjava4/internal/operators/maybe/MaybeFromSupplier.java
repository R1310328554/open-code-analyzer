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
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 执行 {@link Supplier}，非 null 值 onSuccess，null 时 onComplete，异常时 onError。
 *
 * @param <T> 元素类型
 * @since 3.0.0
 */
public final class MaybeFromSupplier<T> extends Maybe<T> implements Supplier<T> {

    final Supplier<? extends T> supplier;

    /** @param supplier 待执行的 Supplier */
    public MaybeFromSupplier(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    /** 调用 supplier.get() 并按返回值或异常转发信号。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        Disposable d = Disposable.empty();
        observer.onSubscribe(d);

        if (!d.isDisposed()) {

            T v;

            try {
                v = supplier.get();
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
                if (v == null) {
                    observer.onComplete();
                } else {
                    observer.onSuccess(v);
                }
            }
        }
    }

    @Override
    public T get() throws Throwable {
        return supplier.get();
    }
}
