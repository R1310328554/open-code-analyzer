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

import java.util.Objects;

/**
 * 延迟创建：每次订阅时调用 {@link Supplier} 获取新的 {@link ObservableSource} 再订阅。
 *
 * @param <T> 元素类型
 */
public final class ObservableDefer<T> extends Observable<T> {
    final Supplier<? extends ObservableSource<? extends T>> supplier;
    /** @param supplier 每次订阅时提供 ObservableSource 的 Supplier */
    public ObservableDefer(Supplier<? extends ObservableSource<? extends T>> supplier) {
        this.supplier = supplier;
    }

    /** 调用 supplier.get()，null 或异常经 EmptyDisposable 处理。 */
    @Override
    public void subscribeActual(Observer<? super T> observer) {
        ObservableSource<? extends T> pub;
        try {
            pub = Objects.requireNonNull(supplier.get(), "The supplier returned a null ObservableSource");
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
            EmptyDisposable.error(t, observer);
            return;
        }

        pub.subscribe(observer);
    }
}
