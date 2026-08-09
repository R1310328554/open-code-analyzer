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
package org.redisson;

import java.util.Map.Entry;

/**
 * 不可变 {@link java.util.Map.Entry} 实现，用于 Redisson Map 扫描/迭代结果。
 * <p>{@link #setValue} 不支持修改。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class RedissonMapEntry<K, V> implements Entry<K, V> {

    private final K key;
    private final V value;
    
    /** @param key 映射键
     *  @param value 映射值 */
    public RedissonMapEntry(K key, V value) {
        super();
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    /** 不支持修改；始终抛出 {@link UnsupportedOperationException}。 */
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

}
