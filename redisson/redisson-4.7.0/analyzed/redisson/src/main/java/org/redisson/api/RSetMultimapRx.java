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

import java.util.Set;

import io.reactivex.rxjava3.core.Single;

/**
 * 基于 Set 的 Multimap RxJava3 API；同一键下值不可重复。
 * 
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 元素类型
 */
public interface RSetMultimapRx<K, V> extends RMultimapRx<K, V> {

    /**
     * 返回与 {@code key} 关联的值集合视图；{@code containsKey(key)} 为 false 时
     * 返回空集合而非 {@code null}。
     *
     * <p>对返回集合的修改会同步到底层 multimap，反之亦然。
     * 
     * @param key 映射键
     * @return 值集合
     */
    RSetRx<V> get(K key);
 
    /**
     * 一次性返回全部元素；结果 Set 不关联底层 map，修改不会反映到 map。
     *
     * @param key 映射键
     * @return 值集合 
     */
    Single<Set<V>> getAll(K key);
    
    /**
     * 移除与 {@code key} 关联的全部值；返回后 {@code key} 不再映射任何值。
     * <p>若不需要返回值，请使用 {@link RMultimapReactive#fastRemove}。
     * 
     * @param key 映射键
     * @return 被移除的值集合（可能为空）；修改返回值不影响 multimap
     */
    Single<Set<V>> removeAll(Object key);
    
    /**
     * 为指定键存储一组值，替换该键下已有值。
     *
     * <p>{@code values} 为空时等价于 {@link #removeAll(Object)}。
     *
     * @param key 映射键
     * @param values 映射值集合
     * @return 被替换的旧值集合；键无旧值时返回空集合
     */
    Single<Set<V>> replaceValues(K key, Iterable<? extends V> values);

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.MapPutListener
     * @see org.redisson.api.listener.MapRemoveListener
     * @see org.redisson.api.listener.SetAddListener
     * @see org.redisson.api.listener.SetRemoveListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    Single<Integer> addListener(ObjectListener listener);

}
