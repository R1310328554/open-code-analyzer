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
import io.reactivex.rxjava4.internal.fuseable.HasUpstreamPublisher;

import java.util.Objects;

/**
 * 接受上游 {@link Publisher} 作为源的 Flowable 算子抽象基类。
 *
 * @param <T> 上游值类型
 * @param <R> 输出值类型
 */
abstract class AbstractFlowableWithUpstream<T, R> extends Flowable<R> implements HasUpstreamPublisher<T> {

    /** 上游 Publisher 源。 */
    protected final Flowable<T> source;

    /**
     * 包装已校验非 null 的上游 Publisher。
     * @param source 上游 Publisher 实例，非 null（已校验）
     */
    AbstractFlowableWithUpstream(Flowable<T> source) {
        this.source = Objects.requireNonNull(source, "source is null");
    }

    /** 返回上游 Publisher。 */
    @Override
    public final Publisher<T> source() {
        return source;
    }
}
