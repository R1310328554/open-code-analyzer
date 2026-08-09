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

/**
 * 将 {@link Publisher} 包装为 {@link Flowable}，订阅时直接委托上游 Publisher。
 * @param <T> 元素类型
 */
public final class FlowableFromPublisher<T> extends Flowable<T> {
    final Publisher<? extends T> publisher;

    /** @param publisher 上游 Publisher */
    public FlowableFromPublisher(Publisher<? extends T> publisher) {
        this.publisher = publisher;
    }

    /** 将下游 Subscriber 直接订阅到 wrapped Publisher。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        publisher.subscribe(s);
    }
}
