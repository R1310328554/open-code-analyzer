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

import java.util.concurrent.Callable;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 订阅时同步调用 {@link Callable}，成功则 onComplete，异常则 onError。
 */
public final class CompletableFromCallable extends Completable {

    final Callable<?> callable;

    /** @param callable 要调用的 Callable */
    public CompletableFromCallable(Callable<?> callable) {
        this.callable = callable;
    }

    /** 调用 callable 并根据结果完成或出错。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        Disposable d = Disposable.empty();
        observer.onSubscribe(d);
        try {
            callable.call();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            if (!d.isDisposed()) {
                observer.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
            return;
        }
        if (!d.isDisposed()) {
            observer.onComplete();
        }
    }
}
