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
 * 将下游 {@link Observer} 映射/包装为上游 {@code Observer} 的接口。
 *
 * @param <Downstream> 下游值类型
 * @param <Upstream> 上游值类型
 */
@FunctionalInterface
public interface ObservableOperator<@NonNull Downstream, @NonNull Upstream> {
    /**
     * 对子 {@link Observer} 应用函数并返回新的父 {@code Observer}。
     * @param observer 子 {@code Observer} 实例
     * @return 父 {@code Observer} 实例
     * @throws Throwable 失败时抛出
     */
    @NonNull
    Observer<? super Upstream> apply(@NonNull Observer<? super Downstream> observer) throws Throwable;
}
