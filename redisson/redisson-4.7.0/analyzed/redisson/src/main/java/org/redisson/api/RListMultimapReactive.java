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

import java.util.List;

import reactor.core.publisher.Mono;

/**
 * 基于 List 的 Multimap Reactor API。
 * <p>保持插入顺序，允许同一键下值重复；各方法返回 {@link Mono}。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RListMultimapReactive<K, V> extends RMultimapReactive<K, V> {

    /**
     * 返回与 {@code key} 关联的值列表视图。
     * 当 {@code containsKey(key)} 为 false 时返回空集合而非 {@code null}。
     *
     * <p>对返回集合的修改会反映到底层 multimap，反之亦然。
     * 
     * @param key 映射键
     * @return 值列表
     */
    RListReactive<V> get(K key);
 
    /**
     * 一次性返回指定键的全部元素。
     * 结果列表<b>不</b>与底层 map 绑定，修改结果不会影响 multimap。
     *
     * @param key 映射键
     * @return 值列表
     */
    Mono<List<V>> getAll(K key);
    
    /**
     * 移除与 {@code key} 关联的全部值。
     *
     * <p>方法返回后 {@code key} 不再映射任何值。
     * <p>若不需要返回值，可使用 {@link RMultimapReactive#fastRemove}。</p>
     * 
     * @param key 映射键
     * @return 被移除的值列表（可能为空）；修改返回列表不影响 multimap。
     */
    Mono<List<V>> removeAll(Object key);
    
    /**
     * 用 {@code values} 替换指定键的全部已有值。
     *
     * <p>若 {@code values} 为空，等价于 {@link #removeAll(Object)}。
     *
     * @param key 映射键
     * @param values 新值集合
     * @return 被替换的旧值列表；修改返回列表不影响 multimap。
     */
    Mono<List<V>> replaceValues(K key, Iterable<? extends V> values);

    /**
     * 注册 ListMultimap 对象事件监听器。
     *
     * @see org.redisson.api.listener.MapPutListener
     * @see org.redisson.api.listener.MapRemoveListener
     * @see org.redisson.api.listener.ListAddListener
     * @see org.redisson.api.listener.ListRemoveListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    @Override
    Mono<Integer> addListener(ObjectListener listener);

}
