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
 * 表示可通过 {@link MaybeObserver} 消费的基本 {@link Maybe} 源基础接口。
 * <p>
 * 本接口也是通过 {@link Maybe#create(MaybeOnSubscribe)} 包装为 Maybe 的
 * 自定义算子的基础类型。
 *
 * @param <T> 元素类型
 * @since 2.0
 */
@FunctionalInterface
public interface MaybeSource<@NonNull T> {

    /**
     * 将给定 {@link MaybeObserver} 订阅到此 {@link MaybeSource} 实例。
     * @param observer {@code MaybeObserver}，不可为 {@code null}
     * @throws NullPointerException 若 {@code observer} 为 {@code null}
     */
    void subscribe(@NonNull MaybeObserver<? super T> observer);
}
