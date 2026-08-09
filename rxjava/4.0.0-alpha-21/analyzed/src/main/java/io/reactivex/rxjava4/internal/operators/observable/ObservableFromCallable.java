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

import java.util.concurrent.Callable;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.internal.observers.DeferredScalarDisposable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 调用 {@link Callable} 并将单个结果作为 onNext 后 onComplete；
 * 异常经 onError 转发。实现 {@link Supplier} 供标量融合。
 * @param <T> 元素类型
 */
public final class ObservableFromCallable<T> extends Observable<T> implements Supplier<T> {

    final Callable<? extends T> callable;

    /** @param callable 订阅时调用的 Callable */
    public ObservableFromCallable(Callable<? extends T> callable) {
        this.callable = callable;
    }

    /** 经 DeferredScalarDisposable 调用 callable 并 complete 单值。 */
    @Override
    public void subscribeActual(Observer<? super T> observer) {
        DeferredScalarDisposable<T> d = new DeferredScalarDisposable<>(observer);
        observer.onSubscribe(d);
        if (d.isDisposed()) {
            return;
        }
        T value;
        try {
            value = ExceptionHelper.nullCheck(callable.call(), "Callable returned a null value.");
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

    /** 标量路径：直接 call callable 并 nullCheck 结果。 */
    @Override
    public T get() throws Throwable {
        return ExceptionHelper.nullCheck(callable.call(), "The Callable returned a null value.");
    }
}
