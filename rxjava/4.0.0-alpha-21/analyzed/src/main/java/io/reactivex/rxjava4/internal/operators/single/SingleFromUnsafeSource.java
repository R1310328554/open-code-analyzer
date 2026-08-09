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

/**
 * 将 {@link SingleSource} 包装为 {@link Single}，不做线程安全或生命周期校验。
 * 直接透传 subscribe 调用，供内部 unsafe 场景使用。
 * @param <T> 元素类型
 */
public final class SingleFromUnsafeSource<T> extends Single<T> {
    final SingleSource<T> source;

    /** @param source 待包装的上游 SingleSource */
    public SingleFromUnsafeSource(SingleSource<T> source) {
        this.source = source;
    }

    /** 直接调用 source.subscribe，无额外拦截或调度。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(observer);
    }
}
