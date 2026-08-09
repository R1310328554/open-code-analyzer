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
package org.redisson.api.map.event;

import org.redisson.api.RMapCache;

/**
 * Map 条目变更事件的数据载体，供各类 {@link MapEntryListener} 回调使用。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class EntryEvent<K, V> {

    /** 条目事件类型：创建、更新、移除或过期。 */
    public enum Type {CREATED, UPDATED, REMOVED, EXPIRED}
    
    private RMapCache<K, V> source;
    private Type type;
    private K key;
    private V value;
    private V oldValue;

    /** 无参构造，供序列化框架使用。 */
    public EntryEvent() {
    }

    /** @param source 触发事件的 {@link RMapCache} 实例
     *  @param type 事件类型
     *  @param key 条目键
     *  @param value 当前值
     *  @param oldValue 变更前的旧值（创建事件时可为 null） */
    public EntryEvent(RMapCache<K, V> source, Type type, K key, V value, V oldValue) {
        super();
        this.source = source;
        this.type = type;
        this.key = key;
        this.value = value;
        this.oldValue = oldValue;
    }
    
    /** @return 事件来源 Map 对象 */
    public RMapCache<K, V> getSource() {
        return source;
    }

    /** @return 事件类型 */
    public Type getType() {
        return type;
    }
    
    /** @return 条目键 */
    public K getKey() {
        return key;
    }
    
    /** @return 变更前的旧值 */
    public V getOldValue() {
        return oldValue;
    }
    
    /** @return 当前值 */
    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "EntryEvent{" +
                "type=" + type +
                ", key=" + key +
                ", value=" + value +
                ", oldValue=" + oldValue +
                '}';
    }
}
