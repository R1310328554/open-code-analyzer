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
import io.reactivex.rxjava4.internal.fuseable.HasUpstreamMaybeSource;

/**
 * 接受上游 {@link MaybeSource} 的中间 Maybe 算子抽象基类。
 *
 * @param <T> 上游值类型
 * @param <R> 输出值类型
 */
abstract class AbstractMaybeWithUpstream<T, R> extends Maybe<R> implements HasUpstreamMaybeSource<T> {

    protected final MaybeSource<T> source;

    /** @param source 上游 MaybeSource */
    AbstractMaybeWithUpstream(MaybeSource<T> source) {
        this.source = source;
    }

    /** 返回上游 MaybeSource。 */
    @Override
    public final MaybeSource<T> source() {
        return source;
    }
}
