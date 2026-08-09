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
import io.reactivex.rxjava4.subscribers.SerializedSubscriber;

/**
 * 序列化下游事件，保证 onNext/onError/onComplete 不会并发交错。
 * @param <T> 元素类型
 */
public final class FlowableSerialized<T> extends AbstractFlowableWithUpstream<T, T> {
    /** @param source 上游 Flowable */
    public FlowableSerialized(Flowable<T> source) {
        super(source);
    }

    /** 用 {@link SerializedSubscriber} 包装下游以保证事件串行。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new SerializedSubscriber<>(s));
    }
}
