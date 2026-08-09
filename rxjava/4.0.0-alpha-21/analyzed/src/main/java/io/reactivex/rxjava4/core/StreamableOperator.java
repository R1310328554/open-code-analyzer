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
import io.reactivex.rxjava4.disposables.StreamerCancellation;

/**
 * 将上游 {@link Streamer} 映射/包装为下游 {@code Streamer} 的接口。
 *
 * @param <T> 上游值类型
 * @param <R> 下游值类型
 * @since 4.0.0
 */
@FunctionalInterface
public interface StreamableOperator<@NonNull T, @NonNull R> {
    /**
     * 对上游 {@link Streamer} 应用函数并返回新的下游 {@code Streamer}。
     * @param container 处理下游取消传播的 {@link StreamerCancellation}
     * @param streamer 上游 {@code Streamer} 实例
     * @return 下游 {@code Streamer} 实例
     * @throws Throwable 失败时抛出
     */
    @NonNull
    Streamer<? extends R> apply(@NonNull StreamerCancellation container,
            @NonNull Streamer<? extends T> streamer) throws Throwable;
}
