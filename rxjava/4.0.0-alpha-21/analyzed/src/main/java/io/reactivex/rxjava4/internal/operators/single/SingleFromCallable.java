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

import java.util.Objects;
import java.util.concurrent.Callable;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 订阅时同步调用 callable.call()，
 * 成功则 onSuccess，异常则 onError（已 dispose 时 RxJavaPlugins.onError）。
 *
 * @param <T> 元素类型
 */
public final class SingleFromCallable<T> extends Single<T> {

    final Callable<? extends T> callable;

    /** @param callable 提供单值的 Callable */
    public SingleFromCallable(Callable<? extends T> callable) {
        this.callable = callable;
    }

    /** 先 onSubscribe(empty Disposable)，再 callable.call() 并发射结果。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        Disposable d = Disposable.empty();
        observer.onSubscribe(d);

        if (d.isDisposed()) {
            return;
        }
        T value;

        try {
            value = Objects.requireNonNull(callable.call(), "The callable returned a null value");
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
