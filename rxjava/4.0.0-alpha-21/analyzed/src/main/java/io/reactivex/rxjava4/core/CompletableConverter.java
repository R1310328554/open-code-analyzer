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
 * 便捷接口与回调，供 {@link Completable#to} 算子将 {@link Completable} 流畅地转换为其他类型。
 * <p>历史：2.1.7 — 实验性
 * @param <R> 输出类型
 * @since 2.2
 */
@FunctionalInterface
public interface CompletableConverter<@NonNull R> {
    /**
     * 对上游 {@link Completable} 应用函数，返回类型为 {@code R} 的转换结果。
     *
     * @param upstream 上游 {@link Completable} 实例
     * @return 转换后的值
     */
    R apply(@NonNull Completable upstream);
}
