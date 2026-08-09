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
import io.reactivex.rxjava4.internal.operators.flowable.FlowableElementAtMaybe.ElementAtSubscriber;

/**
 * 将 {@link Publisher} 的第 index 个元素作为 {@link Maybe} 发射。
 *
 * @param <T> 源元素类型
 * @since 3.0.0
 */
public final class FlowableElementAtMaybePublisher<T> extends Maybe<T> {

    final Publisher<T> source;

    final long index;

    /**
     * @param source 上游 Publisher
     * @param index 目标索引（0 起）
     */
    public FlowableElementAtMaybePublisher(Publisher<T> source, long index) {
        this.source = source;
        this.index = index;
    }

    /** 复用 {@link FlowableElementAtMaybe.ElementAtSubscriber} 订阅 source。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new ElementAtSubscriber<>(observer, index));
    }
}
