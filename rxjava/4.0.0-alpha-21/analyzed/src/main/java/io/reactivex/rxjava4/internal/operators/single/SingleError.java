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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;

/**
 * 订阅时调用 errorSupplier 获取 Throwable，
 * 经 EmptyDisposable.error 立即向下游发射错误。
 *
 * @param <T> 元素类型（永不成功发射）
 */
public final class SingleError<T> extends Single<T> {

    final Supplier<? extends Throwable> errorSupplier;

    /** @param errorSupplier 提供要发射的 Throwable 的 Supplier */
    public SingleError(Supplier<? extends Throwable> errorSupplier) {
        this.errorSupplier = errorSupplier;
    }

    /** 调用 errorSupplier.get() 后 EmptyDisposable.error 通知下游。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        Throwable error;

        try {
            error = ExceptionHelper.nullCheck(errorSupplier.get(), "Supplier returned a null Throwable.");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            error = e;
        }

        EmptyDisposable.error(error, observer);
    }

}
