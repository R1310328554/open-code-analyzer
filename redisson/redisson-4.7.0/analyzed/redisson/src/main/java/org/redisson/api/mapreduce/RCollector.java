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
package org.redisson.api.mapreduce;

/**
 * MapReduce Map 阶段用于暂存键值对的收集器，
 * 其输出供 Reduce 阶段按键分组归约。
 * 
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RCollector<K, V> {

    /**
     * 写入一条待归约的键值映射。
     * 
     * @param key Reduce 阶段可用的键
     * @param value Reduce 阶段可用的值
     */
    void emit(K key, V value);
    
}
