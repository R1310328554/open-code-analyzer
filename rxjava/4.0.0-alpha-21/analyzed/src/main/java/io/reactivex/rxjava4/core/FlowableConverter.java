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

package io.reactivex.rxjava4.core;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * {@link Flowable#to} 算子使用的便捷接口与回调，用于将 {@link Flowable} 流畅地转换为其他值。
 * <p>History: 2.1.7 - experimental
 * @param <T> 上游类型
 * @param <R> 输出类型
 * @since 2.2
 */
@FunctionalInterface
public interface FlowableConverter<@NonNull T, @NonNull R> {
    /**
     * 对上游 {@link Flowable} 应用函数并返回类型为 {@code R} 的转换值。
     *
     * @param upstream 上游 {@code Flowable} 实例
     * @return 转换后的值
     */
    R apply(@NonNull Flowable<T> upstream);
}
