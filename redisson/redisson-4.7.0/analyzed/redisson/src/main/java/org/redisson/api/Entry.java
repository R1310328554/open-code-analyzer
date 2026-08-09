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

/**
 * 简单的键值对容器，用于 API 层传递 {@code Map.Entry} 风格数据。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class Entry<K, V> {

    private K key;
    private V value;

    /** 无参构造，供序列化框架使用。 */
    public Entry() {

    }

    /** @param key 键
     *  @param value 值 */
    public Entry(K key, V  value) {
        this.key = key;
        this.value = value;
    }

    /** @return 值 */
    public V getValue() {
        return value;
    }

    /** @return 键 */
    public K getKey() {
        return key;
    }

}