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
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.operators.flowable.FlowableFlatMapSingle.FlatMapSingleSubscriber;

/**
 * 将上游每个元素映射为 {@link SingleSource} 并合并其成功值为单一序列；
 * 适用于任意 {@link Publisher} 源。
 * @param <T> 上游元素类型
 * @param <R> 输出元素类型
 */
public final class FlowableFlatMapSinglePublisher<T, R> extends Flowable<R> {

    final Publisher<T> source;

    final Function<? super T, ? extends SingleSource<? extends R>> mapper;

    final boolean delayErrors;

    final int maxConcurrency;

    /**
     * @param source 上游 Publisher
     * @param mapper 将元素映射为 SingleSource 的函数
     * @param delayError 为 true 时延迟报告错误
     * @param maxConcurrency 最大并行 inner 订阅数
     */
    public FlowableFlatMapSinglePublisher(Publisher<T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper,
            boolean delayError, int maxConcurrency) {
        this.source = source;
        this.mapper = mapper;
        this.delayErrors = delayError;
        this.maxConcurrency = maxConcurrency;
    }

    /** 复用 {@link FlowableFlatMapSingle.FlatMapSingleSubscriber} 订阅 source。 */
    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        source.subscribe(new FlatMapSingleSubscriber<>(s, mapper, delayErrors, maxConcurrency));
    }
}
