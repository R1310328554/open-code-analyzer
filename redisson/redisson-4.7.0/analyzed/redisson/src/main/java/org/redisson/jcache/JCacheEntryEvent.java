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
package org.redisson.jcache;

import javax.cache.Cache;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;

/**
 * 传递给 JCache 监听器的事件载体，扩展 {@link CacheEntryEvent}。
 * <p>
 * 支持携带变更后的 value 与可选 oldValue（UPDATE/REMOVE 等场景）。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key
 * @param <V> value
 */
public class JCacheEntryEvent<K, V> extends CacheEntryEvent<K, V> {

    private static final long serialVersionUID = -4601376694286796662L;

    /** 事件关联的键（序列化后的 Object 存储）。 */
    private final Object key;
    /** 事件关联的新值或当前值。 */
    private final Object value;
    /** 变更前的旧值；无旧值时为 null。 */
    private final Object oldValue;

    /** 构造不含 oldValue 的缓存条目事件。 */
    public JCacheEntryEvent(Cache<K, V> source, EventType eventType, Object key, Object value) {
        super(source, eventType);
        this.key = key;
        this.value = value;
        this.oldValue = null;
    }

    /** 构造含 oldValue 的缓存条目事件（如 UPDATE）。 */
    public JCacheEntryEvent(Cache<K, V> source, EventType eventType, Object key, Object value, Object oldValue) {
        super(source, eventType);
        this.key = key;
        this.value = value;
        this.oldValue = oldValue;
    }

    /** 返回事件键。 */
    @Override
    public K getKey() {
        return (K) key;
    }

    /** 返回事件中的值。 */
    @Override
    public V getValue() {
        return (V) value;
    }

    /** 类型 unwrap，支持转为 {@link JCacheEntryEvent}。 */
    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(getClass())) {
            return clazz.cast(this);
        }

        return null;
    }

    /** 返回变更前的旧值。 */
    @Override
    public V getOldValue() {
        return (V) oldValue;
    }

    /** 是否携带有效的 oldValue。 */
    @Override
    public boolean isOldValueAvailable() {
        return oldValue != null;
    }

}
