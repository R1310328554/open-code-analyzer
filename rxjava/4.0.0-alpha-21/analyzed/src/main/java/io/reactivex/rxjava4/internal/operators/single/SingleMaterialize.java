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

package io.reactivex.rxjava4.internal.operators.single;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.operators.mixed.MaterializeSingleObserver;

/**
 * 将上游 Single 的终端信号封装为 {@link Notification} 后发射。
 * 成功 → Notification.createOnNext；错误 → Notification.createOnError。
 * @param <T> 上游元素类型
 * @since 3.0.0
 */
public final class SingleMaterialize<T> extends Single<Notification<T>> {

    final Single<T> source;

    /** @param source 待物化的上游 Single */
    public SingleMaterialize(Single<T> source) {
        this.source = source;
    }

    /** 用 MaterializeSingleObserver 将 onSuccess/onError 转为 Notification。 */
    @Override
    protected void subscribeActual(SingleObserver<? super Notification<T>> observer) {
        source.subscribe(new MaterializeSingleObserver<>(observer));
    }
}
