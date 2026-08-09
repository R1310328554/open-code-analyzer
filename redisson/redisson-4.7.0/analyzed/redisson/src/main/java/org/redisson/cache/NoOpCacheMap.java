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
package org.redisson.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * 无操作本地缓存映射。
 * <p>
 * 当 {@link org.redisson.api.LocalCachedMapOptions#getCacheSize()} 为 {@code -1} 时使用，
 * 始终不存储任何数据。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class NoOpCacheMap<K, V> implements ConcurrentMap<K, V> {

    /** {@inheritDoc} 始终返回 0。 */
    @Override
    public int size() {
        return 0;
    }

    /** {@inheritDoc} 始终返回 {@code false}（与空缓存语义不同）。 */
    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    /** {@inheritDoc} 始终返回 {@code null}，不缓存任何值。 */
    @Override
    public V get(Object key) {
        return null;
    }

    /** {@inheritDoc} 忽略写入，始终返回 {@code null}。 */
    @Override
    public V put(K key, V value) {
        return null;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection<V> values() {
        return Collections.emptyList();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }
    @Override
    public V putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return false;
    }

    @Override
    public V replace(K key, V value) {
        return null;
    }
}
