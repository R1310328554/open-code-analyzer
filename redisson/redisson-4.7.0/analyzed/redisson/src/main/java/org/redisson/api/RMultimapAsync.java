/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api;

import java.util.Collection;
import java.util.Set;

/**
 * Multimap 基础异步 API，一个键可映射多个值。
 * <p>各方法返回 {@link RFuture}；基于 Redis Hash 结构。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */

public interface RMultimapAsync<K, V> extends RExpirableAsync {

    /**
     * 返回 multimap 中键值对总数。
     *
     * @return multimap 大小
     */
    RFuture<Integer> sizeAsync();

    /**
     * 若 multimap 中存在键 {@code key} 的至少一个键值对则返回 {@code true}。
     * 
     * @param key 映射键
     * @return 包含该键时为 {@code true}
     */
    RFuture<Boolean> containsKeyAsync(Object key);

    /**
     * 若 multimap 中存在值 {@code value} 的至少一个键值对则返回 {@code true}。
     * 
     * @param value 映射值
     * @return 包含该值时为 {@code true}
     */
    RFuture<Boolean> containsValueAsync(Object value);

    /**
     * 若 multimap 中存在键 {@code key} 且值 {@code value} 的键值对则返回 {@code true}。
     * 
     * @param key 映射键
     * @param value 映射值
     * @return 包含该条目时为 {@code true}
     */
    RFuture<Boolean> containsEntryAsync(Object key, Object value);

    /**
     * 向 multimap 存入一个键值对。
     *
     * <p>部分实现允许重复键值对，此时 {@code put} 总是新增并令大小加 1；
     * 其他实现禁止重复，已存在的键值对再次写入无效。
     *
     * @param key 映射键
     * @param value 映射值
     * @return 若 multimap 大小增加则为 {@code true}；
     *     若已存在且不允许重复则为 {@code false}
     */
    RFuture<Boolean> putAsync(K key, V value);

    /**
     * 移除键 {@code key} 且值 {@code value} 的一个键值对（若存在）。
     * 若存在多个匹配项，移除哪一个未定义。
     *
     * @param key 映射键
     * @param value 映射值
     * @return multimap 发生变化时为 {@code true}
     */
    RFuture<Boolean> removeAsync(Object key, Object value);

    // Bulk Operations

    /**
     * 将 {@code values} 中每个值以同一键 {@code key} 写入 multimap。
     * 等价于循环调用 {@code put(key, value)}，但通常更高效。
     *
     * <p>若 {@code values} 为空则为空操作。
     *
     * @param key 映射键
     * @param values 映射值集合
     * @return multimap 发生变化时为 {@code true}
     */
    RFuture<Boolean> putAllAsync(K key, Iterable<? extends V> values);

    /**
     * 用 {@code values} 替换指定键的全部已有值。
     *
     * <p>若 {@code values} 为空，等价于 {@link #removeAllAsync(Object)}。
     *
     * @param key 映射键
     * @param values 新值集合
     * @return 被替换的旧值集合；修改返回集合不影响 multimap。
     */
    RFuture<Collection<V>> replaceValuesAsync(K key, Iterable<? extends V> values);

    /**
     * 用 {@code values} 替换指定键的全部已有值（快速版，不返回旧值）。
     * 比 {@link #replaceValuesAsync(Object, Iterable)} 更快。
     *
     * <p>若 {@code values} 为空，等价于 {@link #removeAllAsync(Object)}。
     *
     * @param key 映射键
     * @param values 新值集合
     */
    RFuture<Void> fastReplaceValuesAsync(K key, Iterable<? extends V> values);

    /**
     * 移除与 {@code key} 关联的全部值。
     *
     * <p>方法返回后 {@code key} 不再映射任何值。
     *
     * @param key 映射键
     * @return 被移除的值集合（可能为空）；修改返回集合不影响 multimap。
     */
    RFuture<Collection<V>> removeAllAsync(Object key);

    /** 异步一次性返回指定键的全部元素（结果不与底层 map 绑定）。 */
    RFuture<Collection<V>> getAllAsync(K key);

    /**
     * 返回 multimap 中不重复键的数量。
     *
     * @return 键数量
     */
    RFuture<Integer> keySizeAsync();

    /**
     * 一次操作移除多个键及其全部关联值。
     *
     * 比 {@code RMultimap.remove} 更快，但不返回被移除的值。
     *
     * @param keys 待移除的映射键
     * @return 实际从 hash 中移除的键数量（不含不存在的键）
     */
    RFuture<Long> fastRemoveAsync(K... keys);

    /**
     * 一次操作从 multimap 中移除多个值。
     *
     * @param values 待移除的映射值
     * @return 实际移除的值数量
     */
    RFuture<Long> fastRemoveValueAsync(V... values);

    /**
     * 一次性读取全部键。
     *
     * @return 键集合
     */
    RFuture<Set<K>> readAllKeySetAsync();

}
