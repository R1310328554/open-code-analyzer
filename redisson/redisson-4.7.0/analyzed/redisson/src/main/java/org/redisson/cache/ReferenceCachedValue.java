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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

/**
 * 基于软/弱引用的缓存值包装。
 * <p>
 * 值对象由 GC 管理生命周期，通过 {@link Reference} 间接访问。
 */

public class ReferenceCachedValue<K, V> extends StdCachedValue<K, V> implements CachedValue<K, V> {
    
    /** 引用类型：软引用或弱引用。 */
    public enum Type {SOFT, WEAK}
    
    /** 指向实际值对象的软/弱引用。 */
    private final Reference<V> ref;

    /**
     * 创建引用型缓存值。
     *
     * @param key 键
     * @param value 值对象
     * @param ttl 存活时间（毫秒）
     * @param maxIdleTime 最大空闲时间（毫秒）
     * @param queue 引用队列
     * @param type 引用类型
     */
    public ReferenceCachedValue(K key, V value, long ttl, long maxIdleTime, ReferenceQueue<V> queue, Type type) {
        super(key, null, ttl, maxIdleTime);
        if (type == Type.SOFT) {
            this.ref = new CachedValueSoftReference<V>(this, value, queue);
        } else {
            this.ref = new CachedValueWeakReference<V>(this, value, queue);
        }
    }

    /** 通过引用获取值，并更新最后访问时间。 */
    @Override
    public V getValue() {
        super.getValue();
        return ref.get();
    }

}
