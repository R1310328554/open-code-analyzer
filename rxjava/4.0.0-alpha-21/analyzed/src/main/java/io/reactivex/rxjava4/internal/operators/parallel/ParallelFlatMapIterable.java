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

package io.reactivex.rxjava4.internal.operators.parallel;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.operators.flowable.FlowableFlattenIterable;
import io.reactivex.rxjava4.parallel.ParallelFlowable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 对每条并行轨道将元素经 mapper 映射为 Iterable 并逐元素展开发射。
 *
 * @param <T> 输入元素类型
 * @param <R> 输出元素类型
 * @since 3.0.0
 */
public final class ParallelFlatMapIterable<T, R> extends ParallelFlowable<R> {

    final ParallelFlowable<T> source;

    final Function<? super T, ? extends Iterable<? extends R>> mapper;

    final int prefetch;

    /**
     * @param source 并行上游
     * @param mapper 将元素映射为 Iterable 的函数
     * @param prefetch 预取数量
     */
    public ParallelFlatMapIterable(
            ParallelFlowable<T> source,
            Function<? super T, ? extends Iterable<? extends R>> mapper,
            int prefetch) {
        this.source = source;
        this.mapper = mapper;
        this.prefetch = prefetch;
    }

    @Override
    public int parallelism() {
        return source.parallelism();
    }

    /** 每条轨道用 FlowableFlattenIterable.subscribe 包装下游 Subscriber。 */
    @Override
    public void subscribe(Subscriber<? super R>[] subscribers) {
        subscribers = RxJavaPlugins.onSubscribe(this, subscribers);

        if (!validate(subscribers)) {
            return;
        }

        int n = subscribers.length;

        @SuppressWarnings("unchecked")
        final Subscriber<T>[] parents = new Subscriber[n];

        for (int i = 0; i < n; i++) {
            parents[i] = FlowableFlattenIterable.subscribe(subscribers[i], mapper, prefetch);
        }

        source.subscribe(parents);
    }
}
