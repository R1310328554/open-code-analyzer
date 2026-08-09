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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;

import java.util.Objects;

/**
 * 延迟创建实际 {@link MaybeSource}，直到 {@link MaybeObserver} 订阅时才调用 Supplier。
 *
 * @param <T> 元素类型
 */
public final class MaybeDefer<T> extends Maybe<T> {

    final Supplier<? extends MaybeSource<? extends T>> maybeSupplier;

    /** @param maybeSupplier 延迟提供 MaybeSource 的 Supplier */
    public MaybeDefer(Supplier<? extends MaybeSource<? extends T>> maybeSupplier) {
        this.maybeSupplier = maybeSupplier;
    }

    /** 调用 Supplier 获取 MaybeSource 并订阅 observer。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        MaybeSource<? extends T> source;

        try {
            source = Objects.requireNonNull(maybeSupplier.get(), "The maybeSupplier returned a null MaybeSource");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return;
        }

        source.subscribe(observer);
    }
}
