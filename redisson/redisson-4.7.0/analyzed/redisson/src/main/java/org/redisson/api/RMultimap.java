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
import java.util.Map;
import java.util.Set;

/**
 * Multimap 基础接口，允许一个键映射多个值。
 * <p>基于 Redis Hash 结构，键对应一组值集合；
 * 具体实现可为 ListMultimap 或 SetMultimap。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RMultimap<K, V> extends RExpirable, RMultimapAsync<K, V> {

    /**
     * 返回与 {@code key} 关联的 {@link RCountDownLatch} 实例。
     * 
     * @param key 映射键
     * @return 倒计时门闩
     */
    RCountDownLatch getCountDownLatch(K key);
    
    /**
     * 返回与 {@code key} 关联的 {@link RPermitExpirableSemaphore} 实例。
     * 
     * @param key 映射键
     * @return 可过期许可信号量
     */
    RPermitExpirableSemaphore getPermitExpirableSemaphore(K key);

    /**
     * 返回与 {@code key} 关联的 {@link RSemaphore} 实例。
     * 
     * @param key 映射键
     * @return 信号量
     */
    RSemaphore getSemaphore(K key);
    
    /**
     * 返回与 {@code key} 关联的公平 {@link RLock} 实例。
     * 
     * @param key 映射键
     * @return 公平锁
     */
    RLock getFairLock(K key);

    /**
     * 返回与 {@code key} 关联的 {@link RReadWriteLock} 实例。
     * 
     * @param key 映射键
     * @return 读写锁
     */
    RReadWriteLock getReadWriteLock(K key);
    
    /**
     * 返回与 {@code key} 关联的 {@link RLock} 实例。
     * 
     * @param key 映射键
     * @return 分布式锁
     */
    RLock getLock(K key);
    
    /**
     * 返回 multimap 中键值对总数。
     *
     * @return multimap 大小
     */
    int size();

    /**
     * 检查 multimap 是否为空。
     *
     * @return 为空时返回 {@code true}
     */
    boolean isEmpty();

    /**
     * 若 multimap 中存在键 {@code key} 的至少一个键值对则返回 {@code true}。
     * 
     * @param key 映射键
     * @return 包含该键时为 {@code true}
     */
    boolean containsKey(Object key);

    /**
     * 若 multimap 中存在值 {@code value} 的至少一个键值对则返回 {@code true}。
     * 
     * @param value 映射值
     * @return 包含该值时为 {@code true}
     */
    boolean containsValue(Object value);

    /**
     * 若 multimap 中存在键 {@code key} 且值 {@code value} 的键值对则返回 {@code true}。
     * 
     * @param key 映射键
     * @param value 映射值
     * @return 包含该条目时为 {@code true}
     */
    boolean containsEntry(Object key, Object value);

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
    boolean put(K key, V value);

    /**
     * 移除键 {@code key} 且值 {@code value} 的一个键值对（若存在）。
     * 若存在多个匹配项，移除哪一个未定义。
     *
     * @param key 映射键
     * @param value 映射值
     * @return multimap 发生变化时为 {@code true}
     */
    boolean remove(Object key, Object value);

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
    boolean putAll(K key, Iterable<? extends V> values);

    /**
     * 用 {@code values} 替换指定键的全部已有值。
     *
     * <p>若 {@code values} 为空，等价于 {@link #removeAll(Object) removeAll(key)}。
     *
     * @param key 映射键
     * @param values 新值集合
     * @return 被替换的旧值集合；修改返回集合不影响 multimap。
     */
    Collection<V> replaceValues(K key, Iterable<? extends V> values);

    /**
     * 用 {@code values} 替换指定键的全部已有值（快速版，不返回旧值）。
     * 比 {@link #replaceValues} 更快，但不返回被替换的值。
     *
     * <p>若 {@code values} 为空，等价于 {@link #removeAll(Object) removeAll(key)}。
     *
     * @param key 映射键
     * @param values 新值集合
     */
    void fastReplaceValues(K key, Iterable<? extends V> values);

    /**
     * 移除与 {@code key} 关联的全部值。
     *
     * <p>方法返回后 {@code key} 不再映射任何值。
     * <p>若不需要返回值，可使用 {@link RMultimap#fastRemove}。</p>
     * 
     * @param key 映射键
     * @return 被移除的值集合（可能为空）；修改返回集合不影响 multimap。
     */
    Collection<V> removeAll(Object key);

    /**
     * 清空 multimap 的全部键值对，使其 {@linkplain #isEmpty 为空}。
     */
    void clear();

    /**
     * 返回与 {@code key} 关联的值集合视图。
     * 当 {@code containsKey(key)} 为 false 时返回空集合而非 {@code null}。
     *
     * <p>对返回集合的修改会反映到底层 multimap，反之亦然。
     * 
     * @param key 映射键
     * @return 值集合
     */
    Collection<V> get(K key);

    /**
     * 一次性返回指定键的全部元素。
     * 结果集合<b>不</b>与底层 map 绑定，修改结果不会影响 multimap。
     *
     * @param key 映射键
     * @return 值集合
     */
    Collection<V> getAll(K key);

    /**
     * 返回 multimap 中全部不重复键的集合视图。
     * 仅当键至少映射一个值时才会出现在键集中。
     *
     * <p>对返回集合的修改会反映到底层 multimap，反之亦然；
     * 但不支持向返回集合添加键。
     * 
     * @return 键集合
     */
    Set<K> keySet();

    /**
     * 返回 multimap 中全部不重复键的集合视图（分批加载）。
     * {@code count} 定义每批键数量；较大值可减少大 multimap 迭代时的 HSCAN 往返次数。
     *
     * <p>对返回集合的修改会反映到底层 multimap，反之亦然；
     * 但不支持向返回集合添加键。
     *
     * @param count 每批键数量
     * @return 键集合
     */
    default Set<K> keySet(int count) {
        return keySet();
    }

    /**
     * 返回 multimap 中不重复键的数量。
     *  
     * @return 键数量
     */
    int keySize();

    /**
     * 返回 multimap 中每个键值对的值集合视图（不合并重复）。
     * 因此 {@code values().size() == size()}。
     *
     * <p>对返回集合的修改会反映到底层 multimap，反之亦然；
     * 但不支持向返回集合添加值。
     * 
     * @return 值集合
     */
    Collection<V> values();

    /**
     * 返回 multimap 中每个键值对的值集合视图（分批加载，不合并重复）。
     * {@code count} 定义每批数量；较大值可减少 HSCAN/SSCAN 往返次数。
     *
     * @param count 每批迭代数量
     * @return 值集合
     */
    default Collection<V> values(int count) {
        return values();
    }

    /**
     * 返回 multimap 中全部键值对的集合视图（{@link Map.Entry} 形式）。
     *
     * <p>对返回集合或其条目的修改会反映到底层 multimap，反之亦然；
     * 但不支持向返回集合添加条目。
     * 
     * @return 条目集合
     */
    Collection<Map.Entry<K, V>> entries();

    /**
     * 返回 multimap 中全部键值对的集合视图（分批加载，{@link Map.Entry} 形式）。
     * {@code count} 定义每批数量；较大值可减少 HSCAN/SSCAN 往返次数。
     *
     * @param count 每批迭代数量
     * @return 条目集合
     */
    default Collection<Map.Entry<K, V>> entries(int count) {
        return entries();
    }

    /**
     * 一次操作移除多个键及其全部关联值。
     *
     * 比 {@code RMultimap.remove} 更快，但不返回被移除的值。
     *
     * @param keys 待移除的映射键
     * @return 实际从 hash 中移除的键数量（不含不存在的键）
     */
    long fastRemove(K... keys);

    /**
     * 一次操作从 multimap 中移除多个值。
     *
     * @param values 待移除的映射值
     * @return 实际移除的值数量
     */
    long fastRemoveValue(V... values);

    /**
     * 一次性读取全部键。
     *
     * @return 键集合
     */
    Set<K> readAllKeySet();

}
