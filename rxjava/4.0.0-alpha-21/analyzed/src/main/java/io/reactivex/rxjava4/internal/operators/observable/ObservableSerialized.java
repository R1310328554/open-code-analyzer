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

package io.reactivex.rxjava4.internal.operators.observable;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.observers.SerializedObserver;

/**
 * 用 {@link SerializedObserver} 包装下游，保证 onNext/onError/onComplete 串行、不并发重入。
 *
 * @param <T> 元素类型
 */
public final class ObservableSerialized<T> extends AbstractObservableWithUpstream<T, T> {
    /** @param upstream 待序列化回调的上游 Observable */
    public ObservableSerialized(Observable<T> upstream) {
        super(upstream);
    }

    /** 订阅上游并将事件经 SerializedObserver 转发给下游。 */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new SerializedObserver<>(observer));
    }
}
