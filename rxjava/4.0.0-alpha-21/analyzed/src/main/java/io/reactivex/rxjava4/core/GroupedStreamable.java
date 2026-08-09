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

import io.reactivex.rxjava4.annotations.*;

/**
 * 按 key 分组的 {@link Streamable}，可通过 {@link #getKey()} 获取 key 值。
 * @param <K>
 *            key 的类型，可为 null
 * @param <T>
 *            {@code GroupedStreamable} 发射元素的类型
 * @see Streamable#groupBy(io.reactivex.rxjava4.functions.Function)
 * @see <a href="http://reactivex.io/documentation/operators/groupby.html">ReactiveX documentation: GroupBy</a>
 * @since 4.0.0
 */
public abstract class GroupedStreamable<@Nullable K, @NonNull T> implements Streamable<T> {

    final K key;

    /**
     * 使用给定 key 构造 GroupedStreamable。
     * @param key key
     */
    protected GroupedStreamable(@Nullable K key) {
        this.key = key;
    }

    /**
     * 返回标识本 {@code GroupedStreamable} 所发射元素分组的 key。
     *
     * @return 本 {@code GroupedStreamable} 发射元素所依据的分组 key
     */
    @Nullable
    public K getKey() {
        return key;
    }

}
