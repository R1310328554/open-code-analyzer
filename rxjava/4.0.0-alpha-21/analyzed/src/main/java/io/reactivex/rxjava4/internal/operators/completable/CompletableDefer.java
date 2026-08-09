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
 * 每次订阅时通过 {@link Supplier} 获取新的 {@link CompletableSource} 并订阅。
 */
public final class CompletableDefer extends Completable {

    final Supplier<? extends CompletableSource> completableSupplier;

    /** @param completableSupplier 每次订阅时提供 CompletableSource 的 Supplier */
    public CompletableDefer(Supplier<? extends CompletableSource> completableSupplier) {
        this.completableSupplier = completableSupplier;
    }

    /** 调用 supplier 获取 CompletableSource 并订阅；supplier 异常时报告错误。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        CompletableSource c;

        try {
            c = Objects.requireNonNull(completableSupplier.get(), "The completableSupplier returned a null CompletableSource");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptyDisposable.error(e, observer);
            return;
        }

        c.subscribe(observer);
    }

}
