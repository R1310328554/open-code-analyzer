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

import java.util.Map;
import java.util.Set;

/**
 * 基于 Set 的 Multimap；同一键下值不可重复。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RSetMultimap<K, V> extends RMultimap<K, V> {

    /**
     * {@inheritDoc}
     *
     * <p>因 {@code RSetMultimap} 同一键下值唯一，本方法返回 {@link Set} 而非 {@link RMultimap} 接口声明的 {@link java.util.Collection}。
     */
    @Override
    RSet<V> get(K key);

    /**
     * {@inheritDoc}
     *
     * <p>因 {@code RSetMultimap} 同一键下值唯一，本方法返回 {@link Set} 而非 {@link RMultimap} 接口声明的 {@link java.util.Collection}。
     */
    Set<V> getAll(K key);

    /**
     * {@inheritDoc}
     *
     * <p>因 {@code RSetMultimap} 同一键下值唯一，本方法返回 {@link Set} 而非 {@link RMultimap} 接口声明的 {@link java.util.Collection}。
     */
    @Override
    Set<V> removeAll(Object key);

    /**
     * {@inheritDoc}
     *
     * <p>因 {@code RSetMultimap} 同一键下值唯一，本方法返回 {@link Set} 而非 {@link RMultimap} 接口声明的 {@link java.util.Collection}。
     *
     * <p>{@code values} 中的重复值在 multimap 中仅保留一份。
     */
    @Override
    Set<V> replaceValues(K key, Iterable<? extends V> values);

    /**
     * {@inheritDoc}
     *
     * <p>因 {@code RSetMultimap} 同一键下值唯一，本方法返回 {@link Set} 而非 {@link RMultimap} 接口声明的 {@link java.util.Collection}。
     */
    @Override
    Set<Map.Entry<K, V>> entries();

    /**
     * {@inheritDoc}
     *
     * <p>因 {@code RSetMultimap} 同一键下值唯一，本方法返回 {@link Set} 而非 {@link RMultimap} 接口声明的 {@link java.util.Collection}。
     */
    @Override
    default Set<Map.Entry<K, V>> entries(int count) {
        return entries();
    }

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
    int addListener(ObjectListener listener);


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
    RFuture<Integer> addListenerAsync(ObjectListener listener);

}
