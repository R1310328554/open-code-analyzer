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
 * 组合 {@link Maybe} 的接口。
 *
 * @param <Upstream> 上游值类型
 * @param <Downstream> 下游值类型
 */
@FunctionalInterface
public interface MaybeTransformer<@NonNull Upstream, @NonNull Downstream> {
    /**
     * 对上游 {@link Maybe} 应用函数并返回元素类型可能不同的 {@link MaybeSource}。
     * @param upstream 上游 {@code Maybe} 实例
     * @return 转换后的 {@code MaybeSource} 实例
     */
    @NonNull
    MaybeSource<Downstream> apply(@NonNull Maybe<Upstream> upstream);
}
