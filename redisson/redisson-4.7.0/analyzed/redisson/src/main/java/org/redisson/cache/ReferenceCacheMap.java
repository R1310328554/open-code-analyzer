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

import java.lang.ref.ReferenceQueue;

import org.redisson.cache.ReferenceCachedValue.Type;

/**
 * 基于软/弱引用的本地缓存映射。
 * <p>
 * 值由 GC 回收时通过 {@link ReferenceQueue} 自动从映射中移除。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class ReferenceCacheMap<K, V> extends AbstractCacheMap<K, V> {

    /** 引用队列，用于感知被 GC 回收的值。 */
    private final ReferenceQueue<V> queue = new ReferenceQueue<V>();
    
    /** 引用类型（软引用或弱引用）。 */
    private final ReferenceCachedValue.Type type;
    
    /** 创建使用弱引用值的缓存映射。 */
    public static <K, V> ReferenceCacheMap<K, V> weak(long timeToLiveInMillis, long maxIdleInMillis) {
        return new ReferenceCacheMap<K, V>(timeToLiveInMillis, maxIdleInMillis, Type.WEAK);
    }
    
    /** 创建使用软引用值的缓存映射。 */
    public static <K, V> ReferenceCacheMap<K, V> soft(long timeToLiveInMillis, long maxIdleInMillis) {
        return new ReferenceCacheMap<K, V>(timeToLiveInMillis, maxIdleInMillis, Type.SOFT);
    }
    
    ReferenceCacheMap(long timeToLiveInMillis, long maxIdleInMillis, ReferenceCachedValue.Type type) {
        super(0, timeToLiveInMillis, maxIdleInMillis);
        this.type = type;
    }

    @Override
    protected CachedValue<K, V> create(K key, V value, long ttl, long maxIdleTime) {
        return new ReferenceCachedValue<K, V>(key, value, ttl, maxIdleTime, queue, type);
    }

    /** 引用缓存无固定容量上限，始终视为可写入。 */
    @Override
    protected boolean isFull(K key) {
        return true;
    }

    @Override
    protected boolean removeExpiredEntries() {
        while (true) {
            CachedValueReference value = (CachedValueReference) queue.poll();
            if (value == null) {
                break;
            }
            if (map.remove(value.getOwner().getKey(), value.getOwner())) {
                onValueRemove((CachedValue<K, V>) value.getOwner());
            }
        }
        return super.removeExpiredEntries();
    }

    /** 无固定容量，映射满时不触发淘汰。 */
    @Override
    protected void onMapFull() {
    }

}