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

/**
 * JSR-107 {@link Cache.Entry} 的简单不可变实现。
 * <p>
 * 用于迭代器与批量操作返回键值对快照。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key
 * @param <V> value
 */
public class JCacheEntry<K, V> implements Cache.Entry<K, V> {

    /** 缓存键。 */
    private final K key;
    /** 缓存值。 */
    private final V value;
    
    /** 构造只读 Entry 快照。 */
    public JCacheEntry(K key, V value) {
        super();
        this.key = key;
        this.value = value;
    }

    /** 返回键。 */
    @Override
    public K getKey() {
        return key;
    }

    /** 返回值。 */
    @Override
    public V getValue() {
        return value;
    }

    /** 若 clazz 可赋值给本类则 cast，否则返回 null。 */
    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(getClass())) {
            return clazz.cast(this);
        }

        return null;
    }

}
