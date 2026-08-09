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
 * compose 算子使用的便捷接口与回调，用于将 {@link Completable} 流畅地转换为另一个 {@code Completable}。
 */
@FunctionalInterface
public interface CompletableTransformer {
    /**
     * 对上游 {@link Completable} 应用函数并返回 {@link CompletableSource}。
     * @param upstream 上游 {@code Completable} 实例
     * @return 转换后的 {@code CompletableSource} 实例
     */
    @NonNull
    CompletableSource apply(@NonNull Completable upstream);
}
