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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;

import java.util.Objects;

/**
 * 订阅时调用 {@link Supplier} 获取错误并立即向 observer 发出。
 */
public final class CompletableErrorSupplier extends Completable {

    final Supplier<? extends Throwable> errorSupplier;

    /** @param errorSupplier 提供错误的 Supplier */
    public CompletableErrorSupplier(Supplier<? extends Throwable> errorSupplier) {
        this.errorSupplier = errorSupplier;
    }

    /** 获取错误并通过 {@link EmptyDisposable#error} 发出。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        Throwable error;

        try {
            error = Objects.requireNonNull(errorSupplier.get(), "The error returned is null");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            error = e;
        }

        EmptyDisposable.error(error, observer);
    }

}
