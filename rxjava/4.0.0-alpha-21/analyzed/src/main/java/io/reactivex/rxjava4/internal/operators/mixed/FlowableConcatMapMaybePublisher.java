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

package io.reactivex.rxjava4.internal.operators.mixed;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.operators.mixed.FlowableConcatMapMaybe.ConcatMapMaybeSubscriber;
import io.reactivex.rxjava4.core.ErrorMode;

/**
 * 将 {@link Publisher} 各元素映射为 {@link MaybeSource} 并串行订阅，
 * 转发 inner onSuccess 值；复用 {@link FlowableConcatMapMaybe} 的 Subscriber 实现。
 * <p>History: 2.1.11 - experimental
 * @param <T> 上游元素类型
 * @param <R> 下游元素类型
 * @since 2.2
 */
public final class FlowableConcatMapMaybePublisher<T, R> extends Flowable<R> {

    final Publisher<T> source;

    final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

    final ErrorMode errorMode;

    final int prefetch;

    /**
     * @param source 上游 Publisher
     * @param mapper 由 T 映射 MaybeSource 的函数
     * @param errorMode 错误处理模式
     * @param prefetch 预取队列容量
     */
    public FlowableConcatMapMaybePublisher(Publisher<T> source,
            Function<? super T, ? extends MaybeSource<? extends R>> mapper,
                    ErrorMode errorMode, int prefetch) {
        this.source = source;
        this.mapper = mapper;
        this.errorMode = errorMode;
        this.prefetch = prefetch;
    }

    /** 复用 ConcatMapMaybeSubscriber 订阅任意 Publisher。 */
    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        source.subscribe(new ConcatMapMaybeSubscriber<>(s, mapper, prefetch, errorMode));
    }
}
