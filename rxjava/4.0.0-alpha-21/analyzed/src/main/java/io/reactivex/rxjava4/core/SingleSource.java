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
 * 表示可通过 {@link SingleObserver} 消费的基本 {@link Single} 源基础接口。
 * <p>
 * 本接口也是通过 {@link Single#create(SingleOnSubscribe)} 包装为 Single 的自定义算子的基础类型。
 *
 * @param <T> 元素类型
 * @since 2.0
 */
@FunctionalInterface
public interface SingleSource<@NonNull T> {

    /**
     * 将给定 {@link SingleObserver} 订阅到此 {@link SingleSource} 实例。
     * @param observer {@code SingleObserver}，不可为 {@code null}
     * @throws NullPointerException 若 {@code observer} 为 {@code null}
     */
    void subscribe(@NonNull SingleObserver<? super T> observer);
}
