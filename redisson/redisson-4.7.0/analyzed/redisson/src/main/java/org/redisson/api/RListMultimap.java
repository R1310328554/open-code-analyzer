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

/**
 * 基于 List 的 Multimap，保持插入顺序且允许同一键下值重复。
 * <p>每个键对应一个 {@link RList}，底层由 Redis Hash + List 实现。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RListMultimap<K, V> extends RMultimap<K, V> {

    /**
     * {@inheritDoc}
     *
     * <p>因 {@code RListMultimap} 允许同一键下值重复且保持插入顺序，
     * 本方法返回 {@link List} 而非 {@link RMultimap} 接口声明的 {@link java.util.Collection}。
     */
    @Override
    RList<V> get(K key);

    /**
     * {@inheritDoc}
     *
     * <p>Because a {@code RListMultimap} may has duplicates among values mapped by key and stores insertion order
     * method returns a {@link List}, instead of the {@link java.util.Collection}
     * specified in the {@link RMultimap} interface.
     */
    List<V> getAll(K key);

    /**
     * {@inheritDoc}
     *
     * <p>Because a {@code RListMultimap} may has duplicates among values mapped by key and stores insertion order
     * method returns a {@link List}, instead of the {@link java.util.Collection}
     * specified in the {@link RMultimap} interface.
     */
    @Override
    List<V> removeAll(Object key);

    /**
     * {@inheritDoc}
     *
     * <p>因 {@code RListMultimap} 允许同一键下值重复且保持插入顺序，
     * 本方法返回 {@link List} 而非 {@link RMultimap} 接口声明的 {@link java.util.Collection}。
     *
     */
    @Override
    List<V> replaceValues(K key, Iterable<? extends V> values);

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
    int addListener(ObjectListener listener);


    /**
     * 异步注册 ListMultimap 对象事件监听器。
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
    RFuture<Integer> addListenerAsync(ObjectListener listener);

}
