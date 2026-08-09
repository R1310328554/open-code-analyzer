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

package io.reactivex.rxjava4.internal.operators.maybe;

import io.reactivex.rxjava4.core.*;

/**
 * 无防护地包装 {@link MaybeSource}，对每个 {@link MaybeObserver} 直接调用其 subscribe()。
 *
 * @param <T> 元素类型
 */
public final class MaybeUnsafeCreate<T> extends AbstractMaybeWithUpstream<T, T> {

    /** @param source 上游 MaybeSource（须自行保证协议安全） */
    public MaybeUnsafeCreate(MaybeSource<T> source) {
        super(source);
    }

    /** 直接将 observer 传给上游 subscribe，无额外包装。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(observer);
    }

}
