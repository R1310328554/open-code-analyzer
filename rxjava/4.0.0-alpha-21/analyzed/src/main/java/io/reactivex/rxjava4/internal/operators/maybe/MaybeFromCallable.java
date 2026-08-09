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

import java.util.concurrent.Callable;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 执行 {@link Callable}，非 null 值 onSuccess，null 时 onComplete，异常时 onError。
 *
 * @param <T> 元素类型
 */
public final class MaybeFromCallable<T> extends Maybe<T> implements Supplier<T> {

    final Callable<? extends T> callable;

    /** @param callable 待执行的 Callable */
    public MaybeFromCallable(Callable<? extends T> callable) {
        this.callable = callable;
    }

    /** 调用 callable.call() 并按返回值或异常转发信号。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        Disposable d = Disposable.empty();
        observer.onSubscribe(d);

        if (!d.isDisposed()) {

            T v;

            try {
                v = callable.call();
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
    public T get() throws Exception {
        return callable.call();
    }
}
