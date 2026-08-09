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

import javax.cache.processor.MutableEntry;

/**
 * {@link EntryProcessor} 执行期间的可变 Entry 视图。
 * <p>
 * 通过 {@link Action} 记录读/写/删/加载等语义，供处理器结束后统一提交变更。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key
 * @param <V> value
 */
public class JMutableEntry<K, V> implements MutableEntry<K, V> {

    /** EntryProcessor 对 entry 的操作类型，用于提交阶段决策。 */
    public enum Action {CREATED, READ, UPDATED, DELETED, LOADED, SKIPPED}
    
    /** 所属 JCache，用于懒加载与存在性判断。 */
    private final JCache<K, V> jCache;
    /** Entry 键。 */
    private final K key;
    /** 是否在 getValue 时触发 read-through 加载。 */
    private boolean isReadThrough;

    /** 当前累积的操作类型，默认 SKIPPED 表示尚未触达。 */
    private Action action = Action.SKIPPED;
    /** 当前值缓存；remove 后置 null。 */
    private V value;
    /** 是否已从 Cache 读取过 value（避免重复加锁读）。 */
    private boolean isValueRead;
    
    /** 构造可变 Entry；初始 value 可为 null。 */
    public JMutableEntry(JCache<K, V> jCache, K key, V value, boolean isReadThrough) {
        super();
        this.jCache = jCache;
        this.key = key;
        this.value = value;
        this.isReadThrough = isReadThrough;
    }

    @Override
    public K getKey() {
        return key;
    }

    /** 返回当前缓存的 value 字段（不触发 read-through）。 */
    public V value() {
        return value;
    }
    
    /**
     * 懒加载：必要时从 Cache 加锁读取或 read-through 加载，
     * 并设置 action 为 READ 或 LOADED。
     */
    @Override
    public V getValue() {
        if (action != Action.SKIPPED) {
            return value;
        }
        
        if (!isValueRead) {
            value = jCache.getValueLocked(key);
            isValueRead = true;
        }
        
        if (value != null) {
            action = Action.READ;
        } else if (isReadThrough) {
            value = jCache.loadValue(key);
            if (value != null) {
                action = Action.LOADED;
            }
            isReadThrough = false;
        }
        return value;
    }
    
    @Override
    public <T> T unwrap(Class<T> clazz) {
        return (T) this;
    }

    /** 等价于 getValue() != null。 */
    @Override
    public boolean exists() {
        return getValue() != null;
    }

    /** 标记 DELETED（CREATED 时回退为 SKIPPED）并清空 value。 */
    @Override
    public void remove() {
        if (action == Action.CREATED) {
            action = Action.SKIPPED;
        } else {
            action = Action.DELETED;
        }
        value = null;
    }

    /** 设置新值并标记 CREATED 或 UPDATED。 */
    @Override
    public void setValue(V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        
        if (action != Action.CREATED) {
            if (jCache.containsKey(key)) {
                action = Action.UPDATED;
            } else {
                action = Action.CREATED;
            }
        }
        this.value = value;
    }
    
    /** 返回处理器执行后的操作类型。 */
    public Action getAction() {
        return action;
    }

}
