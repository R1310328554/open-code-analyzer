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

import io.reactivex.rxjava4.core.Flowable;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.operators.flowable.FlowableConcatMapEager.ConcatMapEagerDelayErrorSubscriber;
import io.reactivex.rxjava4.core.ErrorMode;

/**
 * 适用于任意 {@link Publisher} 源的 ConcatMapEager 实现。
 * <p>History: 2.0.7 - experimental
 * @param <T> 输入元素类型
 * @param <R> 输出元素类型
 * @since 2.1
 */
public final class FlowableConcatMapEagerPublisher<T, R> extends Flowable<R> {

    final Publisher<T> source;

    final Function<? super T, ? extends Publisher<? extends R>> mapper;

    final int maxConcurrency;

    final int prefetch;

    final ErrorMode errorMode;

    /**
     * @param source 上游 Publisher
     * @param mapper 将元素映射为 inner Publisher 的函数
     * @param maxConcurrency 最大并行 inner 订阅数
     * @param prefetch 每个 inner 的预取量
     * @param errorMode 错误处理模式
     */
    public FlowableConcatMapEagerPublisher(Publisher<T> source,
            Function<? super T, ? extends Publisher<? extends R>> mapper,
            int maxConcurrency,
            int prefetch,
            ErrorMode errorMode) {
        this.source = source;
        this.mapper = mapper;
        this.maxConcurrency = maxConcurrency;
        this.prefetch = prefetch;
        this.errorMode = errorMode;
    }

    /** 订阅 source 并使用 ConcatMapEagerDelayErrorSubscriber 转发。 */
    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        source.subscribe(new ConcatMapEagerDelayErrorSubscriber<>(
                s, mapper, maxConcurrency, prefetch, errorMode));
    }
}
