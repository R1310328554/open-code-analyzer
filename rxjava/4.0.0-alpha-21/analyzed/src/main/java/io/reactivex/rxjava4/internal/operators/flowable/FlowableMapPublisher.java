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
import io.reactivex.rxjava4.internal.operators.flowable.FlowableMap.MapSubscriber;

/**
 * 对任意 {@link Publisher} 源中的每个元素应用 map 映射。
 * <p>History: 2.0.7 - experimental
 * @param <T> 输入值类型
 * @param <U> 输出值类型
 * @since 2.1
 */
public final class FlowableMapPublisher<T, U> extends Flowable<U> {

    final Publisher<T> source;

    final Function<? super T, ? extends U> mapper;
    /**
     * @param source 上游 Publisher
     * @param mapper 元素映射函数
     */
    public FlowableMapPublisher(Publisher<T> source, Function<? super T, ? extends U> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    /** 复用 {@link FlowableMap.MapSubscriber} 订阅任意 Publisher。 */
    @Override
    protected void subscribeActual(Subscriber<? super U> s) {
        source.subscribe(new MapSubscriber<T, U>(s, mapper));
    }
}
