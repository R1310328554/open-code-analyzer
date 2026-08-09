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
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;

/**
 * 订阅时调用 {@link Supplier} 获取 Throwable 并立即 onError；
 * Supplier 异常同样经 {@link EmptyDisposable#error} 转发。
 *
 * @param <T> 元素类型（不会发射）
 */
public final class ObservableError<T> extends Observable<T> {
    final Supplier<? extends Throwable> errorSupplier;
    /** @param errorSupplier 提供错误对象的 Supplier */
    public ObservableError(Supplier<? extends Throwable> errorSupplier) {
        this.errorSupplier = errorSupplier;
    }

    /** 求值 errorSupplier 并将结果或捕获异常作为 onError 信号。 */
    @Override
    public void subscribeActual(Observer<? super T> observer) {
        Throwable error;
        try {
            error = ExceptionHelper.nullCheck(errorSupplier.get(), "Supplier returned a null Throwable.");
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
            error = t;
        }
        EmptyDisposable.error(error, observer);
    }
}
