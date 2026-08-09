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

import org.redisson.api.AsyncIterator;
import org.redisson.api.RMap;

import java.util.concurrent.CompletionStage;

/**
 * 异步 Map 加载器，用于读穿透（read-through）或在执行 {@link RMap#loadAll} 时从外部数据源加载数据。
 * 
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface MapLoaderAsync<K, V> {

    /**
     * 异步按键从外部数据源加载 Map 条目值。
     * 
     * @param key - map key
     * @return 异步结果；若不存在则完成值为 {@code null}
     */
    CompletionStage<V> load(K key);
    
    /**
     * 异步加载全部键的迭代器。
     * 
     * @return 异步键迭代器；当键数量过大无法一次性装入内存时尤为有用
     */
    AsyncIterator<K> loadAllKeys();
    
}
