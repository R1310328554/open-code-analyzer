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
package org.redisson.api.map;

import org.redisson.api.RMap;

/**
 * Map 加载器，用于读穿透（read-through）或在执行 {@link RMap#loadAll} 时从外部数据源加载数据。
 * 
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface MapLoader<K, V> {

    /**
     * 按键从外部数据源加载 Map 条目值。
     * 
     * @param key - map key
     * @return 对应的值；若不存在则返回 {@code null}
     */
    V load(K key);
    
    /**
     * 加载全部键集合。
     * 
     * @return 可迭代的键集合；当键数量过大无法一次性装入内存时尤为有用
     */
    Iterable<K> loadAllKeys();
    
}
