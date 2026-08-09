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

package io.reactivex.rxjava4.parallel;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * 组合 ParallelFlowable 的函数式接口（compose 参数）。
 * <p>History: 2.0.8 - experimental
 *
 * @param <Upstream> 上游元素类型
 * @param <Downstream> 下游元素类型
 * @since 2.2
 */
@FunctionalInterface
public interface ParallelTransformer<@NonNull Upstream, @NonNull Downstream> {
    /**
     * 变换 upstream 并返回（可能换型的）ParallelFlowable。
     * @param upstream 上游实例
     * @return 变换后的 ParallelFlowable
     */
    @NonNull
    ParallelFlowable<Downstream> apply(@NonNull ParallelFlowable<Upstream> upstream);
}
