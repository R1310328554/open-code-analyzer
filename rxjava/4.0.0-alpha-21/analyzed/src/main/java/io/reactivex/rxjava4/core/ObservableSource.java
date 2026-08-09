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
 * 表示可通过 {@link Observer} 消费的基本、无背压 {@link Observable} 源基础接口。
 *
 * @param <T> 元素类型
 * @since 2.0
 */
@FunctionalInterface
public interface ObservableSource<@NonNull T> {

    /**
     * 将给定 {@link Observer} 订阅到此 {@link ObservableSource} 实例。
     * @param observer {@code Observer}，不可为 {@code null}
     * @throws NullPointerException 若 {@code observer} 为 {@code null}
     */
    void subscribe(@NonNull Observer<? super T> observer);
}
