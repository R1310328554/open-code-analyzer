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

package io.reactivex.rxjava4.operators;

import io.reactivex.rxjava4.annotations.*;

/**
 * {@link SimpleQueue} 变体：{@code poll()} 不声明 throws Throwable。
 * 供 SPSC 队列等不通过融合函数抛错的实现使用。
 *
 * @param <T> offer/poll 的元素类型，非 null
 * @since 3.1.1
 */
public interface SimplePlainQueue<@NonNull T> extends SimpleQueue<T> {

    @Nullable
    @Override
    T poll();
}
