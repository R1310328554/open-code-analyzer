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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.MaybeSource;
import io.reactivex.rxjava4.functions.Function;

/**
 * 将 {@link MaybeSource} 映射为 {@link Publisher} 的辅助 {@link Function}，
 * 供 Publisher 提供的各 MaybeSource 合并/串联时使用。
 */
public enum MaybeToPublisher implements Function<MaybeSource<Object>, Publisher<Object>> {
    INSTANCE;

    /** @return 单例 Function 实例 */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> Function<MaybeSource<T>, Publisher<T>> instance() {
        return (Function)INSTANCE;
    }

    /** 将 MaybeSource 包装为 {@link MaybeToFlowable}。 */
    @Override
    public Publisher<Object> apply(MaybeSource<Object> t) {
        return new MaybeToFlowable<>(t);
    }
}
