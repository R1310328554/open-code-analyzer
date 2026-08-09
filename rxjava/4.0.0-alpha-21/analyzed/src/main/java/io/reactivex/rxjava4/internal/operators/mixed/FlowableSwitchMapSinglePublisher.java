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

/**
 * 对 {@link Publisher} 各元素映射并切换 {@link SingleSource}。
 * 复用 {@link FlowableSwitchMapSingle} 内部实现。
 * @param <T> 上游元素类型
 * @param <R> 下游元素类型
 * @since 3.0.0
 */
public final class FlowableSwitchMapSinglePublisher<T, R> extends Flowable<R> {

    final Publisher<T> source;

    final Function<? super T, ? extends SingleSource<? extends R>> mapper;

    final boolean delayErrors;

    /**
     * @param source 上游 Publisher
     * @param mapper 由 T 映射 SingleSource 的函数
     * @param delayErrors 是否延迟合并错误
     */
    public FlowableSwitchMapSinglePublisher(Publisher<T> source,
            Function<? super T, ? extends SingleSource<? extends R>> mapper,
            boolean delayErrors) {
        this.source = source;
        this.mapper = mapper;
        this.delayErrors = delayErrors;
    }

    /** 复用 SwitchMapSingleSubscriber 订阅任意 Publisher。 */
    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        source.subscribe(new FlowableSwitchMapSingle.SwitchMapSingleSubscriber<>(s, mapper, delayErrors));
    }
}
