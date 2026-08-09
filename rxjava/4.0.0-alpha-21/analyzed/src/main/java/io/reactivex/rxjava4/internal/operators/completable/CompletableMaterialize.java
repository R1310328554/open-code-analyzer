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

package io.reactivex.rxjava4.internal.operators.completable;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.operators.mixed.MaterializeSingleObserver;

/**
 * 将 {@link Completable} 源的信号类型转换为同类型的单个 {@link Notification}。
 * <p>History: 2.2.4 - experimental
 *
 * @param <T> 源元素类型
 * @since 3.0.0
 */
public final class CompletableMaterialize<T> extends Single<Notification<T>> {

    final Completable source;

    /** @param source 要物化的 Completable 源 */
    public CompletableMaterialize(Completable source) {
        this.source = source;
    }

    /** 订阅 source 并将终止事件包装为 Notification 发出。 */
    @Override
    protected void subscribeActual(SingleObserver<? super Notification<T>> observer) {
        source.subscribe(new MaterializeSingleObserver<>(observer));
    }
}
