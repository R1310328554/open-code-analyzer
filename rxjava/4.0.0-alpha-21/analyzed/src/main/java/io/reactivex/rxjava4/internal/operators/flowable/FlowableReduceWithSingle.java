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

package io.reactivex.rxjava4.internal.operators.flowable;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.internal.operators.flowable.FlowableReduceSeedSingle.ReduceSeedObserver;

import java.util.Objects;

/**
 * 订阅时由 {@link Supplier} 生成 seed，再用累加器归约并以 {@link Single} 发射。
 *
 * @param <T> 上游元素类型
 * @param <R> 累加结果类型
 */
public final class FlowableReduceWithSingle<T, R> extends Single<R> {

    final Publisher<T> source;

    final Supplier<R> seedSupplier;

    final BiFunction<R, ? super T, R> reducer;

    /**
     * @param source 上游 Publisher
     * @param seedSupplier 初始累加值 Supplier
     * @param reducer 累加器函数
     */
    public FlowableReduceWithSingle(Publisher<T> source, Supplier<R> seedSupplier, BiFunction<R, ? super T, R> reducer) {
        this.source = source;
        this.seedSupplier = seedSupplier;
        this.reducer = reducer;
    }

    /** 获取 seed 后委托 {@link ReduceSeedObserver} 订阅上游。 */
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
