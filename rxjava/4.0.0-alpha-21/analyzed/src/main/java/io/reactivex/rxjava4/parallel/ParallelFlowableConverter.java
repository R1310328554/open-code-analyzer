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
 * {@link ParallelFlowable#to} 的回调：将 ParallelFlowable 转换为任意类型 R。
 * <p>History: 2.1.7 - experimental
 *
 * @param <T> 上游元素类型
 * @param <R> 转换结果类型
 * @since 2.2
 */
@FunctionalInterface
public interface ParallelFlowableConverter<T, R> {
    /**
     * 对 upstream 应用转换逻辑并返回 R。
     * @param upstream 上游 ParallelFlowable
     * @return 转换结果
     */
    @NonNull
    R apply(@NonNull ParallelFlowable<T> upstream);
}
