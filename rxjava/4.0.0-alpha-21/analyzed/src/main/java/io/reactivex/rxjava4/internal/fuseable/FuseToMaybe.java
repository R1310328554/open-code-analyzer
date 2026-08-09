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

package io.reactivex.rxjava4.internal.fuseable;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Maybe;

/**
 * 表示算子实现可在从 Maybe 转为其他响应式类型后再次调用 toMaybe 时
 * 宏融合回 Maybe 的接口：
 * <pre>
 * {@code
 * Single<Integer> single = Maybe.just(1).isEmpty();
 * Maybe<Integer> maybe = single.toMaybe();
 * }
 * </pre>
 *
 * {@code Single.toMaybe()} 会检查本接口并调用 {@link #fuseToMaybe()}，
 * 返回可能是 Maybe 专用 isEmpty() 实现的 Maybe。
 * <p>
 * 组装时略有开销（1 次 instanceof 检查、1 次算子分配、丢弃 1 个算子），
 * 但运行时不会产生转换开销。
 *
 * @param <T> 值类型
 */
public interface FuseToMaybe<@NonNull T> {

    /**
     * 返回算子对应的（直接）Maybe。
     * <p>实现应处理必要的 RxJavaPlugins 包装。
     * @return Maybe 实例
     */
    @NonNull
    Maybe<T> fuseToMaybe();
}
