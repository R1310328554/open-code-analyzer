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

import io.reactivex.rxjava4.annotations.Nullable;

/**
 * 按 key 分组的 {@link Observable}，可通过 {@link #getKey()} 获取 key 值。
 * <p>
 * <em>注意：</em>{@link GroupedObservable} 会缓存待发射的元素，直至被订阅。
 * 因此为避免内存泄漏，不应简单忽略不关心的 {@code GroupedObservable}。
 * 可对其应用如 {@link Observable#take take}{@code (0)} 等算子，通知其可丢弃缓冲区。
 *
 * @param <K>
 *            key 的类型
 * @param <T>
 *            {@code GroupedObservable} 发射元素的类型
 * @see Observable#groupBy(io.reactivex.rxjava4.functions.Function)
 * @see <a href="http://reactivex.io/documentation/operators/groupby.html">ReactiveX documentation: GroupBy</a>
 */
public abstract class GroupedObservable<K, T> extends Observable<T> {

    final K key;

    /**
     * 使用给定 key 构造 GroupedObservable。
     * @param key key
     */
    protected GroupedObservable(@Nullable K key) {
        this.key = key;
    }

    /**
     * 返回标识本 {@code GroupedObservable} 所发射元素分组的 key。
     *
     * @return 本 {@code GroupedObservable} 发射元素所依据的分组 key
     */
    @Nullable
    public K getKey() {
        return key;
    }
}
