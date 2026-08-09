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
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.internal.operators.observable.ObservableReduceSeedSingle.ReduceSeedObserver;

import java.util.Objects;

/**
 * 订阅时调用 seedSupplier 获取初始值，再用 reducer 累加上游元素，
 * 委托 {@link ObservableReduceSeedSingle.ReduceSeedObserver} 完成归约。
 *
 * @param <T> 上游元素类型
 * @param <R> 累积结果类型
 */
public final class ObservableReduceWithSingle<T, R> extends Single<R> {

    final ObservableSource<T> source;

    final Supplier<R> seedSupplier;

    final BiFunction<R, ? super T, R> reducer;

    /**
     * @param source 上游 ObservableSource
     * @param seedSupplier 提供初始累积值的 Supplier
     * @param reducer 累加器 (acc, value) -> acc
     */
    public ObservableReduceWithSingle(ObservableSource<T> source, Supplier<R> seedSupplier, BiFunction<R, ? super T, R> reducer) {
        this.source = source;
        this.seedSupplier = seedSupplier;
        this.reducer = reducer;
    }

    /** 调用 seedSupplier 获取 seed 后订阅 ReduceSeedObserver。 */
    @Override
    protected void subscribeActual(SingleObserver<? super R> observer) {
        R seed;

        try {
            seed = Objects.requireNonNull(seedSupplier.get(), "The seedSupplier returned a null value");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return;
        }
        source.subscribe(new ReduceSeedObserver<>(observer, reducer, seed));
    }
}
